/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.debezium.wildfly;

import org.jboss.as.naming.ContextListAndJndiViewManagedReferenceFactory;
import org.jboss.as.naming.ContextListManagedReferenceFactory;
import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ValueManagedReference;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.msc.value.InjectedValue;


class ReferenceFactoryService<T> implements Service<ManagedReferenceFactory>, ContextListAndJndiViewManagedReferenceFactory {
    private final InjectedValue<T> injector = new InjectedValue<T>();

    private ManagedReference reference;

    public synchronized void start(StartContext startContext) throws StartException {
        reference = new ValueManagedReference(new ImmediateValue<Object>(injector.getValue()));
    }

    public synchronized void stop(StopContext stopContext) {
        reference = null;
    }

    public synchronized ManagedReferenceFactory getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public synchronized ManagedReference getReference() {
        return reference;
    }

    public Injector<T> getInjector() {
        return injector; 
    }

    @Override
    public String getInstanceClassName() {
        final Object value = reference != null ? reference.getInstance() : null;
        return value != null ? value.getClass().getName() : ContextListManagedReferenceFactory.DEFAULT_INSTANCE_CLASS_NAME;
    }

    @Override
    public String getJndiViewInstanceValue() {
        final Object value = reference != null ? reference.getInstance() : null;
        return String.valueOf(value);
    }    
}
