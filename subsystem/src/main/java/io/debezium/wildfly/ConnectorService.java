/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.debezium.wildfly;

import java.util.Map;
import java.util.Properties;

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
    
    private final String name;
    private final String connectorClass;
    private final ClassLoader classLoader;
    private final Map<String, String> properties;
    
    ConnectorService(String name, String connectorClass, ClassLoader classLoader, Map<String, String> properties){
        this.name = name;
        this.connectorClass = connectorClass;
        this.classLoader = classLoader;
        this.properties = properties;
    }
        
    @Override
    public void start(StartContext context) throws StartException {
        ConnectorEngine engine = connectorEngineInjector.getValue();
        
        Configuration.Builder b = Configuration.create();
        b.with(EmbeddedEngine.CONNECTOR_CLASS.name(), this.connectorClass);
        resolve(this.properties, b);
        
        try {
            if (engine.deployConnector(b.build(), new ConnectorEngine.ConnectorCallback() {}, this.classLoader)) {
                logger.debug("Debezium {} Connector deployed and started", this.name);
            } else {
                logger.debug("Debezium {} Connector failed to deploy", this.name);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InterruptedException e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
        ConnectorEngine engine = connectorEngineInjector.getValue();
        engine.undeployConnector(name, null);
        logger.debug("Debezium {} Connector stopped and undeployed", this.name);
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
                value = value.replace("${connector-name}", this.name);
                
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
