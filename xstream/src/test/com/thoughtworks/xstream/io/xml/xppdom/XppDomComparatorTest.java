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

package com.thoughtworks.xstream.io.xml.xppdom;

import java.util.Comparator;

import junit.framework.TestCase;


/**
 * Tests {@link XppDomComparator}.
 *
 * @author J&ouml;rg Schaible
 */
public class XppDomComparatorTest extends TestCase {
    // ~ Instance fields --------------------------------------------------------

    private ThreadLocal<String> xpath;
    private XppDomComparator comparator;

    // ~ Methods ----------------------------------------------------------------

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        xpath = new ThreadLocal<>();
        comparator = new XppDomComparator(xpath);
    }

    private void assertEquals(final Comparator<XppDom> comparator, final XppDom o1, final XppDom o2) {
        if (comparator.compare(o1, o2) != 0) {
            fail("Cpmarator claims '" + o1 + "' to be different from '" + o2 + "'");
        }
    }

    /**
     * Tests comparison of empty document.
     *
     * @throws Exception unexpected
     */
    public void testEqualsEmptyDocuments() throws Exception {
        final String xml = "<dom/>";
        final XppDom dom1 = XppFactory.buildDom(xml);
        final XppDom dom2 = XppFactory.buildDom(xml);
        assertEquals(comparator, dom1, dom2);
        assertNull(xpath.get());
    }

    /**
     * Tests comparison of different values.
     *
     * @throws Exception unexpected
     */
    public void testSortsElementsWithDifferentValue() throws Exception {
        final XppDom dom1 = XppFactory.buildDom("<dom>value1</dom>");
        final XppDom dom2 = XppFactory.buildDom("<dom>value2</dom>");
        assertEquals(-1, comparator.compare(dom1, dom2));
        assertEquals("/dom::text()", xpath.get());
        assertEquals(1, comparator.compare(dom2, dom1));
        assertEquals("/dom::text()", xpath.get());
    }

    /**
     * Tests comparison of a value and null.
     *
     * @throws Exception unexpected
     */
    public void testSortsElementsWithValueAndNull() throws Exception {
        final XppDom dom1 = XppFactory.buildDom("<dom/>");
        final XppDom dom2 = XppFactory.buildDom("<dom>value</dom>");
        assertEquals(-1, comparator.compare(dom1, dom2));
        assertEquals("/dom::text()", xpath.get());
        assertEquals(1, comparator.compare(dom2, dom1));
        assertEquals("/dom::text()", xpath.get());
    }

    /**
     * Tests comparison of attributes.
     *
     * @throws Exception unexpected
     */
    public void testEqualsAttributes() throws Exception {
        final String xml = "<dom a='1' b='2'/>";
        final XppDom dom1 = XppFactory.buildDom(xml);
        final XppDom dom2 = XppFactory.buildDom(xml);
        assertEquals(comparator, dom1, dom2);
        assertNull(xpath.get());
    }

    /**
     * Tests comparison of attributes in different order.
     *
     * @throws Exception unexpected
     */
    public void testEqualsAttributesInDifferentOrder() throws Exception {
        final XppDom dom1 = XppFactory.buildDom("<dom a='1' b='2'/>");
        final XppDom dom2 = XppFactory.buildDom("<dom b='2' a='1'/>");
        assertEquals(comparator, dom1, dom2);
        assertNull(xpath.get());
    }

    /**
     * Tests comparison of same attributes with different values.
     *
     * @throws Exception unexpected
     */
    public void testSortsSameAttributes() throws Exception {
        final XppDom dom1 = XppFactory.buildDom("<dom a='1' b='2'/>");
        final XppDom dom2 = XppFactory.buildDom("<dom a='2' b='1'/>");
        assertEquals(-1, comparator.compare(dom1, dom2));
        assertEquals("/dom[@a]", xpath.get());
        assertEquals(1, comparator.compare(dom2, dom1));
        assertEquals("/dom[@a]", xpath.get());
    }

    /**
     * Tests comparison of different attributes.
     *
     * @throws Exception unexpected
     */
    public void testSortsDifferentAttributes() throws Exception {
        final XppDom dom1 = XppFactory.buildDom("<dom a='1'/>");
        final XppDom dom2 = XppFactory.buildDom("<dom b='1'/>");
        assertEquals(-1, comparator.compare(dom1, dom2));
        assertEquals("/dom[@a?]", xpath.get());
        assertEquals(1, comparator.compare(dom2, dom1));
        assertEquals("/dom[@b?]", xpath.get());
    }

    /**
     * Tests comparison of different number of attributes.
     *
     * @throws Exception unexpected
     */
    public void testSortsAccordingNumberOfAttributes() throws Exception {
        final XppDom dom1 = XppFactory.buildDom("<dom/>");
        final XppDom dom2 = XppFactory.buildDom("<dom a='1'/>");
        assertEquals(-1, comparator.compare(dom1, dom2));
        assertEquals("/dom::count(@*)", xpath.get());
        assertEquals(1, comparator.compare(dom2, dom1));
        assertEquals("/dom::count(@*)", xpath.get());
    }

    /**
     * Tests comparison of document with children.
     *
     * @throws Exception unexpected
     */
    public void testEqualsDocumentsWithChildren() throws Exception {
        final String xml = "<dom><a/></dom>";
        final XppDom dom1 = XppFactory.buildDom(xml);
        final XppDom dom2 = XppFactory.buildDom(xml);
        assertEquals(comparator, dom1, dom2);
        assertNull(xpath.get());
    }

    /**
     * Tests comparison of different number of children.
     *
     * @throws Exception unexpected
     */
    public void testSortsAccordingNumberOfChildren() throws Exception {
        final XppDom dom1 = XppFactory.buildDom("<dom/>");
        final XppDom dom2 = XppFactory.buildDom("<dom><a/></dom>");
        assertEquals(-1, comparator.compare(dom1, dom2));
        assertEquals("/dom::count(*)", xpath.get());
        assertEquals(1, comparator.compare(dom2, dom1));
        assertEquals("/dom::count(*)", xpath.get());
    }

    /**
     * Tests comparison of different elements.
     *
     * @throws Exception unexpected
     */
    public void testSortsElementsByName() throws Exception {
        final XppDom dom1 = XppFactory.buildDom("<dom><a/></dom>");
        final XppDom dom2 = XppFactory.buildDom("<dom><b/></dom>");
        assertEquals(-1, comparator.compare(dom1, dom2));
        assertEquals("/dom/a[0]?", xpath.get());
        assertEquals(1, comparator.compare(dom2, dom1));
        assertEquals("/dom/b[0]?", xpath.get());
    }

    /**
     * Tests comparison of different nth elements.
     *
     * @throws Exception unexpected
     */
    public void testSortsElementsByNthName() throws Exception {
        final XppDom dom1 = XppFactory.buildDom("<dom><a/><b/><c/><a/></dom>");
        final XppDom dom2 = XppFactory.buildDom("<dom><a/><b/><c/><b/></dom>");
        assertEquals(-1, comparator.compare(dom1, dom2));
        assertEquals("/dom/a[1]?", xpath.get());
        assertEquals(1, comparator.compare(dom2, dom1));
        assertEquals("/dom/b[1]?", xpath.get());
    }

    /**
     * Tests comparison sorts attributes before elements.
     *
     * @throws Exception unexpected
     */
    public void testSortsAttributesBeforeElements() throws Exception {
        final XppDom dom1 = XppFactory.buildDom("<dom x='a'><a/></dom>");
        final XppDom dom2 = XppFactory.buildDom("<dom x='b'><b/></dom>");
        assertEquals(-1, comparator.compare(dom1, dom2));
        assertEquals("/dom[@x]", xpath.get());
        assertEquals(1, comparator.compare(dom2, dom1));
        assertEquals("/dom[@x]", xpath.get());
    }

    /**
     * Tests comparison will reset XPath after recursion.
     *
     * @throws Exception unexpected
     */
    public void testWillResetXPathAfterRecursion() throws Exception {
        final XppDom dom1 = XppFactory.buildDom("<dom><a><b>foo</b></a><c x='1'/></dom>");
        final XppDom dom2 = XppFactory.buildDom("<dom><a><b>foo</b></a><c x='2'/></dom>");
        assertEquals(-1, comparator.compare(dom1, dom2));
        assertEquals("/dom/c[0][@x]", xpath.get());
        assertEquals(1, comparator.compare(dom2, dom1));
        assertEquals("/dom/c[0][@x]", xpath.get());
    }

    /**
     * Tests comparison of empty document.
     *
     * @throws Exception unexpected
     */
    public void testCompareWithoutReference() throws Exception {
        comparator = new XppDomComparator();
        final String xml = "<dom/>";
        final XppDom dom1 = XppFactory.buildDom(xml);
        final XppDom dom2 = XppFactory.buildDom(xml);
        assertEquals(comparator, dom1, dom2);
        assertNull(xpath.get());
    }
}
