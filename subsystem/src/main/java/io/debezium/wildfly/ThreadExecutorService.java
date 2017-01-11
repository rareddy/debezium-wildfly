/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.wildfly;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

class ThreadExecutorService implements Service<Executor> {

    private int threadCount;
    private ExecutorService threadExecutor;
    
    public ThreadExecutorService(int threadCount) {
        this.threadCount = threadCount;
    }
    
    @Override
    public Executor getValue() throws IllegalStateException,
            IllegalArgumentException {
        return this.threadExecutor;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.threadExecutor = Executors.newFixedThreadPool(threadCount);
    }

    @Override
    public void stop(StopContext context) {
        this.threadExecutor.shutdown();
    }
}
