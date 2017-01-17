/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.wildfly;

import static io.debezium.wildfly.Constants.asProperties;
import static io.debezium.wildfly.Constants.isDefined;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.jboss.as.controller.*;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;

import io.debezium.embedded.ConnectorEngine;

class EventStreamAdd extends AbstractAddStepHandler {
    public static EventStreamAdd INSTANCE = new EventStreamAdd();
    
    static AttributeDefinition[] ATTRIBUTES = {
            Constants.ITEM_CONFIGURATION
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
        
        Map<String, String> properties = null;
        if (isDefined(Constants.ITEM_CONFIGURATION, operation, context)) {
            properties = asProperties(Constants.ITEM_CONFIGURATION, operation, context);
        }          
        
        final ServiceTarget target = context.getServiceTarget();
        
        EventStreamService service = new EventStreamService(name, properties);
        ServiceBuilder<ConnectorEngine> builder = target.addService(ServiceNames.eventStreamServiceName(name), service);     
        builder.addDependency(ServiceNames.THREAD_POOL_SERVICE, ExecutorService.class, service.executorInjector);
        builder.addDependency(ServiceNames.PATH_SERVICE, String.class, service.pathInjector);
        builder.setInitialMode(ServiceController.Mode.ACTIVE).install();
    }    
}
