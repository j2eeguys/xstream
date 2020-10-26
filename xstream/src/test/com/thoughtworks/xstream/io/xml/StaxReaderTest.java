/*
 * Copyright (C) 2004 Joe Walnes.
 * Copyright (C) 2006, 2007, 2015, 2016, 2019 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 29. September 2004 by James Strachan
 */
package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;


public class StaxReaderTest extends AbstractStaxReaderTest {
    @Override
    protected StaxDriver createDriver(final QNameMap qnameMap) {
        return new StaxDriver(qnameMap);
    }

    @Override
    protected HierarchicalStreamReader createReader(final String xml) throws Exception {
        final String prefix = getName().endsWith("ISOControlCharactersInValue") ? XML_1_1_HEADER : "";
        return super.createReader(prefix + xml);
    }

    @Override
    protected String getSpecialCharsInJavaNamesForXml10() {
        return super.getSpecialCharsInJavaNamesForXml10_4th();
    }

    @Override
    public void testIsXXEVulnerableWithExternalGeneralEntity() throws Exception {
        try {
            super.testIsXXEVulnerableWithExternalGeneralEntity();
        } catch (final XStreamException e) {
            final String message = e.getCause().getMessage();
            if (!message.contains("external entity")) {
                throw e;
            }
        }
    }

    @Override
    public void testIsXXEVulnerableWithExternalParameterEntity() throws Exception {
        try {
            super.testIsXXEVulnerableWithExternalParameterEntity();
            fail("Thrown " + XStreamException.class.getName() + " expected");
        } catch (final XStreamException e) {
            final String message = e.getCause().getMessage();
            if (!message.contains("external entity")) {
                if (message.contains("com.wutka.dtd.DTDParseException")) {
                    System.err.println("BEAStaxReader was selected as default StAX driver for StaxReaderTest!");
                } else {
                    throw e;
                }
            }
        }
    }

    @Override
    public void testNullCharacterInValue() throws Exception {
        // not possible, null value is invalid in XML
    }

    @Override
    public void testNonUnicodeCharacterInValue() throws Exception {
        // not possible, character is invalid in XML
    }

    @Override
    public void testNonUnicodeCharacterInCDATA() throws Exception {
        // not possible, character is invalid in XML
    }

    // inherits tests from superclass
}
