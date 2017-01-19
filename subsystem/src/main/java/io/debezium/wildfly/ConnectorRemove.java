/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.wildfly;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;

class ConnectorRemove extends AbstractRemoveStepHandler {
    public static ConnectorRemove INSTANCE = new ConnectorRemove();
    
    @Override
    protected void performRuntime(OperationContext context,
            final ModelNode operation, final ModelNode model)
            throws OperationFailedException {
                
        final ModelNode address = operation.require(OP_ADDR);
        final PathAddress pathAddress = PathAddress.pathAddress(address);

        final String connectorName = pathAddress.getLastElement().getValue();
        final String eventStreamName = pathAddress.getElement(1).getValue();

        final ServiceRegistry registry = context.getServiceRegistry(true);
        final ServiceName serviceName = ServiceNames.connectorServiceName(eventStreamName, connectorName);
        final ServiceController<?> controller = registry.getService(serviceName);
        if (controller != null) {
            context.removeService(serviceName);
        }
    }
}
