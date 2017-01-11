/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.debezium.wildfly;

import java.util.HashMap;
import java.util.Map;

enum Namespace {
    // must be first
    UNKNOWN(null),
    DEBEZIUM_1_0("urn:jboss:domain:debezium:1.0"); //$NON-NLS-1$

    /**
     * The current namespace version.
     */
    public static final Namespace CURRENT = DEBEZIUM_1_0;

    private final String uri;

    Namespace(String uri) {
        this.uri = uri;
    }

    /**
     * Get the URI of this namespace.
     *
     * @return the URI
     */
    public String getUri() {
        return uri;
    }

    private static final Map<String, Namespace> namespaces;

    static {
        final Map<String, Namespace> map = new HashMap<String, Namespace>();
        for (Namespace namespace : values()) {
            final String name = namespace.getUri();
            if (name != null) map.put(name, namespace);
        }
        namespaces = map;
    }

    /**
     * Converts the specified uri to a {@link Namespace}.
     * @param uri a namespace uri
     * @return the matching namespace enum.
     */
    public static Namespace forUri(String uri) {
        final Namespace element = namespaces.get(uri);
        return element == null ? UNKNOWN : element;
    }
}
