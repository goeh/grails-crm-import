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

import spock.lang.Specification

/**
 * Unit tests for the CrmImportFormat class.
 */
class CsvImportFormatSpec extends Specification {

    def "parse without header"() {
        given:
        def s = "Grails,Groovy\nPlay,Scala\nWicket,Java\n"
        def fmt = new CsvImportFormat(new ByteArrayInputStream(s.bytes))
        def map

        when:
        map = fmt.next()

        then:
        map.A == "Grails"
        map.B == "Groovy"

        when:
        map = fmt.next()

        then:
        map.A == "Play"
        map.B == "Scala"


        when:
        map = fmt.next()

        then:
        map.A == "Wicket"
        map.B == "Java"

        when:
        map = fmt.next()

        then:
        map == null
    }

    def "parse header and three lines"() {
        given:
        def s = "Framework,Language\nGrails,Groovy\nPlay,Scala\nWicket,Java\n"
        def fmt = new CsvImportFormat(new ByteArrayInputStream(s.bytes), true)
        def map

        when:
        map = fmt.next()

        then:
        map.Framework == "Grails"
        map.Language == "Groovy"

        when:
        map = fmt.next()

        then:
        map.Framework == "Play"
        map.Language == "Scala"


        when:
        map = fmt.next()

        then:
        map.Framework == "Wicket"
        map.Language == "Java"

        when:
        map = fmt.next()

        then:
        map == null
    }

    def "use each() to iterate"() {
        given:
        def s = "Framework,Language\nGrails,Groovy\nPlay,Scala\nWicket,Java\n"
        def fmt = new CsvImportFormat(new ByteArrayInputStream(s.bytes), true)
        def result = []

        when:
        fmt.each{
            result << it
        }

        then:
        result.size() == 3
        result[0].Framework == 'Grails'
        result[1].Framework == 'Play'
        result[2].Framework == 'Wicket'
    }


    def "use for() to iterate"() {
        given:
        def s = "Framework,Language\nGrails,Groovy\nPlay,Scala\nWicket,Java\n"
        def fmt = new CsvImportFormat(new ByteArrayInputStream(s.bytes), true)
        def result = []

        when:
        for(it in fmt) {
            result << it
        }

        then:
        result.size() == 3
        result[0].Framework == 'Grails'
        result[1].Framework == 'Play'
        result[2].Framework == 'Wicket'
    }
}
