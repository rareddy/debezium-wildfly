/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.wildfly;

import static io.debezium.wildfly.Constants.*;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoAttributes;

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

class SubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, 
    XMLElementWriter<SubsystemMarshallingContext> {
    public static SubsystemParser INSTANCE = new SubsystemParser();
    
    @Override
    public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context)
            throws XMLStreamException {
        
        context.startSubsystemElement(Namespace.CURRENT.getUri(), false);
        ModelNode node = context.getModelNode();
        if (!node.isDefined()) {
            return;
        }

        if (like(node, Element.ASYNC_THREAD_POOL_ELEMENT)){
            writer.writeStartElement(Element.ASYNC_THREAD_POOL_ELEMENT.getLocalName());
            THREAD_COUNT_ATTRIBUTE.marshallAsAttribute(node, false, writer);
            writer.writeEndElement();
        }
        
        boolean hasChildren = node.hasDefined(Element.CONNECTOR.getLocalName())
                && node.get(Element.CONNECTOR.getLocalName()).asPropertyList().size() > 0;
        
        if (hasChildren) {
            writer.writeStartElement(Element.CONNECTORS.getLocalName());
            ModelNode connectors = node.get(Element.CONNECTOR.getLocalName());
            for (String name : connectors.keys()) {                    
                final ModelNode connector = connectors.get(name);
                writer.writeStartElement(Element.CONNECTOR.getLocalName());
                writeConnector(writer, connector, name);
                writer.writeEndElement();
            }            
            writer.writeEndElement();
        }
        writer.writeEndElement(); // End of subsystem element
    }
        
    private void writeConnector(XMLExtendedStreamWriter writer, ModelNode node, String name)
            throws XMLStreamException {
        writer.writeAttribute(CONNECTOR_NAME_ATTRIBUTE.getXmlName(), name);
        CONNECTOR_MODULE_ATTRIBUTE.marshallAsAttribute(node, false, writer);
        CONNECTOR_SLOT_ATTRIBUTE.marshallAsAttribute(node, false, writer);
        
        if (node.hasDefined(CONNECTOR_CONFIGURATION.getName())) {
            writer.writeStartElement(CONNECTOR_CONFIGURATION.getName());
            for (Property property : node.get(CONNECTOR_CONFIGURATION.getName()).asPropertyList()) {
                writeProperty(writer, property.getName(), property.getValue().asString(),
                        CONFIG_PROPERTY_VALUE.getXmlName());
            }
            writer.writeEndElement();
        }        
    }
        
    private void writeProperty(XMLExtendedStreamWriter writer, String name, String value, String localName)
            throws XMLStreamException {
        writer.writeStartElement(localName);
        writer.writeAttribute("name", name);
        writer.writeAttribute("value", value);
        writer.writeEndElement();
    }    

    @Override
    public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {
        final ModelNode address = new ModelNode();
        address.add(SUBSYSTEM, DebeziumExtension.DEBEZIUM_SUBSYSTEM);
        address.protect();
        
        final ModelNode bootServices = new ModelNode();
        bootServices.get(OP).set(ADD);
        bootServices.get(OP_ADDR).set(address);
        list.add(bootServices);  
        
        // no attributes 
        requireNoAttributes(reader);

        // elements
        while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
                case DEBEZIUM_1_0: {
                    Element element = Element.forName(reader.getLocalName());
                    switch (element) {
                    case CONNECTORS:
                        parseConnectors(reader, address.clone(), list);
                        break;
                    case ASYNC_THREAD_POOL_ELEMENT:
                        parseAsyncThreadConfiguration(reader, bootServices);
                        break;
                     default: 
                        throw ParseUtils.unexpectedElement(reader);
                    }
                    break;
                }
                default: {
                    throw ParseUtils.unexpectedElement(reader);
                }
            }
        }  
    }
    
    private ModelNode parseAsyncThreadConfiguration(XMLExtendedStreamReader reader,
            ModelNode node) throws XMLStreamException {
        if (reader.getAttributeCount() > 0) {
            for(int i=0; i<reader.getAttributeCount(); i++) {
                String attrName = reader.getAttributeLocalName(i);
                String attrValue = reader.getAttributeValue(i);
                Element element = Element.forName(attrName);
                switch(element) {
                case MAX_THREAD_COUNT_ATTRIBUTE:
                    node.get(element.getLocalName()).set(attrValue);
                    break;
                default: 
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }               
            }
        }
        while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT));
        return node;        
    }    

    private void parseConnectors(final XMLExtendedStreamReader reader, ModelNode address, final List<ModelNode> list)
            throws XMLStreamException {
         
        requireNoAttributes(reader);       
        
        while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
            Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case CONNECTOR:
                    parseConnector(reader, address, list);
                    break;
                default: 
                    throw ParseUtils.unexpectedElement(reader);
            }
        }
    }
    
    private void parseConnector(XMLExtendedStreamReader reader, ModelNode parentAddress, final List<ModelNode> list)
            throws XMLStreamException {
        
        ModelNode connectorNode = new ModelNode();
        String name = null;
        if (reader.getAttributeCount() > 0) {
            for(int i=0; i<reader.getAttributeCount(); i++) {
                String attrName = reader.getAttributeLocalName(i);
                String attrValue = reader.getAttributeValue(i);
                
                Element element = Element.forName(attrName);
                switch(element) {
                case NAME:
                    name = attrValue;
                    break;
                case MODULE:
                case SLOT:
                    connectorNode.get(element.getLocalName()).set(attrValue);
                    break;
                default: 
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
        }
        ModelNode address = parentAddress.clone();
        address.add(Element.CONNECTOR.getLocalName(), name);
        address.protect();
        connectorNode.get(OP).set(ADD);
        connectorNode.get(OP_ADDR).set(address);
        
        if (name != null) {
            list.add(connectorNode);  
        }
        else {
            throw new XMLStreamException();
        }                    
        
         parseConfiguration(reader, connectorNode);
    }
    
    private void parseConfiguration(XMLExtendedStreamReader reader, ModelNode parentNode)
            throws XMLStreamException {
        
        while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
            Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case CONFIGURATION:
                    parseConfigurationProperties("config-property", reader, parentNode);
                    break;                    
                default: 
                    throw ParseUtils.unexpectedElement(reader);
            }
        }
    }    
    
    private void parseConfigurationProperties(String childElementName, XMLExtendedStreamReader reader, ModelNode node)
            throws XMLStreamException {
        requireNoAttributes(reader);
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            final Element element = Element.forName(reader.getLocalName());
            if (childElementName.equals(element.getLocalName())) {
                final String[] array = ParseUtils.requireAttributes(reader, org.jboss.as.controller.parsing.Attribute.NAME.getLocalName(), org.jboss.as.controller.parsing.Attribute.VALUE.getLocalName());
                CONNECTOR_CONFIGURATION.parseAndAddParameterElement(array[0], array[1], node, reader);
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
            ParseUtils.requireNoContent(reader);
        }
    }
}
