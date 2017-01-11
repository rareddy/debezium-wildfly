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

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.kafka.connect.source.SourceRecord;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.config.Configuration;
import io.debezium.consumer.EventQueue;
import io.debezium.embedded.EmbeddedEngine;

class ConnectorService implements Service<Void> {
    private final static Logger logger = LoggerFactory.getLogger(ConnectorService.class);
    private final String name;
    private final ClassLoader classLoader;
    private final Map<String, String> properties;
    protected final InjectedValue<Executor> executorInjector = new InjectedValue<Executor>();
    protected final InjectedValue<EventQueue> eventsInjector = new InjectedValue<EventQueue>();
    private EmbeddedEngine engine;
    
    ConnectorService(String name, ClassLoader classLoader, Map<String, String> properties){
        this.name = name;
        this.classLoader = classLoader;
        this.properties = properties;
    }
    
    @Override
    public void start(StartContext context) throws StartException {
        Configuration.Builder b = Configuration.create();
        if (this.properties != null && !this.properties.isEmpty()) {
            for (String key : this.properties.keySet()) {
                b.with(key, this.properties.get(key));
            }
        }
        Configuration config = b.build();
        
        Consumer<SourceRecord> c = new Consumer<SourceRecord>() {
            public void accept(SourceRecord record) {                
                EventQueue queue = eventsInjector.getValue();
                // the below call will block until the record is accepted, not good with
                // multi-thread environments, but here we are dealing single thread. alternative is coming up
                // with whole durable persistent mechanism for events. 
                while(true) {
                    try {
                        if (queue.offerLast(record, 1000, TimeUnit.MILLISECONDS)) {
                            logger.trace("Received event from topic = "+record.topic());
                            break;
                        }
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                        engine.stop();
                    }
                }
            }
        };
        
        String jdbcDriver = properties.get("database.jdbc.driver");
        if ( jdbcDriver != null) {
            try {
                @SuppressWarnings("unchecked")
                Class<Driver> driver = (Class<Driver>) Class.forName(jdbcDriver, false, this.classLoader);
                DriverManager.registerDriver(driver.newInstance());
            } catch (InstantiationException | IllegalAccessException | SQLException | ClassNotFoundException e) {
                logger.error("jdbc driver {} not found in classpath ", jdbcDriver);
            }
        }
        
        this.engine = EmbeddedEngine.create()
                .using(config)
                .using(this.classLoader)
                .notifying(c)
                .build();
        
        executorInjector.getValue().execute(this.engine);
        logger.info("Started Debezium connector for " + this.name);
    }

    @Override
    public void stop(StopContext context) {
        try {
            this.engine.stop();
            while (!this.engine.await(30, TimeUnit.SECONDS)) {
                logger.info("Wating another 30 seconds for the embedded enging to shut down");
            }
            logger.info("Stopped Debezium connector for " + this.name);
        } catch ( InterruptedException e ) {
            Thread.interrupted();
        }
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }
}
