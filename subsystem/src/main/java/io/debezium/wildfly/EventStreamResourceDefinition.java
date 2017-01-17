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

class EventStreamResourceDefinition extends SimpleResourceDefinition {
    public static final PathElement EVENT_STREAM_PATH = PathElement.pathElement(Element.EVENT_STREAM.getLocalName());

    public EventStreamResourceDefinition() {
        super(EVENT_STREAM_PATH, DebeziumExtension.getResourceDescriptionResolver(Element.EVENT_STREAM.getLocalName()), 
                EventStreamAdd.INSTANCE,
                EventStreamRemove.INSTANCE);
    }
    
    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION,
                GenericSubsystemDescribeHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        for (int i = 0; i < EventStreamAdd.ATTRIBUTES.length; i++) {
			resourceRegistration.registerReadWriteAttribute(EventStreamAdd.ATTRIBUTES[i], null,
					new SubsytemResourceDefinition.AttributeWrite(EventStreamAdd.ATTRIBUTES[i]));
        }
    }
    
    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerSubModel(new ConnectorResourceDefinition());
    }
}