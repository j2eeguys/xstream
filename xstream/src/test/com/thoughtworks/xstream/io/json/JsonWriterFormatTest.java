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

package com.thoughtworks.xstream.io.json;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import com.thoughtworks.acceptance.objects.OpenSourceSoftware;
import com.thoughtworks.acceptance.objects.SampleLists;
import com.thoughtworks.acceptance.someobjects.Handler;
import com.thoughtworks.acceptance.someobjects.Protocol;
import com.thoughtworks.acceptance.someobjects.X;
import com.thoughtworks.acceptance.someobjects.Y;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.extended.ToStringConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonWriter.Format;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the {@link JsonWriter} formats.
 *
 * @author J&ouml;rg Schaible
 */
public class JsonWriterFormatTest extends TestCase {

    private final XStream xstream;
    private final Object target;
    private final int mode;
    private final Format format;
    private final String json;

    public static class YString extends Y {
        private static final long serialVersionUID = 201101L;

        public YString(final String y) {
            yField = y;
        }

        @Override
        public String toString() {
            return yField;
        }
    }

    private final static class HandlerConverter implements Converter {
        @Override
        public boolean canConvert(final Class<?> type) {
            return type == Handler.class;
        }

        @Override
        public void marshal(final Object source, final HierarchicalStreamWriter writer,
                final MarshallingContext context) {
            final Handler h = (Handler)source;
            writer.startNode("str");
            writer.setValue("test");
            writer.endNode();
            writer.startNode("protocol");
            context.convertAnother(h.getProtocol());
            writer.endNode();
            final HierarchicalStreamWriter writer1 = writer;
            writer1.startNode("i", int.class);
            writer.setValue("42");
            writer.endNode();
        }

