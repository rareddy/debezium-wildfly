/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.wildfly;

import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

class ConnectorResourceDefinition extends SimpleResourceDefinition {
    public static final PathElement CONNECTOR_PATH = PathElement.pathElement(Element.CONNECTOR.getLocalName());

    public ConnectorResourceDefinition() {
        super(CONNECTOR_PATH, DebeziumExtension.getResourceDescriptionResolver(Element.CONNECTOR.getLocalName()), 
                ConnectorAdd.INSTANCE,
                ConnectorRemove.INSTANCE);
    }
    
    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION,
                GenericSubsystemDescribeHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        for (int i = 0; i < ConnectorAdd.ATTRIBUTES.length; i++) {
			resourceRegistration.registerReadWriteAttribute(ConnectorAdd.ATTRIBUTES[i], null,
					new SubsytemResourceDefinition.AttributeWrite(ConnectorAdd.ATTRIBUTES[i]));
        }
    }
    
    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
    }
}