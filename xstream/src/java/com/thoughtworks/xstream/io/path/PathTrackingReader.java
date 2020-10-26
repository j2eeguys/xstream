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

package com.thoughtworks.xstream.io.path;

import com.thoughtworks.xstream.converters.ErrorWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.ReaderWrapper;


/**
 * Wrapper for HierarchicalStreamReader that tracks the path (a subset of XPath) of the current node that is being read.
 * 
 * @see PathTracker
 * @see Path
 * @author Joe Walnes
 */
public class PathTrackingReader extends ReaderWrapper {

    private final PathTracker pathTracker;

    public PathTrackingReader(final HierarchicalStreamReader reader, final PathTracker pathTracker) {
        super(reader);
        this.pathTracker = pathTracker;
        pathTracker.pushElement(getNodeName());
    }

    @Override
    public void moveDown() {
        super.moveDown();
        pathTracker.pushElement(getNodeName());
    }

    @Override
    public void moveUp() {
        super.moveUp();
        pathTracker.popElement();
    }

    @Override
    public void appendErrors(final ErrorWriter errorWriter) {
        errorWriter.add("path", pathTracker.getPath().toString());
        super.appendErrors(errorWriter);
    }

}