        @Override
        public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
            reader.moveDown();
            reader.moveUp();
            reader.moveDown();
            final Protocol p = (Protocol)context.convertAnother(null, Protocol.class);
            reader.moveUp();
            reader.moveDown();
            reader.moveUp();
            return new Handler(p);
        }
    }

    public JsonWriterFormatTest(
            final String name, final Object target, final String json, final int writerMode,
            final JsonWriter.Format format) {
        super(name);
        this.target = target;
        this.json = json;
        mode = writerMode;
        this.format = format;

        xstream = new XStream();
        xstream.setMode(name.endsWith("+ID") ? XStream.ID_REFERENCES : XStream.NO_REFERENCES);
        xstream.alias("chseq", CharSequence.class);
        xstream.alias("oss", OpenSourceSoftware.class);
        xstream.alias("collections", SampleLists.class);
        xstream.alias("x", X.class);
        xstream.alias("ys", YString.class);
        xstream.alias("h", Handler.class);
        xstream.useAttributeFor(OpenSourceSoftware.class, "license");
        try {
            xstream.registerConverter(new ToStringConverter(YString.class));
        } catch (final NoSuchMethodException e) {
            throw new AssertionFailedError(e.getMessage());
        }
        xstream.registerConverter(new HandlerConverter());
    }

    @Override
    protected void runTest() throws Throwable {
        assertEquals(json, toJSON(mode, format));
    }

    private String toJSON(final int mode, final JsonWriter.Format format) {
        final StringWriter writer = new StringWriter(1024);
        try {
            writeJSON(writer, mode, format);
            return writer.toString();
        } finally {
            // System.out.println(writer.toString() + " ---> " + getName());
        }
    }

    private void writeJSON(final Writer writer, final int mode, final JsonWriter.Format format) {
        @SuppressWarnings("resource")
        final JsonWriter jsonWriter = new JsonWriter(writer, mode, format, 0);
        try {
            xstream.marshal(target, jsonWriter);
        } finally {
            jsonWriter.flush();
        }
    }

    public static Test suite() {
        final Map<String, Integer> modes = new LinkedHashMap<String, Integer>();
        modes.put("optimized", new Integer(0));
        modes.put("noRoot", new Integer(AbstractJsonWriter.DROP_ROOT_MODE));
        modes.put("explicit", new Integer(AbstractJsonWriter.EXPLICIT_MODE));

        final Map<String, JsonWriter.Format> formats = new LinkedHashMap<String, JsonWriter.Format>();
        formats.put("Minimal", new JsonWriter.Format(new char[0], new char[0],
            JsonWriter.Format.COMPACT_EMPTY_ELEMENT));
        formats.put("Pretty", new JsonWriter.Format("  ".toCharArray(), "\n".toCharArray(),
            JsonWriter.Format.SPACE_AFTER_LABEL));
        formats.put("Compact", new JsonWriter.Format("  ".toCharArray(), "\n".toCharArray(),
            JsonWriter.Format.SPACE_AFTER_LABEL | JsonWriter.Format.COMPACT_EMPTY_ELEMENT));

        final Properties properties = new Properties();
        properties.put("one", "1");
        final X x = new X();
        x.anInt = 42;
        x.aStr = "X";
        x.innerObj = new YString("Y");
        final X emptyX = new X();
        emptyX.innerObj = new Y();
        final SampleLists<String, X> lists = new SampleLists<String, X>();
        lists.good = new LinkedList<String>();
        lists.good.add("XStream");
        lists.bad = new TreeSet<X>();
        lists.bad.add(new X());
        final Map<String, Object> targets = new LinkedHashMap<String, Object>();
        targets.put("String", "text");
        targets.put("CharSequenceArray", new CharSequence[]{"text", new StringBuffer("buffer"), null});
        targets.put("CharSequenceArray+ID", new CharSequence[]{"text", new StringBuffer("buffer"), null});
        targets.put("EmptyStringArray", new String[][]{new String[0]});
        targets.put("EmptyStringArray+ID", new String[][]{new String[0]});
        targets.put("Properties", properties);
        targets.put("Object", new OpenSourceSoftware("Codehaus", "XStream", "BSD"));
        targets.put("AttributeOnly", new OpenSourceSoftware(null, null, "BSD"));
        targets.put("X", x);
        targets.put("EmptyX", emptyX);
        targets.put("Collections", lists);
        targets.put("EmptyList", new ArrayList<Object>());
        targets.put("CustomConverter", new Handler(new Protocol("ldap")));

        final Map<String, String> results = new HashMap<String, String>();
        results.put("optimizedMinimalString", "{'string':'text'}");
        results.put("optimizedPrettyString", "{'string': 'text'}");
        results.put("optimizedCompactString", "{'string': 'text'}");
        results.put("noRootMinimalString", "'text'");
        results.put("noRootPrettyString", "'text'");
        results.put("noRootCompactString", "'text'");
        results.put("explicitMinimalString", "{'string':[[],['text']]}");
        results.put("explicitPrettyString", "{'string': [\n  [\n  ],\n  [\n    'text'\n  ]\n]}");
        results.put("explicitCompactString", "{'string': [\n  [],\n  [\n    'text'\n  ]\n]}");
        results.put("optimizedMinimalCharSequenceArray", "{'chseq-array':['text','buffer',null]}");
        results.put("optimizedPrettyCharSequenceArray", "{'chseq-array': [\n  'text',\n  'buffer',\n  null\n]}");
        results.put("optimizedCompactCharSequenceArray", "{'chseq-array': [\n  'text',\n  'buffer',\n  null\n]}");
        results.put("noRootMinimalCharSequenceArray", "['text','buffer',null]");
        results.put("noRootPrettyCharSequenceArray", "[\n  'text',\n  'buffer',\n  null\n]");
        results.put("noRootCompactCharSequenceArray", "[\n  'text',\n  'buffer',\n  null\n]");
        results.put("explicitMinimalCharSequenceArray",
            "{'chseq-array':[[],[{'string':[[],['text']]},{'string-buffer':[[],['buffer']]},{'null':[[],[null]]}]]}");
        results.put("explicitPrettyCharSequenceArray",
            "{'chseq-array': [\n  [\n  ],\n  [\n    {\n      'string': [\n        [\n        ],\n        [\n          'text'\n        ]\n      ]\n    },\n    {\n      'string-buffer': [\n        [\n        ],\n        [\n          'buffer'\n        ]\n      ]\n    },\n    {\n      'null': [\n        [\n        ],\n        [\n          null\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("explicitCompactCharSequenceArray",
            "{'chseq-array': [\n  [],\n  [\n    {\n      'string': [\n        [],\n        [\n          'text'\n        ]\n      ]\n    },\n    {\n      'string-buffer': [\n        [],\n        [\n          'buffer'\n        ]\n      ]\n    },\n    {\n      'null': [\n        [],\n        [\n          null\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("optimizedMinimalCharSequenceArray+ID", "{'chseq-array':['text',{'@id':'2','$':'buffer'},null]}");
        results.put("optimizedPrettyCharSequenceArray+ID",
            "{'chseq-array': [\n  'text',\n  {\n    '@id': '2',\n    '$': 'buffer'\n  },\n  null\n]}");
        results.put("optimizedCompactCharSequenceArray+ID",
            "{'chseq-array': [\n  'text',\n  {\n    '@id': '2',\n    '$': 'buffer'\n  },\n  null\n]}");
        results.put("noRootMinimalCharSequenceArray+ID", "['text',{'@id':'2','$':'buffer'},null]");
        results.put("noRootPrettyCharSequenceArray+ID",
            "[\n  'text',\n  {\n    '@id': '2',\n    '$': 'buffer'\n  },\n  null\n]");
        results.put("noRootCompactCharSequenceArray+ID",
            "[\n  'text',\n  {\n    '@id': '2',\n    '$': 'buffer'\n  },\n  null\n]");
        results.put("explicitMinimalCharSequenceArray+ID",
            "{'chseq-array':[[{'id':'1'}],[{'string':[[],['text']]},{'string-buffer':[[{'id':'2'}],['buffer']]},{'null':[[],[null]]}]]}");
        results.put("explicitPrettyCharSequenceArray+ID",
            "{'chseq-array': [\n  [\n    {\n      'id': '1'\n    }\n  ],\n  [\n    {\n      'string': [\n        [\n        ],\n        [\n          'text'\n        ]\n      ]\n    },\n    {\n      'string-buffer': [\n        [\n          {\n            'id': '2'\n          }\n        ],\n        [\n          'buffer'\n        ]\n      ]\n    },\n    {\n      'null': [\n        [\n        ],\n        [\n          null\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("explicitCompactCharSequenceArray+ID",
            "{'chseq-array': [\n  [\n    {\n      'id': '1'\n    }\n  ],\n  [\n    {\n      'string': [\n        [],\n        [\n          'text'\n        ]\n      ]\n    },\n    {\n      'string-buffer': [\n        [\n          {\n            'id': '2'\n          }\n        ],\n        [\n          'buffer'\n        ]\n      ]\n    },\n    {\n      'null': [\n        [],\n        [\n          null\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("optimizedMinimalEmptyStringArray", "{'string-array-array':[[]]}");
        results.put("optimizedPrettyEmptyStringArray", "{'string-array-array': [\n  [\n  ]\n]}");
        results.put("optimizedCompactEmptyStringArray", "{'string-array-array': [\n  []\n]}");
        results.put("noRootMinimalEmptyStringArray", "[[]]");
        results.put("noRootPrettyEmptyStringArray", "[\n  [\n  ]\n]");
        results.put("noRootCompactEmptyStringArray", "[\n  []\n]");
        results.put("explicitMinimalEmptyStringArray", "{'string-array-array':[[],[{'string-array':[[],[]]}]]}");
        results.put("explicitPrettyEmptyStringArray",
            "{'string-array-array': [\n  [\n  ],\n  [\n    {\n      'string-array': [\n        [\n        ],\n        [\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("explicitCompactEmptyStringArray",
            "{'string-array-array': [\n  [],\n  [\n    {\n      'string-array': [\n        [],\n        []\n      ]\n    }\n  ]\n]}");
        results.put("optimizedMinimalEmptyStringArray+ID", "{'string-array-array':[[]]}");
        results.put("optimizedPrettyEmptyStringArray+ID", "{'string-array-array': [\n  [\n  ]\n]}");
        results.put("optimizedCompactEmptyStringArray+ID", "{'string-array-array': [\n  []\n]}");
        results.put("noRootMinimalEmptyStringArray+ID", "[[]]");
        results.put("noRootPrettyEmptyStringArray+ID", "[\n  [\n  ]\n]");
        results.put("noRootCompactEmptyStringArray+ID", "[\n  []\n]");
        results.put("explicitMinimalEmptyStringArray+ID",
            "{'string-array-array':[[{'id':'1'}],[{'string-array':[[{'id':'2'}],[]]}]]}");
        results.put("explicitPrettyEmptyStringArray+ID",
            "{'string-array-array': [\n  [\n    {\n      'id': '1'\n    }\n  ],\n  [\n    {\n      'string-array': [\n        [\n          {\n            'id': '2'\n          }\n        ],\n        [\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("explicitCompactEmptyStringArray+ID",
            "{'string-array-array': [\n  [\n    {\n      'id': '1'\n    }\n  ],\n  [\n    {\n      'string-array': [\n        [\n          {\n            'id': '2'\n          }\n        ],\n        []\n      ]\n    }\n  ]\n]}");
        results.put("optimizedMinimalProperties", "{'properties':[{'@name':'one','@value':'1'}]}");
        results.put("optimizedPrettyProperties",
            "{'properties': [\n  {\n    '@name': 'one',\n    '@value': '1'\n  }\n]}");
        results.put("optimizedCompactProperties",
            "{'properties': [\n  {\n    '@name': 'one',\n    '@value': '1'\n  }\n]}");
        results.put("noRootMinimalProperties", "[{'@name':'one','@value':'1'}]");
        results.put("noRootPrettyProperties", "[\n  {\n    '@name': 'one',\n    '@value': '1'\n  }\n]");
        results.put("noRootCompactProperties", "[\n  {\n    '@name': 'one',\n    '@value': '1'\n  }\n]");
        results.put("explicitMinimalProperties",
            "{'properties':[[],[{'property':[[{'name':'one','value':'1'}],[]]}]]}");
        results.put("explicitPrettyProperties",
            "{'properties': [\n  [\n  ],\n  [\n    {\n      'property': [\n        [\n          {\n            'name': 'one',\n            'value': '1'\n          }\n        ],\n        [\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("explicitCompactProperties",
            "{'properties': [\n  [],\n  [\n    {\n      'property': [\n        [\n          {\n            'name': 'one',\n            'value': '1'\n          }\n        ],\n        []\n      ]\n    }\n  ]\n]}");
        results.put("optimizedMinimalObject", "{'oss':{'@license':'BSD','vendor':'Codehaus','name':'XStream'}}");
        results.put("optimizedPrettyObject",
            "{'oss': {\n  '@license': 'BSD',\n  'vendor': 'Codehaus',\n  'name': 'XStream'\n}}");
        results.put("optimizedCompactObject",
            "{'oss': {\n  '@license': 'BSD',\n  'vendor': 'Codehaus',\n  'name': 'XStream'\n}}");
        results.put("noRootMinimalObject", "{'@license':'BSD','vendor':'Codehaus','name':'XStream'}");
        results.put("noRootPrettyObject", "{\n  '@license': 'BSD',\n  'vendor': 'Codehaus',\n  'name': 'XStream'\n}");
        results.put("noRootCompactObject", "{\n  '@license': 'BSD',\n  'vendor': 'Codehaus',\n  'name': 'XStream'\n}");
        results.put("explicitMinimalObject",
            "{'oss':[[{'license':'BSD'}],[{'vendor':[[],['Codehaus']]},{'name':[[],['XStream']]}]]}");
        results.put("explicitPrettyObject",
            "{'oss': [\n  [\n    {\n      'license': 'BSD'\n    }\n  ],\n  [\n    {\n      'vendor': [\n        [\n        ],\n        [\n          'Codehaus'\n        ]\n      ]\n    },\n    {\n      'name': [\n        [\n        ],\n        [\n          'XStream'\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("explicitCompactObject",
            "{'oss': [\n  [\n    {\n      'license': 'BSD'\n    }\n  ],\n  [\n    {\n      'vendor': [\n        [],\n        [\n          'Codehaus'\n        ]\n      ]\n    },\n    {\n      'name': [\n        [],\n        [\n          'XStream'\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("optimizedMinimalAttributeOnly", "{'oss':{'@license':'BSD'}}");
        results.put("optimizedPrettyAttributeOnly", "{'oss': {\n  '@license': 'BSD'\n}}");
        results.put("optimizedCompactAttributeOnly", "{'oss': {\n  '@license': 'BSD'\n}}");
        results.put("noRootMinimalAttributeOnly", "{'@license':'BSD'}");
        results.put("noRootPrettyAttributeOnly", "{\n  '@license': 'BSD'\n}");
        results.put("noRootCompactAttributeOnly", "{\n  '@license': 'BSD'\n}");
        results.put("explicitMinimalAttributeOnly", "{'oss':[[{'license':'BSD'}],[]]}");
        results.put("explicitPrettyAttributeOnly",
            "{'oss': [\n  [\n    {\n      'license': 'BSD'\n    }\n  ],\n  [\n  ]\n]}");
        results.put("explicitCompactAttributeOnly",
            "{'oss': [\n  [\n    {\n      'license': 'BSD'\n    }\n  ],\n  []\n]}");
        results.put("optimizedMinimalX", "{'x':{'aStr':'X','anInt':42,'innerObj':{'@class':'ys','$':'Y'}}}");
        results.put("optimizedPrettyX",
            "{'x': {\n  'aStr': 'X',\n  'anInt': 42,\n  'innerObj': {\n    '@class': 'ys',\n    '$': 'Y'\n  }\n}}");
        results.put("optimizedCompactX",
            "{'x': {\n  'aStr': 'X',\n  'anInt': 42,\n  'innerObj': {\n    '@class': 'ys',\n    '$': 'Y'\n  }\n}}");
        results.put("noRootMinimalX", "{'aStr':'X','anInt':42,'innerObj':{'@class':'ys','$':'Y'}}");
        results.put("noRootPrettyX",
            "{\n  'aStr': 'X',\n  'anInt': 42,\n  'innerObj': {\n    '@class': 'ys',\n    '$': 'Y'\n  }\n}");
        results.put("noRootCompactX",
            "{\n  'aStr': 'X',\n  'anInt': 42,\n  'innerObj': {\n    '@class': 'ys',\n    '$': 'Y'\n  }\n}");
        results.put("explicitMinimalX",
            "{'x':[[],[{'aStr':[[],['X']]},{'anInt':[[],[42]]},{'innerObj':[[{'class':'ys'}],['Y']]}]]}");
        results.put("explicitPrettyX",
            "{'x': [\n  [\n  ],\n  [\n    {\n      'aStr': [\n        [\n        ],\n        [\n          'X'\n        ]\n      ]\n    },\n    {\n      'anInt': [\n        [\n        ],\n        [\n          42\n        ]\n      ]\n    },\n    {\n      'innerObj': [\n        [\n          {\n            'class': 'ys'\n          }\n        ],\n        [\n          'Y'\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("explicitCompactX",
            "{'x': [\n  [],\n  [\n    {\n      'aStr': [\n        [],\n        [\n          'X'\n        ]\n      ]\n    },\n    {\n      'anInt': [\n        [],\n        [\n          42\n        ]\n      ]\n    },\n    {\n      'innerObj': [\n        [\n          {\n            'class': 'ys'\n          }\n        ],\n        [\n          'Y'\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("optimizedMinimalEmptyX", "{'x':{'anInt':0,'innerObj':{}}}");
        results.put("optimizedPrettyEmptyX", "{'x': {\n  'anInt': 0,\n  'innerObj': {\n  }\n}}");
        results.put("optimizedCompactEmptyX", "{'x': {\n  'anInt': 0,\n  'innerObj': {}\n}}");
        results.put("noRootMinimalEmptyX", "{'anInt':0,'innerObj':{}}");
        results.put("noRootPrettyEmptyX", "{\n  'anInt': 0,\n  'innerObj': {\n  }\n}");
        results.put("noRootCompactEmptyX", "{\n  'anInt': 0,\n  'innerObj': {}\n}");
        results.put("explicitMinimalEmptyX", "{'x':[[],[{'anInt':[[],[0]]},{'innerObj':[[],[]]}]]}");
        results.put("explicitPrettyEmptyX",
            "{'x': [\n  [\n  ],\n  [\n    {\n      'anInt': [\n        [\n        ],\n        [\n          0\n        ]\n      ]\n    },\n    {\n      'innerObj': [\n        [\n        ],\n        [\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("explicitCompactEmptyX",
            "{'x': [\n  [],\n  [\n    {\n      'anInt': [\n        [],\n        [\n          0\n        ]\n      ]\n    },\n    {\n      'innerObj': [\n        [],\n        []\n      ]\n    }\n  ]\n]}");
        results.put("optimizedMinimalCollections", "{'collections':{'good':['XStream'],'bad':[{'anInt':0}]}}");
        results.put("optimizedPrettyCollections",
            "{'collections': {\n  'good': [\n    'XStream'\n  ],\n  'bad': [\n    {\n      'anInt': 0\n    }\n  ]\n}}");
        results.put("optimizedCompactCollections",
            "{'collections': {\n  'good': [\n    'XStream'\n  ],\n  'bad': [\n    {\n      'anInt': 0\n    }\n  ]\n}}");
        results.put("noRootMinimalCollections", "{'good':['XStream'],'bad':[{'anInt':0}]}");
        results.put("noRootPrettyCollections",
            "{\n  'good': [\n    'XStream'\n  ],\n  'bad': [\n    {\n      'anInt': 0\n    }\n  ]\n}");
        results.put("noRootCompactCollections",
            "{\n  'good': [\n    'XStream'\n  ],\n  'bad': [\n    {\n      'anInt': 0\n    }\n  ]\n}");
        results.put("explicitMinimalCollections",
            "{'collections':[[],[{'good':[[{'class':'linked-list'}],[{'string':[[],['XStream']]}]]},{'bad':[[{'class':'sorted-set'}],[{'x':[[],[{'anInt':[[],[0]]}]]}]]}]]}");
        results.put("explicitPrettyCollections",
            "{'collections': [\n  [\n  ],\n  [\n    {\n      'good': [\n        [\n          {\n            'class': 'linked-list'\n          }\n        ],\n        [\n          {\n            'string': [\n              [\n              ],\n              [\n                'XStream'\n              ]\n            ]\n          }\n        ]\n      ]\n    },\n    {\n      'bad': [\n        [\n          {\n            'class': 'sorted-set'\n          }\n        ],\n        [\n          {\n            'x': [\n              [\n              ],\n              [\n                {\n                  'anInt': [\n                    [\n                    ],\n                    [\n                      0\n                    ]\n                  ]\n                }\n              ]\n            ]\n          }\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("explicitCompactCollections",
            "{'collections': [\n  [],\n  [\n    {\n      'good': [\n        [\n          {\n            'class': 'linked-list'\n          }\n        ],\n        [\n          {\n            'string': [\n              [],\n              [\n                'XStream'\n              ]\n            ]\n          }\n        ]\n      ]\n    },\n    {\n      'bad': [\n        [\n          {\n            'class': 'sorted-set'\n          }\n        ],\n        [\n          {\n            'x': [\n              [],\n              [\n                {\n                  'anInt': [\n                    [],\n                    [\n                      0\n                    ]\n                  ]\n                }\n              ]\n            ]\n          }\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("optimizedMinimalEmptyList", "{'list':[]}");
        results.put("optimizedPrettyEmptyList", "{'list': [\n]}");
        results.put("optimizedCompactEmptyList", "{'list': []}");
        results.put("noRootMinimalEmptyList", "[]");
        results.put("noRootPrettyEmptyList", "[\n]");
        results.put("noRootCompactEmptyList", "[]");
        results.put("explicitMinimalEmptyList", "{'list':[[],[]]}");
        results.put("explicitPrettyEmptyList", "{'list': [\n  [\n  ],\n  [\n  ]\n]}");
        results.put("explicitCompactEmptyList", "{'list': [\n  [],\n  []\n]}");
        results.put("optimizedMinimalCustomConverter", "{'h':{'str':'test','protocol':{'id':'ldap'},'i':42}}");
        results.put("optimizedPrettyCustomConverter",
            "{'h': {\n  'str': 'test',\n  'protocol': {\n    'id': 'ldap'\n  },\n  'i': 42\n}}");
        results.put("optimizedCompactCustomConverter",
            "{'h': {\n  'str': 'test',\n  'protocol': {\n    'id': 'ldap'\n  },\n  'i': 42\n}}");
        results.put("noRootMinimalCustomConverter", "{'str':'test','protocol':{'id':'ldap'},'i':42}");
        results.put("noRootPrettyCustomConverter",
            "{\n  'str': 'test',\n  'protocol': {\n    'id': 'ldap'\n  },\n  'i': 42\n}");
        results.put("noRootCompactCustomConverter",
            "{\n  'str': 'test',\n  'protocol': {\n    'id': 'ldap'\n  },\n  'i': 42\n}");
        results.put("explicitMinimalCustomConverter",
            "{'h':[[],[{'str':[[],['test']]},{'protocol':[[],[{'id':[[],['ldap']]}]]},{'i':[[],[42]]}]]}");
        results.put("explicitPrettyCustomConverter",
            "{'h': [\n  [\n  ],\n  [\n    {\n      'str': [\n        [\n        ],\n        [\n          'test'\n        ]\n      ]\n    },\n    {\n      'protocol': [\n        [\n        ],\n        [\n          {\n            'id': [\n              [\n              ],\n              [\n                'ldap'\n              ]\n            ]\n          }\n        ]\n      ]\n    },\n    {\n      'i': [\n        [\n        ],\n        [\n          42\n        ]\n      ]\n    }\n  ]\n]}");
        results.put("explicitCompactCustomConverter",
            "{'h': [\n  [],\n  [\n    {\n      'str': [\n        [],\n        [\n          'test'\n        ]\n      ]\n    },\n    {\n      'protocol': [\n        [],\n        [\n          {\n            'id': [\n              [],\n              [\n                'ldap'\n              ]\n            ]\n          }\n        ]\n      ]\n    },\n    {\n      'i': [\n        [],\n        [\n          42\n        ]\n      ]\n    }\n  ]\n]}");

        final TestSuite suite = new TestSuite(JsonWriterFormatTest.class.getName());
        for (final Map.Entry<String, Integer> entryMode : modes.entrySet()) {
            final String modeName = entryMode.getKey();
            final int mode = entryMode.getValue().intValue();
            for (final Map.Entry<String, JsonWriter.Format> entryFormat : formats.entrySet()) {
                final String formatName = entryFormat.getKey();
                final JsonWriter.Format format = entryFormat.getValue();
                for (final Map.Entry<String, Object> entryTarget : targets.entrySet()) {
                    final String targetName = entryTarget.getKey();
                    final Object target = entryTarget.getValue();
                    final String name = modeName + formatName + targetName;
                    final String result = results.get(name).replace('\'', '"');

                    suite.addTest(new JsonWriterFormatTest(name, target, result, mode, format));
                }
            }
        }

        return suite;
    }
}
