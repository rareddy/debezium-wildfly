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

import org.jboss.msc.service.ServiceName;

class ServiceNames {
    public static ServiceName ENGINE = ServiceName.JBOSS.append("debezium", "engine"); //$NON-NLS-1$ //$NON-NLS-2$
    public static ServiceName CONNECTOR_BASE = ServiceName.JBOSS.append("debezium", "connector");//$NON-NLS-1$ //$NON-NLS-2$
    public static ServiceName THREAD_POOL_SERVICE = ServiceName.JBOSS.append("debezium","async-threads"); //$NON-NLS-1$ //$NON-NLS-2$
    
    public static ServiceName connectorServiceName(String name) {
        return ServiceName.of(CONNECTOR_BASE, name);
    }
}
