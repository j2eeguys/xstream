/*
 * Copyright (C) 2007, 2008, 2009, 2011, 2018 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 30. September 2011 by Joerg Schaible, renamed from SjsxpStaxWriterTest
 */
package com.thoughtworks.xstream.io.xml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;


public final class SjsxpWriterTest extends AbstractStaxWriterTest {
    final static String className = "com.sun.xml.internal.stream.XMLOutputFactoryImpl";

    public static Test suite() {
        try {
            Class.forName(className);
            return new TestSuite(SjsxpWriterTest.class);
        } catch (final ClassNotFoundException e) {
            return new TestCase(SjsxpWriterTest.class.getName() + ": not available") {

                @Override
                public int countTestCases() {
                    return 1;
                }

                @Override
                public void run(final TestResult result) {
                }
            };
        }
    }

    @Override
    protected void assertXmlProducedIs(String expected) {
        if (!staxDriver.isRepairingNamespace()) {
            expected = expected.replaceAll(" xmlns=\"\"", "");
        }
        expected = expected.replaceAll("<(\\w+)([^>]*)/>", "<$1$2></$1>");
        expected = expected.replace("&#xd;", "\r");
        // attributes are not properly escaped
        expected = expected.replace("&#xa;", "\n");
        expected = expected.replace("&#x9;", "\t");
        expected = getXMLHeader() + expected;
        assertEquals(expected, buffer.toString());
    }

    @Override
    protected String getXMLHeader() {
        return "<?xml version=\"1.0\" ?>";
    }

    @Override
    protected StaxDriver getStaxDriver() {
        return new SjsxpDriver();
    }
}
