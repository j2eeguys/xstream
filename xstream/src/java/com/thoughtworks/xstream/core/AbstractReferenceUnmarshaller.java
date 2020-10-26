/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.thoughtworks.xstream.core;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;


/**
 * Abstract base class for a TreeUnmarshaller, that resolves references.
 *
 * @author Joe Walnes
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 * @since 1.2
 */
public abstract class AbstractReferenceUnmarshaller<R> extends TreeUnmarshaller {

    private static final Object NULL = new Object();
    private final Map<R, Object> values = new HashMap<>();
    private final FastStack<R> parentStack = new FastStack<>(16);

    public AbstractReferenceUnmarshaller(
            final Object root, final HierarchicalStreamReader reader, final ConverterLookup converterLookup,
            final Mapper mapper) {
        super(root, reader, converterLookup, mapper);
    }

    @Override
    protected Object convert(final Object parent, final Class<?> type, final Converter converter) {
        if (parentStack.size() > 0) { // handles circular references
            final R parentReferenceKey = parentStack.peek();
            if (parentReferenceKey != null) {
                // see AbstractCircularReferenceTest.testWeirdCircularReference()
                if (!values.containsKey(parentReferenceKey)) {
                    values.put(parentReferenceKey, parent);
                }
            }
        }
        final Object result;
        final String attributeName = getMapper().aliasForSystemAttribute("reference");
        final String reference = attributeName == null ? null : reader.getAttribute(attributeName);
        final boolean isReferenceable = getMapper().isReferenceable(type);
        if (reference != null) {
            final Object cache = isReferenceable ? values.get(getReferenceKey(reference)) : null;
            if (cache == null) {
                final ConversionException ex = new ConversionException("Invalid reference");
                ex.add("reference", reference);
                ex.add("referenced-type", type.getName());
                ex.add("referenceable", Boolean.toString(isReferenceable));
                throw ex;
            }
            result = cache == NULL ? null : cache;
        } else if (!isReferenceable) {
            result = super.convert(parent, type, converter);
        } else {
            final R currentReferenceKey = getCurrentReferenceKey();
            parentStack.push(currentReferenceKey);
            Object localResult = null;
            try {
                localResult = super.convert(parent, type, converter);
            } finally {
                result = localResult;
                if (currentReferenceKey != null) {
                    values.put(currentReferenceKey, result == null ? NULL : result);
                }
                parentStack.popSilently();
            }
        }
        return result;
    }

    protected abstract R getReferenceKey(String reference);

    protected abstract R getCurrentReferenceKey();
}
