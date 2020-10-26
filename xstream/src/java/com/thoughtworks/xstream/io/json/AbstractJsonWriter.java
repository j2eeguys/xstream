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

import java.io.Externalizable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.io.AbstractWriter;
import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.mapper.Mapper;


/**
 * An abstract implementation of a writer that calls abstract methods to build JSON structures. Note, that XStream's
 * implicit collection feature is only compatible with the syntax in {@link #EXPLICIT_MODE}.
 *
 * @author J&ouml;rg Schaible
 * @since 1.4
 */
public abstract class AbstractJsonWriter extends AbstractWriter {
    /**
     * DROP_ROOT_MODE drops the JSON root node.
     * <p>
     * The root node is the first level of the JSON object i.e.
     * </p>
     *
     * <pre>
     * { &quot;person&quot;: {
     *     &quot;name&quot;: &quot;Joe&quot;
     * }}
     * </pre>
     * <p>
     * will be written without root simply as
     * </p>
     *
     * <pre>
     * {
     *     &quot;name&quot;: &quot;Joe&quot;
     * }
     * </pre>
     * <p>
     * Without a root node, the top level element might now also be an array. However, it is possible to generate
     * invalid JSON unless {@link #STRICT_MODE} is also set.
     * </p>
     *
     * @since 1.3.1
     */
    public static final int DROP_ROOT_MODE = 1;
    /**
     * STRICT_MODE prevents invalid JSON for single value objects when dropping the root.
     * <p>
     * The mode is only useful in combination with the {@link #DROP_ROOT_MODE}. An object with a single value as first
     * node i.e.
     * </p>
     *
     * <pre>
     * { &quot;name&quot;: &quot;Joe&quot; }
     * </pre>
     * <p>
     * is simply written as
     * </p>
     *
     * <pre>
     * &quot;Joe&quot;
     * </pre>
     * <p>
     * However, this is no longer valid JSON. Therefore you can activate {@link #STRICT_MODE} and a
     * {@link ConversionException} is thrown instead.
     * </p>
     *
     * @since 1.3.1
     */
    public static final int STRICT_MODE = 2;
    /**
     * EXPLICIT_MODE assures that all data has its explicit equivalent in the resulting JSON.
     * <p>
     * XStream is normally using attributes in XML that have no real equivalent in JSON. Additionally it is essential in
     * XML that the individual child elements of a tag keep order and may have the same tag name. XStream's model relies
     * on both characteristics. However, properties of a JSON object do not have a defined order, but their names have
     * to be unique. Only a JSON array defines the order of its elements.
     * </p>
     * <p>
     * Therefore XStream uses in explicit mode a JSON format that supports the original requirements at the expense of
     * the simplicity of the JSON objects and arrays. Each Java object will be represented by a JSON object with a
     * single property representing the name of the object and an array as value that contains two more arrays. The
     * first one contains a JSON object with all attributes, the second one the value of the Java object which can be
     * null, a string or integer value or again a new JSON object representing a Java object. Here an example of an
     * string array with one member, where the array and the string has an additional attribute 'id':
     * </p>
     *
     * <pre>
     * {&quot;string-array&quot;:[[{&quot;id&quot;:&quot;1&quot;}],[{&quot;string&quot;:[[{&quot;id&quot;:&quot;2&quot;}],[&quot;Joe&quot;]]}]]}
     * </pre>
     * <p>
     * This format can be used to always deserialize into Java again.
     * </p>
     * <p>
     * This mode cannot combined with {@link #STRICT_MODE} or {@link #DROP_ROOT_MODE}.
     * </p>
     *
     * @since 1.4
     */
    public static final int EXPLICIT_MODE = 4;
    /**
     * IEEE_754_MODE keeps precision of 64-bit integer values.
     * <p>
     * In JavaScript every number is expressed as 64-bit double value with a precision of 53 bits following IEEE 754.
     * Therefore it is not possible to represent the complete value range of 64-bit integer values. Any integer value
     * &gt; 2<sup>53</sup> (9007199254740992) or &lt; -2<sup>53</sup> (-9007199254740992) will therefore be written as
     * string value.
     * </p>
     * <p>
     * CAUTION: A client must be aware that the element may contain a number or a string value.
     * </p>
     *
     * @since 1.4.5
     * @see <a href="http://ecma262-5.com/ELS5_HTML.htm#Section_8.5">ECMA Specification: The Number Type</a>
     */
    public static final int IEEE_754_MODE = 8;

