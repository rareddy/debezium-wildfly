/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.debezium.wildfly;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("nls")
enum Element {
    // must be first
    UNKNOWN(null, null),

    // Connector
    ASYNC_THREAD_POOL_ELEMENT("async-thread-pool", "async-thread-pool"),
    MAX_THREAD_COUNT_ATTRIBUTE("async-thread-pool-max-thread-count", "max-thread-count"),
    EVENT_STREAM("event-stream", "event-stream"), 
    CONNECTOR("connector", "connector"), 
    NAME("name", "name"), 
    MODULE("module", "module"), 
    SLOT("slot", "slot"),
    CONFIGURATION("configuration", "configuration"),
    CONFIGURATION_PROPERTY("config-property", "config-property");
    
    private final String name;
    private final String xmlName;

    Element(final String name, final String xmlName) {
        this.name = name;
        this.xmlName = xmlName;
    }

    /**
     * Get the local name of this element.
     * @return the local name
     */
    public String getLocalName() {
        return name;
    }
    
    public String getXmlName() {
        return this.xmlName;
    }

    private static final Map<String, Element> MAP;

    static {
        final Map<String, Element> map = new HashMap<String, Element>();
        for (Element element : values()) {
            final String name = element.getXmlName();
            if (name != null)
                map.put(name, element);
        }
        MAP = map;
    }

    public static Element forName(String xmlName) {
        final Element element = MAP.get(xmlName);
        return element == null ? UNKNOWN : element;
    }  
}

