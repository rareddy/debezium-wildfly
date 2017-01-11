/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.debezium.wildfly;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

import io.debezium.consumer.EventQueue;

/**
 * Single service to funnel all the events from various connectors in Debezium
 * This will be bounded queue, upon reaching the max, then it block items to be removed
 */
class EventsFunnelService implements Service<EventQueue> {

    private EventQueue events;
    
    @Override
    public EventQueue getValue() throws IllegalStateException, IllegalArgumentException {
        return events;
    }

    @Override
    public void start(StartContext context) throws StartException {
        // TODO: queue depth needs to be configurable
        this.events = new EventQueue(1000);
    }

    @Override
    public void stop(StopContext context) {
        this.events = null;
    }
}
