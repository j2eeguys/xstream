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

package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;


public class StandardStaxReaderTest extends AbstractStaxReaderTest {

    @Override
    protected StaxDriver createDriver(final QNameMap qnameMap) {
        return new StandardStaxDriver(qnameMap);
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
