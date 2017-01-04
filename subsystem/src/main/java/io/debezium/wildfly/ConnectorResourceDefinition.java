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