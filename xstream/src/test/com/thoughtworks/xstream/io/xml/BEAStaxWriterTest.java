/*
 * Copyright (C) 2007, 2008, 2009, 2011, 2018, 2019 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 03. November 2007 by Joerg Schaible
 */
package com.thoughtworks.xstream.io.xml;

import java.util.Arrays;


public final class BEAStaxWriterTest extends AbstractStaxWriterTest {
    @Override
    protected void assertXmlProducedIs(String expected) {
        expected = expected.replaceAll(" xmlns=\"\"", "");
        expected = expected.replaceAll("<(\\w+)([^>]*)/>", "<$1$2></$1>");
        expected = expected.replace("&#xd;", "&#13;");
        expected = expected.replace("&#xa;", "&#10;");
        expected = expected.replace("&#x9;", "&#9;");
        expected = getXMLHeader() + expected;
        assertEquals(expected, buffer.toString());
    }

    @Override
    protected String getXMLHeader() {
        return "<?xml version='1.0' encoding='utf-8'?>";
    }

    @Override
    protected StaxDriver getStaxDriver() {
        return new BEAStaxDriver();
    }

    @Override
    protected void marshalRepairing(final QNameMap qnameMap, final String expected) {
        // repairing mode fails for BEA's reference implementation in these cases
        if (!(Arrays
            .asList("testNamespacedXmlWithPrefixTwice", "testNamespacedXmlWithSameAlias")
            .contains(getName()))) {
            super.marshalRepairing(qnameMap, expected);
        }
    }
}
