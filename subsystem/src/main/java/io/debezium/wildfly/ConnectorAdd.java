/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.wildfly;

import static io.debezium.wildfly.Constants.*;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.jboss.as.controller.*;
import org.jboss.dmr.ModelNode;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.clustering.singleton.SingletonPolicy;

import io.debezium.embedded.EmbeddedEngine;

class ConnectorAdd extends AbstractAddStepHandler {
    public static ConnectorAdd INSTANCE = new ConnectorAdd();
    
    static AttributeDefinition[] ATTRIBUTES = {
            Constants.CONNECTOR_MODULE_ATTRIBUTE,
            Constants.CONNECTOR_SLOT_ATTRIBUTE,
            Constants.CONNECTOR_CONFIGURATION
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

        final String connectorName = pathAddress.getLastElement().getValue();
        
        String moduleName = null;
        if (isDefined(CONNECTOR_MODULE_ATTRIBUTE, operation, context)) {
            moduleName = asString(CONNECTOR_MODULE_ATTRIBUTE, operation, context);
        }
        
        String slot = null;
        if (isDefined(CONNECTOR_SLOT_ATTRIBUTE, operation, context)) {
            slot = asString(CONNECTOR_SLOT_ATTRIBUTE, operation, context);
        }
        
        Map<String, String> properties = null;
        if (isDefined(CONNECTOR_CONFIGURATION, operation, context)) {
            properties = asProperties(CONNECTOR_CONFIGURATION, operation, context);
        }          
        
        final ServiceTarget target = context.getServiceTarget();

        final Module module;
        String connectorClass = null;
        ClassLoader classloader = this.getClass().getClassLoader();
        ModuleLoader ml = Module.getCallerModuleLoader();
        if (moduleName != null && ml != null) {
            try {
                ModuleIdentifier id = ModuleIdentifier.create(moduleName);
                if (slot != null) {
                    id = ModuleIdentifier.create(moduleName, slot);
                }
                module = ml.loadModule(id);
                connectorClass = module.getProperty(EmbeddedEngine.CONNECTOR_CLASS.name());
                if (connectorClass == null) {
                    throw new OperationFailedException("connector.class property is not set in module "+moduleName);
                }
                if (properties == null) {
                    properties = new HashMap<String, String>();
                }
                for (String p : module.getPropertyNames()) {
                    if (!properties.containsKey(p)) {
                        properties.put(p, module.getProperty(p));
                    }
                }
                classloader = module.getClassLoader();
            } catch (ModuleLoadException e) {
                throw new OperationFailedException("Module load failed ", e); 
            }
        }
        
        try {
            SingletonPolicy policy = (SingletonPolicy) context.getServiceRegistry(true).getRequiredService(ServiceName.parse(SingletonPolicy.CAPABILITY_NAME)).awaitValue();
            ConnectorService service = new ConnectorService(connectorName, connectorClass, classloader, properties);
            ServiceBuilder<EventQueue> builder = policy.createSingletonServiceBuilder(ServiceNames.connectorServiceName(connectorName), service).build(target);     
            builder.addDependency(ServiceNames.THREAD_POOL_SERVICE, Executor.class, service.executorInjector);
            builder.addDependency(ServiceNames.EVENTS_SERVICE, EventQueue.class, service.eventsInjector);
            builder.addDependency(ServiceNames.PATH_SERVICE, String.class, service.pathInjector);
            builder.setInitialMode(ServiceController.Mode.ACTIVE).install();
        } catch (InterruptedException e) {
            throw new OperationFailedException(e);
        }          
    }    
}
