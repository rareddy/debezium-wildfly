/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.wildfly;

import org.jboss.msc.service.ServiceName;

class ServiceNames {
    public static ServiceName ENGINE = ServiceName.JBOSS.append("debezium", "engine"); //$NON-NLS-1$ //$NON-NLS-2$
    public static ServiceName CONNECTOR_BASE = ServiceName.JBOSS.append("debezium", "connector");//$NON-NLS-1$ //$NON-NLS-2$
    public static ServiceName EVENT_STREAM_BASE = ServiceName.JBOSS.append("debezium", "event-stream");//$NON-NLS-1$ //$NON-NLS-2$
    public static ServiceName THREAD_POOL_SERVICE = ServiceName.JBOSS.append("debezium","async-threads"); //$NON-NLS-1$ //$NON-NLS-2$
    public static ServiceName EVENTS_SERVICE = ServiceName.JBOSS.append("debezium","events"); //$NON-NLS-1$ //$NON-NLS-2$
    public static ServiceName PATH_SERVICE = ServiceName.JBOSS.append("debezium","data-directory-path"); //$NON-NLS-1$ //$NON-NLS-2$

    public static ServiceName connectorServiceName(String eventStreamName, String name) {
        return eventStreamServiceName(eventStreamName).append(name);
    }
    
    public static ServiceName eventStreamServiceName(String name) {
        return ServiceName.of(EVENT_STREAM_BASE, name);
    }
}
