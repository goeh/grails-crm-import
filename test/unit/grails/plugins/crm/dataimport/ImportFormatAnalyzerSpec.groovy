package grails.plugins.crm.dataimport

import spock.lang.Specification

/**
 * Test import format analyzer.
 */
class ImportFormatAnalyzerSpec extends Specification {

    private final static double EPSILON = 0.00001

    private boolean doubleEquals(double a, double b) {
        doubleEquals(a, b, EPSILON)
    }

    private boolean doubleEquals(double a, double b, double epsilon) {
        a == b ? true : Math.abs(a - b) < epsilon
    }

    def "analyze a file"() {
        given:
        def s = "Framework,Language,Version,Email\nGrails,Groovy,2.2.2,test@grails.org\nPlay,Scala,2.1.1,test@playframework.com\nWicket,Java,6.7.0,test@wicket.apache.org\n"
        def fmt = new CsvImportFormat(new ByteArrayInputStream(s.bytes), true)
        def analyzer = new ImportFileAnalyzer()

        when: "Analyze file and collect two samples"
        def columns = analyzer.analyze(fmt, 2)

        then:
        columns.size() == 4
        columns.keySet().toList() == ['Framework', 'Language', 'Version', 'Email']

        columns.Framework.type == 'string'
        columns.Framework.min == 4
        columns.Framework.max == 6

        columns.Language.type == 'string'
        columns.Language.min == 4
        columns.Language.max == 6

        columns.Version.type == 'decimal'
        columns.Version.min == 5
        columns.Version.max == 5

        columns.Email.type == 'email'
        columns.Email.min == 15
        columns.Email.max == 22

        doubleEquals(columns.Framework.score.string, 3.0)
        columns.Framework.score.integer == null
        columns.Framework.score.decimal == null
        columns.Framework.score.email == null

        doubleEquals(columns.Version.score.string, 3.0)
        columns.Version.score.integer == null
        doubleEquals(columns.Version.score.decimal, 3.6)
        columns.Version.score.email == null

        doubleEquals(columns.Email.score.string, 3.0)
        columns.Email.score.integer == null
        columns.Email.score.decimal == null
        doubleEquals(columns.Email.score.email, 3.3)

        // We only sample the first two records so Wicket is not sampled.
        columns.Framework.samples == ['Grails', 'Play']
        columns.Version.samples == ['2.2.2', '2.1.1']
    }

    def "assign domain properties"() {
        given:
        def s = "First Name,Last Name,Address,Zip,City,Phone,Email\nAnders,Svensson,186 Groovy Street,230,Grailstown,031-5551234,anders.svensson@acme.com\n"
        def fmt = new CsvImportFormat(new ByteArrayInputStream(s.bytes), true)
        def mapping = new ImportColumnMapper()
        mapping.add('number') { it ==~ /number|nbr|no/ }
        mapping.add('name') { it ==~ /name/ }
        mapping.add('address') { it ==~ /address|address1|street/ }
        mapping.add('postalCode') { it ==~ /postalcode|postcode|zipcode|zip/ }
        mapping.add('city') { it ==~ /city|postalcity|town/ }
        mapping.add('phone') { it ==~ /phone|telephone|mobile|cellular/ }
        mapping.add('email') { it ==~ /email|e\-mail/ }

        when:
        def columns = new ImportFileAnalyzer().analyze(fmt)
        def map = mapping.map(columns)

        then:
        columns.size() == 7
        map['First Name'] == null
        map['Last Name'] == null
        map.Address == 'address'
        map.Zip == 'postalCode'
        map.City == 'city'
        map.Phone == 'phone'
        map.Email == 'email'
    }
}
