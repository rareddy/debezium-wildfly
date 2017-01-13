/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.wildfly;

import java.util.concurrent.LinkedBlockingDeque;

import org.apache.kafka.connect.source.SourceRecord;

@SuppressWarnings("serial")
class EventQueue extends LinkedBlockingDeque<SourceRecord> {
    private EventQueue delegate;
    
    public EventQueue(int capacity) {
        super(capacity);
    }
    
    public void delegate(EventQueue q) {
        this.delegate = q;
    }
    
}
