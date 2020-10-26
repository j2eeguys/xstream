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

package com.thoughtworks.xstream.converters.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Vector;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;


/**
 * Converts most common Collections (Lists and Sets), specifying a nested element for each item.
 * <p>
 * Supports {@link ArrayList}, {@link HashSet}, {@link LinkedList}, {@link Vector} and {@link LinkedHashSet}.
 * </p>
 * 
 * @author Joe Walnes
 * @see com.thoughtworks.xstream.converters.extended.NamedCollectionConverter
 */
public class CollectionConverter extends AbstractCollectionConverter {

    private final Class<? extends Collection<?>> type;

    public CollectionConverter(final Mapper mapper) {
        this(mapper, null);
    }

    /**
     * Construct a CollectionConverter for a special Collection type.
     * 
     * @param mapper the mapper
     * @param type the Collection type to handle
     * @since 1.4.5
     */
    public CollectionConverter(final Mapper mapper, @SuppressWarnings("rawtypes") final Class<? extends Collection> type) {
        super(mapper);
        @SuppressWarnings("unchecked")
        final Class<? extends Collection<?>> checkedType = (Class<? extends Collection<?>>)type;
        this.type = checkedType;
        if (type != null && !Collection.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type + " not of type " + Collection.class);
        }
    }

    @Override
    public boolean canConvert(final Class<?> type) {
        if (this.type != null) {
            return type.equals(this.type);
        }
        return type.equals(ArrayList.class)
            || type.equals(HashSet.class)
            || type.equals(LinkedList.class)
            || type.equals(Vector.class)
            || type.equals(LinkedHashSet.class);
    }

    @Override
    public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final Collection<?> collection = (Collection<?>)source;
        for (final Object item : collection) {
            writeCompleteItem(item, context, writer);
        }
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final Class<?> collectionType = context.getRequiredType();
        final Collection<?> collection = createCollection(collectionType);
        populateCollection(reader, context, collection);
        return collection;
    }

    protected void populateCollection(final HierarchicalStreamReader reader, final UnmarshallingContext context,
            final Collection<?> collection) {
        populateCollection(reader, context, collection, collection);
    }

    protected void populateCollection(final HierarchicalStreamReader reader, final UnmarshallingContext context,
            final Collection<?> collection, final Collection<?> target) {
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            addCurrentElementToCollection(reader, context, collection, target);
            reader.moveUp();
        }
    }

    protected void addCurrentElementToCollection(final HierarchicalStreamReader reader,
            final UnmarshallingContext context, final Collection<?> collection, final Collection<?> target) {
        @SuppressWarnings("deprecation")
        final Object item = readItem(reader, context, collection); // call readBareItem when deprecated method is removed
        @SuppressWarnings("unchecked")
        final Collection<Object> targetCollection = (Collection<Object>)target;
        targetCollection.add(item);
    }

    @Override
    protected Collection<?> createCollection(final Class<?> type) {
        return (Collection<?>)super.createCollection(this.type != null ? this.type : type);
    }
}
