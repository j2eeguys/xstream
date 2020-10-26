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

package com.thoughtworks.acceptance.annotations;

import com.thoughtworks.acceptance.AbstractAcceptanceTest;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;


/**
 * Test for annotations mapping implicit arrays.
 * 
 * @author J&ouml;rg Schaible
 */
public class ImplicitArrayTest extends AbstractAcceptanceTest {

    @Override
    protected XStream createXStream() {
        XStream xstream = super.createXStream();
        xstream.autodetectAnnotations(true);
        return xstream;
    }

    public void testAnnotation() {
        String expected = ""
            + "<root>\n"
            + "  <string>one</string>\n"
            + "  <string>two</string>\n"
            + "</root>";
        ImplicitRootOne implicitRoot = new ImplicitRootOne();
        implicitRoot.values = new String[]{
            "one", "two"
        };
        assertBothWays(implicitRoot, expected);
    }

    public void testAnnotationWithItemFieldName() {
        String expected = ""
            + "<root>\n"
            + "  <value>one</value>\n"
            + "  <value>two</value>\n"
            + "</root>";
        ImplicitRootTwo implicitRoot = new ImplicitRootTwo();
        implicitRoot.values = new String[]{
            "one", "two"
        };
        assertBothWays(implicitRoot, expected);
    }

    @XStreamAlias("root")
    public static class ImplicitRootOne {
        @XStreamImplicit()
        String[] values;
    }

    @XStreamAlias("root")
    public static class ImplicitRootTwo {
        @XStreamImplicit(itemFieldName = "value")
        String[] values;
    }

    @XStreamAlias("component")
    public static class ParameterizedComponentType {
        @XStreamImplicit(itemFieldName = "line")
        private Point[][] signatureLines;
    }

    @XStreamAlias("point")
    public static class Point {
        @XStreamAsAttribute
        private int x;
        @XStreamAsAttribute
        private int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public void testAnnotationHandlesParameterizedComponentTypes() {
        String xml = ""
            + "<component>\n"
            + "  <line>\n"
            + "    <point x=\"33\" y=\"11\"/>\n"
            + "  </line>\n"
            + "</component>";
        ParameterizedComponentType root = new ParameterizedComponentType();
        root.signatureLines = new Point[][] {
            new Point[] { new Point(33, 11) }
        };
        assertBothWays(root, xml);
    }
    
    @XStreamAlias("point3d")
    public static class Point3D extends Point {
        @XStreamAsAttribute
    	private int z;

		public Point3D(int x, int y, int z) {
    		super(x,y);
			this.z = z;
    	}
    }
    
    @XStreamAlias("root")
    public static class VariantArray {
        @XStreamImplicit
    	private Point[] points;
    }
    
    public void testCanHandleVariantArrays()
	{
		VariantArray array = new VariantArray();
		array.points = new Point[] {
			new Point(1, 2), 
			new Point3D(3, 4, 5), 
			new Point(6, 7)
		};
        String xml = "" //
            + "<root>\n" // 
            + "  <point x=\"1\" y=\"2\"/>\n"
            + "  <point3d x=\"3\" y=\"4\" z=\"5\"/>\n"
            + "  <point x=\"6\" y=\"7\"/>\n"
            + "</root>";
        assertBothWays(array, xml);
	}

    @XStreamAlias("primitives")
    public static class PrimitiveArrays {
        @XStreamImplicit()
        private char[] chars;
    }

    public void testWorksForTypesThatArePrimitiveArrays() {
        PrimitiveArrays type = new PrimitiveArrays();
        type.chars = new char[]{'f', 'o', 'o'};
        String xml = "" //
            + "<primitives>\n" // 
            + "  <char>f</char>\n" //
            + "  <char>o</char>\n" //
            + "  <char>o</char>\n" //
            + "</primitives>";
        assertBothWays(type, xml);
    }
}
