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

package com.thoughtworks.xstream.converters.time;

import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.SingleValueConverter;


/**
 * Converts a {@link OffsetTime} to a string.
 *
 * @author Matej Cimbora
 * @author J&ouml;rg Schaible
 * @since 1.4.10
 */
public class OffsetTimeConverter implements SingleValueConverter {

    private static final DateTimeFormatter FORMATTER;

    static {
        FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .appendOffsetId()
            .toFormatter();
    }

    @Override
    public boolean canConvert(final Class<?> type) {
        return OffsetTime.class == type;
    }

    @Override
    public Object fromString(final String str) {
        try {
            return OffsetTime.parse(str);
        } catch (final DateTimeParseException e) {
            final ConversionException exception = new ConversionException("Cannot parse value as offset time", e);
            exception.add("value", str);
            throw exception;
        }
    }

    @Override
    public String toString(final Object obj) {
        if (obj == null) {
            return null;
        }

        final OffsetTime offsetTime = (OffsetTime)obj;
        return FORMATTER.format(offsetTime);
    }
}
