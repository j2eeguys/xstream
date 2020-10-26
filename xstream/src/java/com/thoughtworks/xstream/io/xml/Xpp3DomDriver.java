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

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;

import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.naming.NameCoder;


/**
 * A {@link HierarchicalStreamDriver} for XPP DOM using the Xpp3 parser.
 * 
 * @author J&ouml;rg Schaible
 * @since 1.4
 */
public class Xpp3DomDriver extends AbstractXppDomDriver {

    /**
     * Construct an Xpp3DomDriver.
     * 
     * @since 1.4
     */
    public Xpp3DomDriver() {
        super(new XmlFriendlyNameCoder());
    }

    /**
     * Construct an Xpp3DomDriver.
     * 
     * @param nameCoder the replacer for XML friendly names
     * @since 1.4
     */
    public Xpp3DomDriver(final NameCoder nameCoder) {
        super(nameCoder);
    }

    @Override
    protected XmlPullParser createParser() {
        return new MXParser();
    }
}
