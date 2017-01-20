/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.debezium.wildfly;

import java.util.Map;

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

class ConnectorService implements Service<Void> {
    private final static Logger logger = LoggerFactory.getLogger(ConnectorService.class);
    protected final InjectedValue<ConnectorEngine> connectorEngineInjector = new InjectedValue<ConnectorEngine>();
    protected InjectedValue<String> pathInjector = new InjectedValue<String>();
    
    private final String connectorName;
    private final String eventStreamName;
    private final String connectorClass;
    private final ClassLoader classLoader;
    private final Map<String, String> properties;
    
    ConnectorService(String eventStreamName, String connectorName, String connectorClass, ClassLoader classLoader, Map<String, String> properties){
        this.eventStreamName = eventStreamName;
        this.connectorName = connectorName;
        this.connectorClass = connectorClass;
        this.classLoader = classLoader;
        this.properties = properties;
    }
        
    @Override
    public void start(StartContext context) throws StartException {
        ConnectorEngine engine = connectorEngineInjector.getValue();
        
        Configuration.Builder b = Configuration.create();                
        resolve(this.properties, b);
        b.with(ConnectorEngine.CONNECTOR_NAME, this.eventStreamName+"."+this.connectorName);
        b.with(ConnectorEngine.CONNECTOR_CLASS.name(), this.connectorClass);
        
        try {
            if (engine.addConnector(b.build(), new ConnectorEngine.ConnectorCallback() {}, this.classLoader)) {
                logger.debug("Debezium {} Connector deployed and started", this.connectorName);
            } else {
                logger.debug("Debezium {} Connector failed to deploy", this.connectorName);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InterruptedException e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
        ConnectorEngine engine = connectorEngineInjector.getValue();
        engine.removeConnector(connectorName, null);
        logger.debug("Debezium {} Connector stopped and undeployed", this.connectorName);
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }
        
    void resolve(Map<String, String> initial, Configuration.Builder config) {
        if (initial != null && !initial.isEmpty()) {
            for (String key : initial.keySet()) {
                // value substitute from module based properties
                String value = initial.get(key);
                value = value.replace("${jboss.node.name}", System.getProperty("jboss.node.name"));
                value = value.replace("${jboss.server.data.dir}", pathInjector.getValue());
                value = value.replace("${connector-name}", this.connectorName);
                value = value.replace("${event-stream-name}", this.eventStreamName);
                
                int idx = value.indexOf("${");
                while (idx != -1) {
                    int end = value.indexOf('}');
                    String property = value.substring(idx+2, end);
                    value = value.replace("${"+property+"}", System.getProperty(property));
                    idx = value.indexOf("${");
                }
                config.with(key, value);
            }
        }        
    }
}
