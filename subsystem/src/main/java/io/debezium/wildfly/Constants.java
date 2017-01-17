/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.wildfly;

import java.util.Map;
import java.util.Set;

import org.jboss.as.controller.*;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

class Constants {
    private static final String NAME = "name";
    private static final String CONNECTOR_MODULE= "module";
    private static final String CONNECTOR_SLOT= "slot";
    private static final String CONFIGURATION= "configuration";
    private static final String CONFIGURATION_PROPERTY = "config-property";
    
    
    public static SimpleAttributeDefinition EVENT_STREAM_NAME_ATTRIBUTE = new SimpleAttributeDefinitionBuilder(
            NAME, ModelType.STRING)
                    .setXmlName(NAME)
                    .setAllowExpression(false)
                    .setAllowNull(true)
                    .build();
    
    public static SimpleAttributeDefinition CONNECTOR_NAME_ATTRIBUTE = new SimpleAttributeDefinitionBuilder(
            NAME, ModelType.STRING)
                    .setXmlName(NAME)
                    .setAllowExpression(false)
                    .setAllowNull(true)
                    .build();
    
    public static SimpleAttributeDefinition CONNECTOR_MODULE_ATTRIBUTE = new SimpleAttributeDefinitionBuilder(
            CONNECTOR_MODULE, ModelType.STRING)
                    .setXmlName(CONNECTOR_MODULE)
                    .setAllowExpression(false)
                    .setAllowNull(false)
                    .build();
    
    public static SimpleAttributeDefinition CONNECTOR_SLOT_ATTRIBUTE = new SimpleAttributeDefinitionBuilder(
            CONNECTOR_SLOT, ModelType.STRING)
                    .setXmlName(CONNECTOR_SLOT)
                    .setAllowExpression(false)
                    .setAllowNull(true)
                    .build();
    
    static final PropertiesAttributeDefinition ITEM_CONFIGURATION = new PropertiesAttributeDefinition.Builder(
            CONFIGURATION, true)
                .setAllowExpression(true)
                .build();
    
    static final SimpleAttributeDefinition CONFIG_PROPERTY_VALUE = new SimpleAttributeDefinitionBuilder(
            CONFIGURATION_PROPERTY, ModelType.STRING, true)
                .setXmlName(CONFIGURATION_PROPERTY)
                .setAllowExpression(true)
                .build();    
    
    static SimpleAttributeDefinition ASYNC_THREAD_POOL_ELEMENT = new SimpleAttributeDefinitionBuilder(
            Element.ASYNC_THREAD_POOL_ELEMENT.getLocalName(), ModelType.STRING)
            .setXmlName(Element.ASYNC_THREAD_POOL_ELEMENT.getXmlName())
            .setAllowNull(true)
            .setAllowExpression(false)
            .setDefaultValue(new ModelNode(false))
            .build();
    
    public static SimpleAttributeDefinition THREAD_COUNT_ATTRIBUTE = new SimpleAttributeDefinitionBuilder(
            Element.MAX_THREAD_COUNT_ATTRIBUTE.getLocalName(), ModelType.INT)
            .setXmlName(Element.MAX_THREAD_COUNT_ATTRIBUTE.getXmlName())
            .setAllowNull(true)
            .setAllowExpression(false)
            .setDefaultValue(new ModelNode(10))
            .build();    
    
    static boolean isDefined(final AttributeDefinition attr, final ModelNode model,
            final OperationContext context) throws OperationFailedException {
        ModelNode resolvedNode = attr.resolveModelAttribute(context, model);
        return resolvedNode.isDefined();        
    }
    
    static Integer asInt(final SimpleAttributeDefinition attr, final ModelNode node,
            final OperationContext context) throws OperationFailedException {
        ModelNode resolvedNode = attr.resolveModelAttribute(context, node);
        return resolvedNode.isDefined() ? resolvedNode.asInt() : null;
    }
    
    static Long asLong(final SimpleAttributeDefinition attr, ModelNode node, OperationContext context)
            throws OperationFailedException {
        ModelNode resolvedNode = attr.resolveModelAttribute(context, node);
        return resolvedNode.isDefined() ? resolvedNode.asLong() : null;
    }
    
    static String asString(final SimpleAttributeDefinition attr, ModelNode node, OperationContext context)
            throws OperationFailedException {
        ModelNode resolvedNode = attr.resolveModelAttribute(context, node);
        return resolvedNode.isDefined() ? resolvedNode.asString() : null;
    }
    
    static Map<String, String> asProperties(final PropertiesAttributeDefinition attr, ModelNode node, OperationContext context)
            throws OperationFailedException {
        ModelNode model = node.get(attr.getName());
        return PropertiesAttributeDefinition.unwrapModel(context, model);
    }
    
    public static Boolean asBoolean(final SimpleAttributeDefinition attr, ModelNode node, OperationContext context)
            throws OperationFailedException {
        ModelNode resolvedNode = attr.resolveModelAttribute(context, node);
        return resolvedNode.isDefined() ? resolvedNode.asBoolean() : null;
    }    
    public static boolean like(ModelNode node, Element element) {
        if (node.isDefined()) {            
            Set<String> keys = node.keys();
            for (String key:keys) {
                if (key.startsWith(element.getLocalName()) && node.get(key).isDefined()) {
                    return true;
                }
            }
        }
        return false;
    }
}
