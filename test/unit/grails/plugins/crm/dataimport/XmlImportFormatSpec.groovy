package grails.plugins.crm.dataimport

import spock.lang.Specification

/**
 * Created with IntelliJ IDEA.
 * User: goran
 * Date: 2013-05-14
 * Time: 09:38
 * To change this template use File | Settings | File Templates.
 */
class XmlImportFormatSpec extends Specification {

    def "parse a flat xml file"() {
        given:
        def s = """<test>
                        <framework><name>Grails</name><language>Groovy</language></framework>
                        <framework><name>Play</name><language>Scala</language></framework>
                        <framework><name>Wicket</name><language>Java</language></framework>
                   </test>"""
        def fmt = new XmlImportFormat(new ByteArrayInputStream(s.bytes))
        def map

        when:
        map = fmt.next().framework

        then:
        map.name == "Grails"
        map.language == "Groovy"

        when:
        map = fmt.next().framework

        then:
        map.name == "Play"
        map.language == "Scala"


        when:
        map = fmt.next().framework

        then:
        map.name == "Wicket"
        map.language == "Java"

        when:
        map = fmt.next()

        then:
        map == null
    }

    def "parse a nested xml file"() {
        given:
        def s = """<test>
                    <framework>
                        <name>Grails</name>
                        <committers>
                            <committer><name>Graeme Rocher</name></committer>
                            <committer><name>Jeff Brown</name></committer>
                            <committer><name>Burt Beckwith</name></committer>
                        </committers>
                    </framework>
               </test>"""
        def fmt = new XmlImportFormat(new ByteArrayInputStream(s.bytes))
        def map
        def facit = [framework: [name: "Grails", committers: [[committer: [name: "Graeme Rocher"]], [committer: [name: "Jeff Brown"]], [committer: [name: "Burk Beckwith"]]]]]

        when:
        map = fmt.next().framework

        then:
        map.name == "Grails"
        map.committers[0].committer.name == "Graeme Rocher"
        map.committers[1].committer.name == "Jeff Brown"
        map.committers[2].committer.name == "Burt Beckwith"

        when:
        map = fmt.next()

        then:
        map == null
    }
}
