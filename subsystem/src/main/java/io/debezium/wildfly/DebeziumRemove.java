/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.wildfly;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

class DebeziumRemove extends AbstractRemoveStepHandler {
    public static DebeziumRemove INSTANCE = new DebeziumRemove();

    @Override
    protected void performRuntime(OperationContext context,
            final ModelNode operation, final ModelNode model)
            throws OperationFailedException {
        context.removeService(ServiceNames.THREAD_POOL_SERVICE);
    }
}
