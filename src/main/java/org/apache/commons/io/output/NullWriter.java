/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.output;

import java.io.Writer;

/**
 * This {@link Writer} writes all data to the famous <b>/dev/null</b>.
 * <p>
 * This <code>Writer</code> has no destination (file/socket etc.) and all
 * characters written to it are ignored and lost.
 * 
 * @version $Id: NullWriter.java 1304052 2012-03-22 20:55:29Z ggregory $
 */
public class NullWriter extends Writer {
    
    /**
     * A singleton.
     */
    public static final NullWriter NULL_WRITER = new NullWriter();

    /**
     * Constructs a new NullWriter.
     */
    public NullWriter() {
    }

    /**
     * Does nothing - output to <code>/dev/null</code>.
     * @param c The character to write
     * @return this writer
     * @since 2.0
     */
    @Override
    public Writer append(char c) {
        //to /dev/null
        return this;
    }

    /**
     * Does nothing - output to <code>/dev/null</code>.
     * @param csq The character sequence to write
     * @param start The index of the first character to write
     * @param end  The index of the first character to write (exclusive)
     * @return this writer
     * @since 2.0
     */
    @Override
    public Writer append(CharSequence csq, int start, int end) {
        //to /dev/null
        return this;
    }

    /**
     * Does nothing - output to <code>/dev/null</code>.
     * @param csq The character sequence to write
     * @return this writer
     * @since 2.0
     */
    @Override
    public Writer append(CharSequence csq) {
        //to /dev/null
        return this;
    }

    /**
     * Does nothing - output to <code>/dev/null</code>.
     * @param idx The character to write
     */
    @Override
    public void write(int idx) {
        //to /dev/null
    }

    /**
     * Does nothing - output to <code>/dev/null</code>.
     * @param chr The characters to write
     */
    @Override
    public void write(char[] chr) {
        //to /dev/null
    }

    /**
     * Does nothing - output to <code>/dev/null</code>.
     * @param chr The characters to write
     * @param st The start offset
     * @param end The number of characters to write
     */
    @Override
    public void write(char[] chr, int st, int end) {
        //to /dev/null
    }

    /**
     * Does nothing - output to <code>/dev/null</code>.
     * @param str The string to write
     */
    @Override
    public void write(String str) {
        //to /dev/null
    }

    /**
     * Does nothing - output to <code>/dev/null</code>.
     * @param str The string to write
     * @param st The start offset
     * @param end The number of characters to write
     */
    @Override
    public void write(String str, int st, int end) {
        //to /dev/null
    }

    /** @see java.io.Writer#flush() */
    @Override
    public void flush() {
        //to /dev/null
    }

    /** @see java.io.Writer#close() */
    @Override
    public void close() {
        //to /dev/null
    }

}
