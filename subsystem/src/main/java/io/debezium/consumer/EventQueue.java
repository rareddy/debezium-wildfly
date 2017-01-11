/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.consumer;

import java.util.concurrent.LinkedBlockingDeque;

import org.apache.kafka.connect.source.SourceRecord;

@SuppressWarnings("serial")
public class EventQueue extends LinkedBlockingDeque<SourceRecord> {
    public EventQueue(int capacity) {
        super(capacity);
    }
}
