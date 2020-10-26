/*
 * Copyright (C) 2007, 2009, 2011, 2018, 2019 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 03. November 2007 by Joerg Schaible
 */
package com.thoughtworks.xstream.io.xml;

import java.io.StringWriter;

import javax.xml.namespace.QName;

import com.thoughtworks.acceptance.someobjects.X;
import com.thoughtworks.acceptance.someobjects.Y;
import com.thoughtworks.xstream.XStream;


public abstract class AbstractStaxWriterTest extends AbstractXMLWriterTest {

    protected StringWriter buffer;
    protected StaxDriver staxDriver;
    private X testInput;

    protected abstract String getXMLHeader();

    protected abstract StaxDriver getStaxDriver();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        staxDriver = getStaxDriver();
        staxDriver.setRepairingNamespace(false);
        buffer = new StringWriter();
        writer = staxDriver.createWriter(buffer);

        testInput = new X();
        testInput.anInt = 9;
        testInput.aStr = "zzz";
        testInput.innerObj = new Y();
        testInput.innerObj.yField = "ooo";
    }

    public void testNamespacedXmlWithPrefix() throws Exception {
        final QNameMap qnameMap = new QNameMap();
        final QName qname = new QName("http://foo.com", "alias", "foo");
        qnameMap.registerMapping(qname, X.class);

        final String expected = "" //
            + "<foo:alias xmlns:foo=\"http://foo.com\">"
            + /**/ "<aStr xmlns=\"\">zzz</aStr>"
            + /**/ "<anInt xmlns=\"\">9</anInt>"
            + /**/ "<innerObj xmlns=\"\">"
            + /**//**/ "<yField>ooo</yField>"
            + /**/ "</innerObj>"
            + "</foo:alias>";
        marshalWithBothRepairingModes(qnameMap, expected);
    }

    public void testNamespacedXmlWithoutPrefix() throws Exception {
        final QNameMap qnameMap = new QNameMap();
        final QName qname = new QName("http://foo.com", "bar");
        qnameMap.registerMapping(qname, X.class);

        final String expected = "" //
            + "<bar xmlns=\"http://foo.com\">"
            + /**/ "<aStr xmlns=\"\">zzz</aStr>"
            + /**/ "<anInt xmlns=\"\">9</anInt>"
            + /**/ "<innerObj xmlns=\"\">"
            + /**//**/ "<yField>ooo</yField>"
            + /**/ "</innerObj>"
            + "</bar>";
        marshalWithBothRepairingModes(qnameMap, expected);
    }

    public void testNamespacedXmlWithPrefixTwice() throws Exception {
        final QNameMap qnameMap = new QNameMap();
        QName qname = new QName("http://foo.com", "alias", "foo");
        qnameMap.registerMapping(qname, X.class);

        qname = new QName("http://bar.com", "alias1", "bar");
        qnameMap.registerMapping(qname, "aStr");

        qname = new QName("http://bar.com", "alias2", "bar");
        qnameMap.registerMapping(qname, "anInt");

        final String expected = "" //
            + "<foo:alias xmlns:foo=\"http://foo.com\">"
            + /**/ "<bar:alias1 xmlns:bar=\"http://bar.com\">zzz</bar:alias1>"
            + /**/ "<bar:alias2 xmlns:bar=\"http://bar.com\">9</bar:alias2>"
            + /**/ "<innerObj xmlns=\"\">"
            + /**//**/ "<yField>ooo</yField>"
            + /**/ "</innerObj>"
            + "</foo:alias>";
        marshalWithBothRepairingModes(qnameMap, expected);
    }

    public void testNamespacedXmlWithSameAlias() throws Exception {
        final QNameMap qnameMap = new QNameMap();
        qnameMap.setDefaultNamespace("http://foobar.com");

        QName qname = new QName("http://foo.com", "alias", "foo");
        qnameMap.registerMapping(qname, "aStr");

        qname = new QName("http://bar.com", "alias", "bar");
        qnameMap.registerMapping(qname, "anInt");

        final String expected = "" //
            + "<com.thoughtworks.acceptance.someobjects.X xmlns=\"http://foobar.com\">"
            + /**/ "<foo:alias xmlns:foo=\"http://foo.com\">zzz</foo:alias>"
            + /**/ "<bar:alias xmlns:bar=\"http://bar.com\">9</bar:alias>"
            + /**/ "<innerObj>"
            + /**//**/ "<yField>ooo</yField>"
            + /**/ "</innerObj>"
            + "</com.thoughtworks.acceptance.someobjects.X>";
        marshalWithBothRepairingModes(qnameMap, expected);
    }

    protected void marshalWithBothRepairingModes(final QNameMap qnameMap, final String expected) {
        marshalNonRepairing(qnameMap, expected);
        marshalRepairing(qnameMap, expected);
    }

    protected void marshalRepairing(final QNameMap qnameMap, final String expected) {
        marshall(qnameMap, true);
        assertXmlProducedIs(expected);
    }

    protected void marshalNonRepairing(final QNameMap qnameMap, final String expected) {
        marshall(qnameMap, false);
        assertXmlProducedIs(expected);
    }

    protected void marshall(final QNameMap qnameMap, final boolean repairNamespaceMode) {
        staxDriver.setRepairingNamespace(repairNamespaceMode);
        staxDriver.setQnameMap(qnameMap);
        final XStream xstream = new XStream(staxDriver);
        buffer = new StringWriter();
        xstream.toXML(testInput, buffer);
    }

}
