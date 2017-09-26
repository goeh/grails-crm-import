/*
 * Copyright (c) 2012 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.dataimport

import au.com.bytecode.opencsv.CSVReader

/**
 * An import format that parse comma separated values from a text file.
 */
class CsvImportFormat implements ImportFormat {

    private CSVReader reader
    private boolean headerRow = true
    public List<String> header

    CsvImportFormat(InputStream inputStream) {
        reader = new CSVReader(new InputStreamReader(inputStream))
    }

    CsvImportFormat(InputStream inputStream, boolean headerRow) {
        reader = new CSVReader(new InputStreamReader(inputStream))
        this.headerRow = headerRow
    }

    Iterator<Map> iterator() {
        new CsvIterator()
    }

    Map next() {
        def values
        def line = reader.readNext()
        if (line != null) {
            if (header || !headerRow) {
                values = parseValues(line.toList(), header)
            } else if (headerRow) {
                header = line.toList()
                values = next()
            }
        }
        return values
    }

    private Map parseValues(List<String> columns, List<String> header) {
        def values = [:]
        columns.eachWithIndex { v, i ->
            values[header ? header[i] : getHeaderKey(i)] = v
        }
        return values
    }

    private String getHeaderKey(int i) {
        int j = i % 26
        int k = (i / 26).intValue()
        String s = ""
        if (k) {
            s += Character.toChars(k.intValue() - 1 + 65)
        }
        s += Character.toChars(j + 65)
        return s
    }

    void close() {
        reader.close()
    }

    private class CsvIterator implements Iterator<Map> {

        Map last

        boolean hasNext() {
            if(last != null) {
                return true
            }
            last = CsvImportFormat.this.next()
            if(last) {
                return true
            }
            return false
        }

        Map next() {
            if(last) {
                def tmp = last
                last = null
                return tmp
            }
            CsvImportFormat.this.next()
        }

        void remove() {
            throw new UnsupportedOperationException("remove() is not supported")
        }
    }
}
