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

package com.thoughtworks.xstream.converters.basic;

/**
 * Converts an int primitive or {@link Integer} wrapper to a string.
 * 
 * @author Joe Walnes
 */
public class IntConverter extends AbstractSingleValueConverter {

    @Override
    public boolean canConvert(final Class<?> type) {
        return type == int.class || type == Integer.class;
    }

    @Override
    public Object fromString(final String str) {
        final long value = Long.decode(str).longValue();
        if (value < Integer.MIN_VALUE || value > 0xFFFFFFFFl) {
            throw new NumberFormatException("For input string: \"" + str + '"');
        }
        return Integer.valueOf((int)value);
    }

}
