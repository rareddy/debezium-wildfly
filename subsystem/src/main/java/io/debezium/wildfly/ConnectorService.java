/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.debezium.wildfly;

import java.io.File;
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
import io.debezium.embedded.EmbeddedEngine;

class ConnectorService implements Service<EventQueue> {
    private final static Logger logger = LoggerFactory.getLogger(ConnectorService.class);
    private final String name;
    private final String connectorClass;
    private final ClassLoader classLoader;
    private final Map<String, String> properties;
    protected final InjectedValue<Executor> executorInjector = new InjectedValue<Executor>();
    protected final InjectedValue<EventQueue> eventsInjector = new InjectedValue<EventQueue>();
    protected InjectedValue<String> pathInjector = new InjectedValue<String>();
    private EmbeddedEngine engine;
    
    ConnectorService(String name, String connectorClass, ClassLoader classLoader, Map<String, String> properties){
        this.name = name;
        this.connectorClass = connectorClass;
        this.classLoader = classLoader;
        this.properties = properties;
    }
    
    private String getProperty(String key, String defaultValue) {
        String value = this.properties.get(key);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }
        return defaultValue;
    }
    
    @Override
    public void start(StartContext context) throws StartException {
        Configuration.Builder b = Configuration.create();
        if (this.properties != null && !this.properties.isEmpty()) {
            for (String key : this.properties.keySet()) {
                // value substitute from module based properties
                String value = this.properties.get(key);
                value = value.replace("${jboss.node.name}", System.getProperty("jboss.node.name"));
                value = value.replace("${jboss.server.data.dir}", pathInjector.getValue());
                value = value.replace("${connector-name}", this.name);
                b.with(key, value);
            }
        }
        // these properties must be generic to the all the connectors.
        b.with(EmbeddedEngine.ENGINE_NAME.name(), this.name);
        b.with(EmbeddedEngine.ENGINE_NAME.name(), this.name);
        b.with(EmbeddedEngine.CONNECTOR_CLASS.name(), this.connectorClass);
        b.with(EmbeddedEngine.OFFSET_STORAGE_FILE_FILENAME.name(),
                getProperty(EmbeddedEngine.OFFSET_STORAGE_FILE_FILENAME.name(),
                        pathInjector.getValue() + "/" + this.name + "-offset.dat"));

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
        
        File dataDirectory = new File(pathInjector.getValue());
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
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
    public EventQueue getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }
}