    public static class Type {
        public static Type NULL = new Type();
        public static Type STRING = new Type();
        public static Type NUMBER = new Type();
        public static Type BOOLEAN = new Type();
    }

    private static class StackElement {
        final Class<?> type;
        int status;

        public StackElement(final Class<?> type, final int status) {
            this.type = type;
            this.status = status;
        }
    }

    private static class IllegalWriterStateException extends IllegalStateException {
        private static final long serialVersionUID = 20151010L;

        public IllegalWriterStateException(final int from, final int to, final String element) {
            super("Cannot turn from state "
                + getState(from)
                + " into state "
                + getState(to)
                + (element == null ? "" : " for property " + element));
        }

        private static String getState(final int state) {
            switch (state) {
            case STATE_ROOT:
                return "ROOT";
            case STATE_END_OBJECT:
                return "END_OBJECT";
            case STATE_START_OBJECT:
                return "START_OBJECT";
            case STATE_START_ATTRIBUTES:
                return "START_ATTRIBUTES";
            case STATE_NEXT_ATTRIBUTE:
                return "NEXT_ATTRIBUTE";
            case STATE_END_ATTRIBUTES:
                return "END_ATTRIBUTES";
            case STATE_START_ELEMENTS:
                return "START_ELEMENTS";
            case STATE_NEXT_ELEMENT:
                return "NEXT_ELEMENT";
            case STATE_END_ELEMENTS:
                return "END_ELEMENTS";
            case STATE_SET_VALUE:
                return "SET_VALUE";
            default:
                throw new IllegalArgumentException("Unknown state provided: "
                    + state
                    + ", cannot create message for IllegalWriterStateException");
            }
        }
    }

    private static final int STATE_ROOT = 1 << 0;
    private static final int STATE_END_OBJECT = 1 << 1;
    private static final int STATE_START_OBJECT = 1 << 2;
    private static final int STATE_START_ATTRIBUTES = 1 << 3;
    private static final int STATE_NEXT_ATTRIBUTE = 1 << 4;
    private static final int STATE_END_ATTRIBUTES = 1 << 5;
    private static final int STATE_START_ELEMENTS = 1 << 6;
    private static final int STATE_NEXT_ELEMENT = 1 << 7;
    private static final int STATE_END_ELEMENTS = 1 << 8;
    private static final int STATE_SET_VALUE = 1 << 9;

    private static final Set<Class<?>> NUMBER_TYPES = new HashSet<>(Arrays.<Class<?>>asList(byte.class, Byte.class,
        short.class, Short.class, int.class, Integer.class, long.class, Long.class, float.class, Float.class,
        double.class, Double.class, BigInteger.class, BigDecimal.class));
    private final int mode;
    private final FastStack<StackElement> stack = new FastStack<>(16);
    private int expectedStates;

    /**
     * Construct a JSON writer.
     *
     * @since 1.4
     */
    public AbstractJsonWriter() {
        this(new NoNameCoder());
    }

    /**
     * Construct a JSON writer with a special mode.
     *
     * @param mode a bit mask of the mode constants
     * @since 1.4
     */
    public AbstractJsonWriter(final int mode) {
        this(mode, new NoNameCoder());
    }

    /**
     * Construct a JSON writer with a special name coder.
     *
     * @param nameCoder the name coder to use
     * @since 1.4
     */
    public AbstractJsonWriter(final NameCoder nameCoder) {
        this(0, nameCoder);
    }

