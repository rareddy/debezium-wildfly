/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
    CONNECTORS("connectors", "connectors"), 
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

