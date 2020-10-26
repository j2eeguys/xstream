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

package com.thoughtworks.xstream.core;

import com.thoughtworks.acceptance.AbstractAcceptanceTest;
import com.thoughtworks.xstream.core.Base64Codec;
import com.thoughtworks.xstream.core.StringCodec;


public class Base64CodecTest extends AbstractAcceptanceTest {

    private StringCodec encoder = new Base64Codec();

    public void testEncodesEntireByteArrayAsString() {
        final byte input[] = "hello world".getBytes();
        final String expected = "aGVsbG8gd29ybGQ=";
        assertEquals(expected, encoder.encode(input));
        assertByteArrayEquals(input, encoder.decode(expected));
    }

    public void testModeWithoutLineWraps() {
        final byte input[] =
                "hello world. hello world. hello world. hello world. hello world. hello world. hello world. "
                    .getBytes();
        final String expected = "aGVsbG8gd29ybGQuIGhlbGxvIHdvcmxkLiBoZWxsbyB3b3JsZC4gaGVsbG8gd29ybGQuIGhlbGxv"
            + "IHdvcmxkLiBoZWxsbyB3b3JsZC4gaGVsbG8gd29ybGQuIA==";
        assertEquals(expected, encoder.encode(input));
        assertByteArrayEquals(input, encoder.decode(expected));
    }

    public void testDecodesLinesWithLF() {
        final byte data[] =
                "hello world. hello world. hello world. hello world. hello world. hello world. hello world. "
                    .getBytes();
        final String input = "aGVsbG8gd29ybGQuIGhlbGxvIHdvcmxkLiBoZWxsbyB3b3JsZC4gaGVsbG8gd29ybGQuIGhlbGxv\n"
            + "IHdvcmxkLiBoZWxsbyB3b3JsZC4gaGVsbG8gd29ybGQuIA==";
        assertByteArrayEquals(data, encoder.decode(input));
    }

    public void testDecodesLinesWithCRLF() {
        final byte data[] =
                "hello world. hello world. hello world. hello world. hello world. hello world. hello world. "
                    .getBytes();
        final String input = "aGVsbG8gd29ybGQuIGhlbGxvIHdvcmxkLiBoZWxsbyB3b3JsZC4gaGVsbG8gd29ybGQuIGhlbGxv\r\n"
            + "IHdvcmxkLiBoZWxsbyB3b3JsZC4gaGVsbG8gd29ybGQuIA==";
        assertByteArrayEquals(data, encoder.decode(input));
    }

    public void testDecodesShortLines() {
        final byte data[] = "hello world".getBytes();
        final String input = "aGVs\nbG8g\nd29y\nbGQ=";
        assertByteArrayEquals(data, encoder.decode(input));
    }

    public void testPadsSingleMissingByteWhenNotMultipleOfThree() {
        final byte input[] = {1, 2, 3, 4, 5};
        final String expected = "AQIDBAU=";
        assertEquals(expected, encoder.encode(input));
        assertByteArrayEquals(input, encoder.decode(expected));
    }

    public void testPadsDoubleMissingByteWhenNotMultipleOfThree() {
        final byte input[] = {1, 2, 3, 4};
        final String expected = "AQIDBA==";
        assertEquals(expected, encoder.encode(input));
        assertByteArrayEquals(input, encoder.decode(expected));
    }

    public void testDoesNotPadWhenMultipleOfThree() {
        final byte input[] = {1, 2, 3, 4, 5, 6};
        final String expected = "AQIDBAUG";
        assertEquals(expected, encoder.encode(input));
        assertByteArrayEquals(input, encoder.decode(expected));
    }

    public void testHandlesAllPositiveBytes() {
        final byte input[] = new byte[127];
        for (int i = 0; i < 126; i++) {
            input[i] = (byte)(i + 1);
        }
        final String expected = "AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyAhIiMkJSYnKCkqKywtLi8wMTIzNDU2Nzg5"
            + "Ojs8PT4/QEFCQ0RFRkdISUpLTE1OT1BRUlNUVVZXWFlaW1xdXl9gYWJjZGVmZ2hpamtsbW5vcHFy"
            + "c3R1dnd4eXp7fH1+AA==";
        assertEquals(expected, encoder.encode(input));
        assertByteArrayEquals(input, encoder.decode(expected));
    }

    public void testHandlesAllNegativeBytes() {
        final byte input[] = new byte[128];
        for (int i = 0; i < 127; i++) {
            input[i] = (byte)(-1 - i);
        }
        final String expected = "//79/Pv6+fj39vX08/Lx8O/u7ezr6uno5+bl5OPi4eDf3t3c29rZ2NfW1dTT0tHQz87NzMvKycjH"
            + "xsXEw8LBwL++vby7urm4t7a1tLOysbCvrq2sq6qpqKempaSjoqGgn56dnJuamZiXlpWUk5KRkI+O"
            + "jYyLiomIh4aFhIOCgQA=";
        assertEquals(expected, encoder.encode(input));
        assertByteArrayEquals(input, encoder.decode(expected));
    }

    public void testHandlesZeroByte() {
        final byte input[] = {0, 0, 0, 0};
        final String expected = "AAAAAA==";
        assertEquals(expected, encoder.encode(input));
        assertByteArrayEquals(input, encoder.decode(expected));
    }

    public void testProducesEmptyStringWhenNoBytesGiven() {
        final byte input[] = new byte[0];
        final String expected = "";
        assertEquals(expected, encoder.encode(input));
        assertByteArrayEquals(input, encoder.decode(expected));
    }
}
