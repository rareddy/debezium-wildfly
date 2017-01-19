/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.wildfly;

import static io.debezium.wildfly.Constants.EVENT_STREAM_JNDI_NAME_ATTRIBUTE;
import static io.debezium.wildfly.Constants.asProperties;
import static io.debezium.wildfly.Constants.asString;
import static io.debezium.wildfly.Constants.isDefined;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.jboss.as.controller.*;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

import io.debezium.embedded.ConnectorEngine;

class EventStreamAdd extends AbstractAddStepHandler {
    public static EventStreamAdd INSTANCE = new EventStreamAdd();
    
    static AttributeDefinition[] ATTRIBUTES = {
            Constants.ITEM_CONFIGURATION,
            Constants.EVENT_STREAM_JNDI_NAME_ATTRIBUTE
    };    
    
    @Override
    protected void populateModel(final ModelNode operation, final ModelNode model) throws OperationFailedException{
        for (int i = 0; i < ATTRIBUTES.length; i++) {
            ATTRIBUTES[i].validateAndSet(operation, model);
        }
    }
    
    @Override
    protected void performRuntime(final OperationContext context,
            final ModelNode operation, final ModelNode model)
            throws OperationFailedException {

        final ModelNode address = operation.require(OP_ADDR);
        final PathAddress pathAddress = PathAddress.pathAddress(address);

        final String name = pathAddress.getLastElement().getValue();
        
        String jndiName = null;
        if (isDefined(EVENT_STREAM_JNDI_NAME_ATTRIBUTE, operation, context)) {
            if(asString(EVENT_STREAM_JNDI_NAME_ATTRIBUTE, operation, context) != null) {
                jndiName = asString(EVENT_STREAM_JNDI_NAME_ATTRIBUTE, operation, context);
            }
        }
        
        if (jndiName == null) {
            throw new OperationFailedException("Event Stream JNDI name not found, must supply a JNDI name");
        }
        
        Map<String, String> properties = null;
        if (isDefined(Constants.ITEM_CONFIGURATION, operation, context)) {
            properties = asProperties(Constants.ITEM_CONFIGURATION, operation, context);
        }          
        
        final ServiceTarget target = context.getServiceTarget();
        
        // create event stream service.
        EventStreamService service = new EventStreamService(name, properties);
        ServiceBuilder<ConnectorEngine> builder = target.addService(ServiceNames.eventStreamServiceName(name), service);     
        builder.addDependency(ServiceNames.THREAD_POOL_SERVICE, ExecutorService.class, service.executorInjector);
        builder.addDependency(ServiceNames.PATH_SERVICE, String.class, service.pathInjector);
        builder.setInitialMode(ServiceController.Mode.ACTIVE);
        builder.install();
        
        // create a JNDI binding for the above event stream
        final ReferenceFactoryService<ConnectorEngine> referenceFactoryService = new ReferenceFactoryService<ConnectorEngine>();
        final ServiceName referenceFactoryServiceName = ServiceNames.eventStreamServiceName(name).append("reference-factory"); //$NON-NLS-1$
        final ServiceBuilder<?> referenceBuilder = target.addService(referenceFactoryServiceName, referenceFactoryService);
        referenceBuilder.addDependency(ServiceNames.eventStreamServiceName(name), ConnectorEngine.class, referenceFactoryService.getInjector());
        referenceBuilder.setInitialMode(ServiceController.Mode.ACTIVE);
        referenceBuilder.install();
        
        final ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(jndiName); 
        final BinderService binderService = new BinderService(bindInfo.getBindName());
        final ServiceBuilder<?> binderBuilder = target.addService(bindInfo.getBinderServiceName(), binderService);
        binderBuilder.addDependency(referenceFactoryServiceName, ManagedReferenceFactory.class, binderService.getManagedObjectInjector());
        binderBuilder.addDependency(bindInfo.getParentContextServiceName(), ServiceBasedNamingStore.class, binderService.getNamingStoreInjector());        
        binderBuilder.setInitialMode(ServiceController.Mode.ACTIVE);        
        binderBuilder.install();
    }
}