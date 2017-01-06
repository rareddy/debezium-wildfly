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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import org.jboss.as.controller.*;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;

class SubsytemResourceDefinition extends SimpleResourceDefinition {
    protected static final PathElement PATH_SUBSYSTEM = PathElement.pathElement(SUBSYSTEM, DebeziumExtension.DEBEZIUM_SUBSYSTEM);
    private boolean server;
    
    public SubsytemResourceDefinition(boolean server) {
        super(PATH_SUBSYSTEM,
              DebeziumExtension.getResourceDescriptionResolver(DebeziumExtension.DEBEZIUM_SUBSYSTEM),
              DebeziumAdd.INSTANCE, 
              DebeziumRemove.INSTANCE);
        this.server = server;
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION,
                GenericSubsystemDescribeHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        for (int i = 0; i < DebeziumAdd.ATTRIBUTES.length; i++) {
            resourceRegistration.registerReadWriteAttribute(DebeziumAdd.ATTRIBUTES[i], null, new AttributeWrite(DebeziumAdd.ATTRIBUTES[i]));
        }        
    }
    
    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerSubModel(new ConnectorResourceDefinition());        
    }
 
    public static class AttributeWrite extends AbstractWriteAttributeHandler<Void> {

        public AttributeWrite(AttributeDefinition... attr) {
            super(attr);
        }

        @Override
        protected boolean applyUpdateToRuntime(OperationContext context,ModelNode operation,String attributeName,ModelNode resolvedValue,
                ModelNode currentValue, org.jboss.as.controller.AbstractWriteAttributeHandler.HandbackHolder<Void> handbackHolder)
                throws OperationFailedException {
            return true;
        }

        @Override
        protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
                ModelNode valueToRestore, ModelNode valueToRevert, Void handback)
                throws OperationFailedException {
        }
    }    
}
