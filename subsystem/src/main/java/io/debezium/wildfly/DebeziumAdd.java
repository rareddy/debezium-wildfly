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

import static io.debezium.wildfly.Constants.ASYNC_THREAD_POOL_ELEMENT;
import static io.debezium.wildfly.Constants.THREAD_COUNT_ATTRIBUTE;
import static io.debezium.wildfly.Constants.asInt;
import static io.debezium.wildfly.Constants.isDefined;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceTarget;

class DebeziumAdd extends AbstractAddStepHandler {
    
    public static DebeziumAdd INSTANCE = new DebeziumAdd(); 

    static SimpleAttributeDefinition[] ATTRIBUTES = {
            Constants.THREAD_COUNT_ATTRIBUTE
    };
    
    @Override
    protected void populateModel(final OperationContext context,
            final ModelNode operation, final Resource resource)
            throws OperationFailedException {    
        resource.getModel().setEmptyObject();
        populate(operation, resource.getModel());
    }
    
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        throw new UnsupportedOperationException();
    }

    static void populate(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (int i = 0; i < ATTRIBUTES.length; i++) {
            ATTRIBUTES[i].validateAndSet(operation, model);
        }
    }
    
    @Override
    protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model)
            throws OperationFailedException {
        
        ServiceTarget target = context.getServiceTarget();

        int maxThreads = 10;
        if (isDefined(ASYNC_THREAD_POOL_ELEMENT, operation, context)) {
            if(asInt(THREAD_COUNT_ATTRIBUTE, operation, context) != null) {
                maxThreads = asInt(THREAD_COUNT_ATTRIBUTE, operation, context);
            }
        }
        
        ThreadExecutorService executorService = new ThreadExecutorService(maxThreads);
        ServiceBuilder<?> serviceBuilder = target.addService(ServiceNames.THREAD_POOL_SERVICE, executorService);
        serviceBuilder.install();
        
        EventsFunnelService eventsService = new EventsFunnelService();
        serviceBuilder = target.addService(ServiceNames.EVENTS_SERVICE, eventsService);
        serviceBuilder.install();
    }

}