    /**
     * Construct a JSON writer with a special mode and name coder.
     *
     * @param mode a bit mask of the mode constants
     * @param nameCoder the name coder to use
     * @since 1.4
     */
    public AbstractJsonWriter(final int mode, final NameCoder nameCoder) {
        super(nameCoder);
        this.mode = (mode & EXPLICIT_MODE) > 0 ? EXPLICIT_MODE : mode;
        stack.push(new StackElement(null, STATE_ROOT));
        expectedStates = STATE_START_OBJECT;
    }

    @Override
    public void startNode(final String name, final Class<?> clazz) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        stack.push(new StackElement(clazz, stack.peek().status));
        handleCheckedStateTransition(STATE_START_OBJECT, name, null);
        expectedStates = STATE_SET_VALUE | STATE_NEXT_ATTRIBUTE | STATE_START_OBJECT | STATE_NEXT_ELEMENT | STATE_ROOT;
    }

    @Override
    public void startNode(final String name) {
        startNode(name, null);
    }

    @Override
    public void addAttribute(final String name, final String value) {
        handleCheckedStateTransition(STATE_NEXT_ATTRIBUTE, name, value);
        expectedStates = STATE_SET_VALUE | STATE_NEXT_ATTRIBUTE | STATE_START_OBJECT | STATE_NEXT_ELEMENT | STATE_ROOT;
    }

    @Override
    public void setValue(String text) {
        final Class<?> type = stack.peek().type;
        if ((type == Character.class || type == Character.TYPE) && "".equals(text)) {
            text = "\u0000";
        }
        handleCheckedStateTransition(STATE_SET_VALUE, null, text);
        expectedStates = STATE_NEXT_ELEMENT | STATE_ROOT;
    }

    @Override
    public void endNode() {
        final int size = stack.size();
        final int nextState = size > 2 ? STATE_NEXT_ELEMENT : STATE_ROOT;
        handleCheckedStateTransition(nextState, null, null);
        stack.pop();
        stack.peek().status = nextState;
        expectedStates = STATE_START_OBJECT;
        if (size > 2) {
            expectedStates |= STATE_NEXT_ELEMENT | STATE_ROOT;
        }
    }

    private void handleCheckedStateTransition(final int requiredState, final String elementToAdd,
            final String valueToAdd) {
        final StackElement stackElement = stack.peek();
        if ((expectedStates & requiredState) == 0) {
            throw new IllegalWriterStateException(stackElement.status, requiredState, elementToAdd);
        }
        final int currentState = handleStateTransition(stackElement.status, requiredState, elementToAdd, valueToAdd);
        stackElement.status = currentState;
    }

    private int handleStateTransition(int currentState, final int requiredState, final String elementToAdd,
            final String valueToAdd) {
        final int size = stack.size();
        final Class<?> currentType = stack.peek().type;
        final boolean isArray = size > 1 && isArray(currentType);
        final boolean isArrayElement = size > 1 && isArray(stack.get(size - 2).type);
        switch (currentState) {
        case STATE_ROOT:
            if (requiredState == STATE_START_OBJECT) {
                currentState = handleStateTransition(STATE_START_ELEMENTS, STATE_START_OBJECT, elementToAdd, null);
                return requiredState;
            }
            throw new IllegalWriterStateException(currentState, requiredState, elementToAdd);

        case STATE_END_OBJECT:
            switch (requiredState) {
            case STATE_START_OBJECT:
                currentState = handleStateTransition(currentState, STATE_NEXT_ELEMENT, null, null);
                currentState = handleStateTransition(currentState, STATE_START_OBJECT, elementToAdd, null);
                return requiredState;
            case STATE_NEXT_ELEMENT:
                nextElement();
                return requiredState;
            case STATE_ROOT:
                if (((mode & DROP_ROOT_MODE) == 0 || size > 2) && (mode & EXPLICIT_MODE) == 0) {
                    endObject();
                }
                return requiredState;
            default:
                throw new IllegalWriterStateException(currentState, requiredState, elementToAdd);
            }

        case STATE_START_OBJECT:
            switch (requiredState) {
            case STATE_SET_VALUE:
            case STATE_START_OBJECT:
            case STATE_ROOT:
            case STATE_NEXT_ELEMENT:
                if (!isArrayElement || (mode & EXPLICIT_MODE) != 0) {
                    currentState = handleStateTransition(currentState, STATE_START_ATTRIBUTES, null, null);
                    currentState = handleStateTransition(currentState, STATE_END_ATTRIBUTES, null, null);
                }
                currentState = STATE_START_ELEMENTS;

                switch (requiredState) {
                case STATE_SET_VALUE:
                    currentState = handleStateTransition(currentState, STATE_SET_VALUE, null, valueToAdd);
                    break;
                case STATE_START_OBJECT:
                    currentState = handleStateTransition(currentState, STATE_START_OBJECT, elementToAdd, null);
                    break;
                case STATE_ROOT:
                case STATE_NEXT_ELEMENT:
                    currentState = handleStateTransition(currentState, STATE_SET_VALUE, null, null);
                    currentState = handleStateTransition(currentState, requiredState, null, null);
                    break;
                }
                return requiredState;
            case STATE_START_ATTRIBUTES:
                if ((mode & EXPLICIT_MODE) != 0) {
                    startArray();
                }
                return requiredState;
            case STATE_NEXT_ATTRIBUTE:
                if ((mode & EXPLICIT_MODE) != 0 || !isArray) {
                    currentState = handleStateTransition(currentState, STATE_START_ATTRIBUTES, null, null);
                    currentState = handleStateTransition(currentState, STATE_NEXT_ATTRIBUTE, elementToAdd, valueToAdd);
                    return requiredState;
                } else {
                    return STATE_START_OBJECT;
                }
            default:
                throw new IllegalWriterStateException(currentState, requiredState, elementToAdd);
            }

        case STATE_NEXT_ELEMENT:
            switch (requiredState) {
            case STATE_START_OBJECT:
                nextElement();
                if (!isArrayElement && (mode & EXPLICIT_MODE) == 0) {
                    addLabel(encodeNode(elementToAdd));
                    if ((mode & EXPLICIT_MODE) == 0 && isArray) {
                        startArray();
                    }
                    return requiredState;
                }
                break;
            case STATE_ROOT:
                currentState = handleStateTransition(currentState, STATE_END_OBJECT, null, null);
                currentState = handleStateTransition(currentState, STATE_ROOT, null, null);
                return requiredState;
            case STATE_NEXT_ELEMENT:
            case STATE_END_OBJECT:
                currentState = handleStateTransition(currentState, STATE_END_ELEMENTS, null, null);
                currentState = handleStateTransition(currentState, STATE_END_OBJECT, null, null);
                if ((mode & EXPLICIT_MODE) == 0 && !isArray) {
                    endObject();
                }
                return requiredState;
            case STATE_END_ELEMENTS:
                if ((mode & EXPLICIT_MODE) == 0 && isArray) {
                    endArray();
                }
                return requiredState;
            default:
                throw new IllegalWriterStateException(currentState, requiredState, elementToAdd);
            }
            //$FALL-THROUGH$
        case STATE_START_ELEMENTS:
            switch (requiredState) {
            case STATE_START_OBJECT:
                if ((mode & DROP_ROOT_MODE) == 0 || size > 2) {
                    if (!isArrayElement || (mode & EXPLICIT_MODE) != 0) {
                        if (!"".equals(valueToAdd)) {
                            startObject();
                        }
                        addLabel(encodeNode(elementToAdd));
                    }
                    if ((mode & EXPLICIT_MODE) != 0) {
                        startArray();
                    }
                }
                if ((mode & EXPLICIT_MODE) == 0) {
                    if (isArray) {
                        startArray();
                    }
                }
                return requiredState;
            case STATE_SET_VALUE:
                if ((mode & STRICT_MODE) != 0 && size == 2) {
                    throw new ConversionException("Single value cannot be root element");
                }
                if (valueToAdd == null) {
                    if (currentType == Mapper.Null.class) {
                        addValue("null", Type.NULL);
                    } else if ((mode & EXPLICIT_MODE) == 0 && !isArray) {
                        startObject();
                        endObject();
                    }
                } else {
                    if ((mode & IEEE_754_MODE) != 0 && (currentType == long.class || currentType == Long.class)) {
                        final long longValue = Long.parseLong(valueToAdd);
                        // JavaScript supports a maximum of 2^53
                        if (longValue > 9007199254740992L || longValue < -9007199254740992L) {
                            addValue(valueToAdd, Type.STRING);
                        } else {
                            addValue(valueToAdd, getType(currentType));
                        }
                    } else {
                        addValue(valueToAdd, getType(currentType));
                    }
                }
                return requiredState;
            case STATE_END_ELEMENTS:
            case STATE_NEXT_ELEMENT:
                if ((mode & EXPLICIT_MODE) == 0) {
                    if (isArray) {
                        endArray();
                    } else {
                        endObject();
                    }
                }
                return requiredState;
            default:
                throw new IllegalWriterStateException(currentState, requiredState, elementToAdd);
            }

        case STATE_END_ELEMENTS:
            switch (requiredState) {
            case STATE_END_OBJECT:
                if ((mode & EXPLICIT_MODE) != 0) {
                    endArray();
                    endArray();
                    endObject();
                }
                return requiredState;
            default:
                throw new IllegalWriterStateException(currentState, requiredState, elementToAdd);
            }

        case STATE_START_ATTRIBUTES:
            switch (requiredState) {
            case STATE_NEXT_ATTRIBUTE:
                if (elementToAdd != null) {
                    final String name = ((mode & EXPLICIT_MODE) == 0 ? "@" : "") + elementToAdd;
                    startObject();
                    addLabel(encodeAttribute(name));
                    addValue(valueToAdd, Type.STRING);
                }
                return requiredState;
            }
            //$FALL-THROUGH$
        case STATE_NEXT_ATTRIBUTE:
            switch (requiredState) {
            case STATE_END_ATTRIBUTES:
                if ((mode & EXPLICIT_MODE) != 0) {
                    if (currentState == STATE_NEXT_ATTRIBUTE) {
                        endObject();
                    }
                    endArray();
                    nextElement();
                    startArray();
                }
                return requiredState;
            case STATE_NEXT_ATTRIBUTE:
                if (!isArray || (mode & EXPLICIT_MODE) != 0) {
                    nextElement();
                    final String name = ((mode & EXPLICIT_MODE) == 0 ? "@" : "") + elementToAdd;
                    addLabel(encodeAttribute(name));
                    addValue(valueToAdd, Type.STRING);
                }
                return requiredState;
            case STATE_SET_VALUE:
            case STATE_START_OBJECT:
                currentState = handleStateTransition(currentState, STATE_END_ATTRIBUTES, null, null);
                currentState = handleStateTransition(currentState, STATE_START_ELEMENTS, null, null);
                switch (requiredState) {
                case STATE_SET_VALUE:
                    if ((mode & EXPLICIT_MODE) == 0) {
                        addLabel(encodeNode("$"));
                    }
                    currentState = handleStateTransition(currentState, STATE_SET_VALUE, null, valueToAdd);
                    if ((mode & EXPLICIT_MODE) == 0) {
                        endObject();
                    }
                    break;
                case STATE_START_OBJECT:
                    currentState = handleStateTransition(currentState, STATE_START_OBJECT, elementToAdd, (mode
                        & EXPLICIT_MODE) == 0 ? "" : null);
                    break;
                case STATE_END_OBJECT:
                    currentState = handleStateTransition(currentState, STATE_SET_VALUE, null, null);
                    currentState = handleStateTransition(currentState, STATE_END_OBJECT, null, null);
                    break;
                }
                return requiredState;
            case STATE_NEXT_ELEMENT:
                currentState = handleStateTransition(currentState, STATE_END_ATTRIBUTES, null, null);
                currentState = handleStateTransition(currentState, STATE_END_OBJECT, null, null);
                return requiredState;
            case STATE_ROOT:
                currentState = handleStateTransition(currentState, STATE_END_ATTRIBUTES, null, null);
                currentState = handleStateTransition(currentState, STATE_END_OBJECT, null, null);
                currentState = handleStateTransition(currentState, STATE_ROOT, null, null);
                return requiredState;
            default:
                throw new IllegalWriterStateException(currentState, requiredState, elementToAdd);
            }

        case STATE_END_ATTRIBUTES:
            switch (requiredState) {
            case STATE_START_ELEMENTS:
                if ((mode & EXPLICIT_MODE) == 0) {
                    nextElement();
                }
                break;
            case STATE_END_OBJECT:
                currentState = handleStateTransition(STATE_START_ELEMENTS, STATE_END_ELEMENTS, null, null);
                currentState = handleStateTransition(currentState, STATE_END_OBJECT, null, null);
                break;
            default:
                throw new IllegalWriterStateException(currentState, requiredState, elementToAdd);
            }
            return requiredState;

        case STATE_SET_VALUE:
            switch (requiredState) {
            case STATE_END_ELEMENTS:
                if ((mode & EXPLICIT_MODE) == 0 && isArray) {
                    endArray();
                }
                return requiredState;
            case STATE_NEXT_ELEMENT:
                currentState = handleStateTransition(currentState, STATE_END_ELEMENTS, null, null);
                currentState = handleStateTransition(currentState, STATE_END_OBJECT, null, null);
                return requiredState;
            case STATE_ROOT:
                currentState = handleStateTransition(currentState, STATE_END_ELEMENTS, null, null);
                currentState = handleStateTransition(currentState, STATE_END_OBJECT, null, null);
                currentState = handleStateTransition(currentState, STATE_ROOT, null, null);
                return requiredState;
            default:
                throw new IllegalWriterStateException(currentState, requiredState, elementToAdd);
            }
        }

        throw new IllegalWriterStateException(currentState, requiredState, elementToAdd);
    }

    /**
     * Method to return the appropriate JSON type for a Java type.
     *
     * @param clazz the type
     * @return One of the {@link Type} instances
     * @since 1.4.4
     */
    protected Type getType(final Class<?> clazz) {
        return clazz == Mapper.Null.class
            ? Type.NULL
            : clazz == Boolean.class || clazz == Boolean.TYPE
                ? Type.BOOLEAN
                : NUMBER_TYPES.contains(clazz) ? Type.NUMBER : Type.STRING;
    }

    /**
     * Method to declare various Java types to be handles as JSON array.
     *
     * @param clazz the type
     * @return <code>true</code> if handles as array
     * @since 1.4
     */
    protected boolean isArray(final Class<?> clazz) {
        return clazz != null
            && (clazz.isArray()
                || Collection.class.isAssignableFrom(clazz)
                || Externalizable.class.isAssignableFrom(clazz)
                || Map.class.isAssignableFrom(clazz)
                || Map.Entry.class.isAssignableFrom(clazz));
    }

    /**
     * Start a JSON object.
     *
     * @since 1.4
     */
    protected abstract void startObject();

    /**
     * Add a label to a JSON object.
     *
     * @param name the label's name
     * @since 1.4
     */
    protected abstract void addLabel(String name);

    /**
     * Add a value to a JSON object's label or to an array.
     *
     * @param value the value itself
     * @param type the JSON type
     * @since 1.4
     */
    protected abstract void addValue(String value, Type type);

    /**
     * Start a JSON array.
     *
     * @since 1.4
     */
    protected abstract void startArray();

    /**
     * Prepare a JSON object or array for another element.
     *
     * @since 1.4
     */
    protected abstract void nextElement();

    /**
     * End the JSON array.
     *
     * @since 1.4
     */
    protected abstract void endArray();

    /**
     * End the JSON object.
     *
     * @since 1.4
     */
    protected abstract void endObject();
}
