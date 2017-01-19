/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.wildfly;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.apache.kafka.connect.storage.FileOffsetBackingStore;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.config.Configuration;
import io.debezium.embedded.ConnectorEngine;
import io.debezium.embedded.EmbeddedEngine;

public class EventStreamService implements Service<ConnectorEngine> {
    private final static Logger logger = LoggerFactory.getLogger(EventStreamService.class);
    
    protected final InjectedValue<ExecutorService> executorInjector = new InjectedValue<ExecutorService>();
    protected InjectedValue<String> pathInjector = new InjectedValue<String>();
    private String name;
    private ConnectorEngine engine;
    private Map<String, String> configuration;
    
    public EventStreamService(String name, Map<String, String> config) {
        this.name = name;
        this.configuration = config;
    }
    
    @Override
    public ConnectorEngine getValue() throws IllegalStateException, IllegalArgumentException {
        return this.engine;
    }
    
    @Override
    public void start(StartContext context) throws StartException {
        Configuration.Builder b = Configuration.create();
        if (this.configuration != null) {
            for (String key : this.configuration.keySet()) {
                b.with(key, this.configuration.get(key));
            }
        }

        // these properties must be generic to the all the connectors.
        b.with(EmbeddedEngine.ENGINE_NAME.name(), this.name);
        b.with(ConnectorEngine.OFFSET_STORAGE, FileOffsetBackingStore.class.getName());
        b.with(ConnectorEngine.OFFSET_STORAGE_FILE_FILENAME.name(), pathInjector.getValue() + "/" + this.name + "-offset.dat");

        Configuration config = b.build();
        
        File dataDirectory = new File(pathInjector.getValue());
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }
        
        this.engine = new ConnectorEngine(config, executorInjector.getValue());
        this.engine.start();
        
        logger.info("Started {} Debezium EventStream", this.name);        
    }

    @Override
    public void stop(StopContext context) {
        try {
            this.engine.close();
            logger.info("Stopped {} Debezium EventStream", this.name);
        } catch ( ExecutionException | InterruptedException e ) {
            Thread.interrupted();
        }        
    }
}
