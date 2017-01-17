/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.wildfly;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import org.jboss.as.controller.*;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;

class SubsytemResourceDefinition extends SimpleResourceDefinition {
    protected static final PathElement PATH_SUBSYSTEM = PathElement.pathElement(SUBSYSTEM, DebeziumExtension.DEBEZIUM_SUBSYSTEM);
    
    public SubsytemResourceDefinition(boolean server) {
        super(PATH_SUBSYSTEM,
              DebeziumExtension.getResourceDescriptionResolver(DebeziumExtension.DEBEZIUM_SUBSYSTEM),
              DebeziumAdd.INSTANCE, 
              DebeziumRemove.INSTANCE);
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
        resourceRegistration.registerSubModel(new EventStreamResourceDefinition());        
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
