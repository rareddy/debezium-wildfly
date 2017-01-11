/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.wildfly;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.ExtensionParsingContext;

public class DebeziumExtension implements Extension {
        
    public static final String DEBEZIUM_SUBSYSTEM = "debezium"; //$NON-NLS-1$
    public static ModelVersion DEBEZIUM_VERSION = ModelVersion.create(1, 0);
    
    public static ResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix) {
        return new StandardResourceDescriptionResolver(keyPrefix,
                "io.debezium.wildfly.i18n",
                DebeziumExtension.class.getClassLoader(), true, false);
    }
    
    @Override
    public void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(DEBEZIUM_SUBSYSTEM, DEBEZIUM_VERSION);
        subsystem.registerXMLElementWriter(SubsystemParser.INSTANCE);
        subsystem.registerSubsystemModel(new SubsytemResourceDefinition(context.getProcessType().isServer()));
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(DEBEZIUM_SUBSYSTEM, Namespace.CURRENT.getUri(), SubsystemParser.INSTANCE);
    }
}
