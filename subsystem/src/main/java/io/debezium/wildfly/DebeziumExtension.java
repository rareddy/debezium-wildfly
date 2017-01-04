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
