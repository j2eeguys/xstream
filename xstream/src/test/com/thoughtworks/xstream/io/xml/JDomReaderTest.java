/*
 * Copyright (C) 2004 Joe Walnes.
 * Copyright (C) 2006, 2007, 2015, 2016, 2017, 2018, 2019 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 09. September 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.io.xml;

import java.io.StringReader;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;


public class JDomReaderTest extends AbstractXMLReaderTest {

    // factory method
    @Override
    protected HierarchicalStreamReader createReader(final String xml) throws Exception {
        return new JDomDriver().createReader(new StringReader(xml));
    }

    @Override
    protected String getSpecialCharsInJavaNamesForXml10() {
        return super.getSpecialCharsInJavaNamesForXml10_4th();
    }

    @Override
    protected String getSpecialCharsInJavaNamesForXml11() {
        return super.getSpecialCharsInJavaNamesForXml10_4th();
    }

    public void testCanReadFromElementOfLargerDocument() throws Exception {
        final String xml = ""
            + "<big>"
            + "  <small>"
            + "    <tiny/>"
            + "  </small>"
            + "  <small-two>"
            + "  </small-two>"
            + "</big>";
        final Document document = new SAXBuilder().build(new StringReader(xml));
        final Element element = document.getRootElement().getChild("small");

        try (final HierarchicalStreamReader xmlReader = new JDomReader(element)) {
            assertEquals("small", xmlReader.getNodeName());
            xmlReader.moveDown();
            assertEquals("tiny", xmlReader.getNodeName());
        }
    }

    @Override
    public void testIsXXEVulnerableWithExternalGeneralEntity() throws Exception {
        try {
            super.testIsXXEVulnerableWithExternalGeneralEntity();
            fail("Thrown " + XStreamException.class.getName() + " expected");
        } catch (final XStreamException e) {
            final String message = e.getCause().getMessage();
            if (!message.contains("DOCTYPE")) {
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
            if (!message.contains("DOCTYPE")) {
                throw e;
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

    @Override
    public void testISOControlCharactersInValue() throws Exception {
        // not possible, although specified for XML 1.1
    }

    // inherits tests from superclass
}
