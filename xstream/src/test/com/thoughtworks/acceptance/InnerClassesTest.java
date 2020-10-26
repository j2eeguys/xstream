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

package com.thoughtworks.acceptance;

public class InnerClassesTest extends AbstractAcceptanceTest {

    public void testSerializedInnerClassMaintainsReferenceToOuterClass() {
        xstream.allowTypes(Outer.class, Outer.Inner.class);

        final Outer outer = new Outer("THE-OUTER-NAME", "THE-INNER-NAME");
        final Outer.Inner inner = outer.getInner();

        assertEquals("Hello from THE-INNER-NAME (inside THE-OUTER-NAME)", inner.getMessage());

        final String xml = xstream.toXML(inner);

        final String expectedXml = ""
            + "<com.thoughtworks.acceptance.Outer_-Inner>\n"
            + "  <innerName>THE-INNER-NAME</innerName>\n"
            + "  <outer-class>\n"
            + "    <inner reference=\"../..\"/>\n"
            + "    <outerName>THE-OUTER-NAME</outerName>\n"
            + "  </outer-class>\n"
            + "</com.thoughtworks.acceptance.Outer_-Inner>";
        assertEquals(expectedXml, xml);

        final Outer.Inner newInner = (Outer.Inner)xstream.fromXML(xml);

        assertEquals("Hello from THE-INNER-NAME (inside THE-OUTER-NAME)", newInner.getMessage());
    }

    @SuppressWarnings("unused")
    public static class OuterType {
        private final String outerName = "Outer Name";
        public InnerType inner = new InnerType();
        private final InnerType.Dynamic1 dyn1 = inner.new Dynamic1();
        private final InnerType.Dynamic1.Dynamic2 dyn2 = dyn1.new Dynamic2();
        private final InnerType.Dynamic3 dyn3 = inner.new Dynamic3(dyn1);

        public class InnerType {
            private final String innerName = "Inner Name";

            public class Dynamic1 {
                private final String name1 = "Name 1";

                public class Dynamic2 {
                    private final String name2 = "Name 2";
                }
            }

            public class Dynamic3 extends Dynamic1.Dynamic2 {
                private final String name3 = "Name 3";
                private final Dynamic1.Dynamic2 dyn4;

                public Dynamic3(final Dynamic1 outer) {
                    outer.super();
                    class Dynamic4 extends Dynamic1.Dynamic2 {
                        private final String name4 = "Name 4";
                        private final Dynamic5 dyn5 = new Dynamic5();

                        class Dynamic5 {
                            private final String name5 = "Name 5";
                        }

                        Dynamic4(final Dynamic1 outer) {
                            outer.super();
                        }
                    }
                    dyn4 = new Dynamic4(outer);
                }
            }
        }
    }

    public void testNestedDynamicTypes() {
        xstream.alias("inner", OuterType.InnerType.class);

        final OuterType outer = new OuterType();

        final String expectedXml = ""
            + "<inner>\n"
            + "  <innerName>Inner Name</innerName>\n"
            + "  <outer-class>\n"
            + "    <outerName>Outer Name</outerName>\n"
            + "    <inner reference=\"../..\"/>\n"
            + "    <dyn1>\n"
            + "      <name1>Name 1</name1>\n"
            + "      <outer-class reference=\"../../..\"/>\n"
            + "    </dyn1>\n"
            + "    <dyn2>\n"
            + "      <name2>Name 2</name2>\n"
            + "      <outer-class reference=\"../../dyn1\"/>\n"
            + "    </dyn2>\n"
            + "    <dyn3>\n"
            + "      <name2>Name 2</name2>\n"
            + "      <outer-class reference=\"../../dyn1\"/>\n"
            + "      <name3>Name 3</name3>\n"
            + "      <dyn4 class=\"com.thoughtworks.acceptance.InnerClassesTest$OuterType$InnerType$Dynamic3$1Dynamic4\">\n"
            + "        <name2>Name 2</name2>\n"
            + "        <outer-class defined-in=\"com.thoughtworks.acceptance.InnerClassesTest$OuterType$InnerType$Dynamic1$Dynamic2\" reference=\"../../../dyn1\"/>\n"
            + "        <name4>Name 4</name4>\n"
            + "        <dyn5>\n"
            + "          <name5>Name 5</name5>\n"
            + "          <outer-class reference=\"../..\"/>\n"
            + "        </dyn5>\n"
            + "        <outer-class reference=\"../..\"/>\n"
            + "      </dyn4>\n"
            + "      <outer-class-1 reference=\"../../..\"/>\n"
            + "    </dyn3>\n"
            + "  </outer-class>\n"
            + "</inner>";

        assertBothWays(outer.inner, expectedXml);
    }
}

class Outer {

    private final Inner inner;
    private final String outerName;

    public Outer(final String outerName, final String innerName) {
        inner = new Inner(innerName);
        this.outerName = outerName;
    }

    public Inner getInner() {
        return inner;
    }

    public class Inner {
        private final String innerName;

        public Inner(final String innerName) {
            this.innerName = innerName;
        }

        public String getMessage() {
            return "Hello from " + innerName + " (inside " + outerName + ")";
        }
    }
}
