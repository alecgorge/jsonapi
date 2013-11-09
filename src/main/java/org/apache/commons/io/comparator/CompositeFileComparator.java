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
package org.apache.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Compare two files using a set of delegate file {@link Comparator}.
 * <p>
 * This comparator can be used to sort lists or arrays of files
 * by combining a number other comparators.
 * <p>
 * Example of sorting a list of files by type (i.e. directory or file)
 * and then by name:
 * <pre>
 *       CompositeFileComparator comparator =
 *                       new CompositeFileComparator(
 *                                   DirectoryFileComparator.DIRECTORY_COMPARATOR,
 *                                   NameFileComparator.NAME_COMPARATOR);
 *       List&lt;File&gt; list = ...
 *       comparator.sort(list);
 * </pre>
 *
 * @version $Id: CompositeFileComparator.java 1304052 2012-03-22 20:55:29Z ggregory $
 * @since 2.0
 */
public class CompositeFileComparator extends AbstractFileComparator implements Serializable {

    private static final Comparator<?>[] NO_COMPARATORS = {};
    private final Comparator<File>[] delegates;

    /**
     * Create a composite comparator for the set of delegate comparators.
     *
     * @param delegates The delegate file comparators
     */
    @SuppressWarnings("unchecked") // casts 1 & 2 must be OK because types are already correct
    public CompositeFileComparator(Comparator<File>... delegates) {
        if (delegates == null) {
            this.delegates = (Comparator<File>[]) NO_COMPARATORS;//1
        } else {
            this.delegates = (Comparator<File>[]) new Comparator<?>[delegates.length];//2
            System.arraycopy(delegates, 0, this.delegates, 0, delegates.length);
        }
    }

    /**
     * Create a composite comparator for the set of delegate comparators.
     *
     * @param delegates The delegate file comparators
     */
    @SuppressWarnings("unchecked") // casts 1 & 2 must be OK because types are already correct
    public CompositeFileComparator(Iterable<Comparator<File>> delegates) {
        if (delegates == null) {
            this.delegates = (Comparator<File>[]) NO_COMPARATORS; //1
        } else {
            List<Comparator<File>> list = new ArrayList<Comparator<File>>();
            for (Comparator<File> comparator : delegates) {
                list.add(comparator);
            }
            this.delegates = (Comparator<File>[]) list.toArray(new Comparator<?>[list.size()]); //2
        }
    }

    /**
     * Compare the two files using delegate comparators.
     * 
     * @param file1 The first file to compare
     * @param file2 The second file to compare
     * @return the first non-zero result returned from
     * the delegate comparators or zero.
     */
    public int compare(File file1, File file2) {
        int result = 0;
        for (Comparator<File> delegate : delegates) {
            result = delegate.compare(file1, file2);
            if (result != 0) {
                break;
            }
        }
        return result;
    }

    /**
     * String representation of this file comparator.
     *
     * @return String representation of this file comparator
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append('{');
        for (int i = 0; i < delegates.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(delegates[i]);
        }
        builder.append('}');
        return builder.toString();
    }
}
