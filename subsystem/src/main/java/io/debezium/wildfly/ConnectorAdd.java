/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package io.debezium.wildfly;

import static io.debezium.wildfly.Constants.*;
import static io.debezium.wildfly.Constants.asString;
import static io.debezium.wildfly.Constants.isDefined;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.concurrent.Executor;

import org.jboss.as.controller.*;
import org.jboss.dmr.ModelNode;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;

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
        
        final ServiceTarget target = context.getServiceTarget();

        final Module module;
        ClassLoader classloader = this.getClass().getClassLoader();
        ModuleLoader ml = Module.getCallerModuleLoader();
        if (moduleName != null && ml != null) {
            try {
                ModuleIdentifier id = ModuleIdentifier.create(moduleName);
                if (slot != null) {
                    id = ModuleIdentifier.create(moduleName, slot);
                }
                module = ml.loadModule(id);
                classloader = module.getClassLoader();
            } catch (ModuleLoadException e) {
                throw new OperationFailedException("Module load failed ", e); 
            }
        }
        
		ConnectorService service = new ConnectorService(classloader);
		ServiceBuilder<Void> builder = target.addService(ServiceNames.connectorServiceName(connectorName), service);
		builder.addDependency(ServiceNames.THREAD_POOL_SERVICE, Executor.class, service.executorInjector);
		builder.setInitialMode(ServiceController.Mode.ACTIVE).install();
    }    
}