package grails.plugins.crm.dataimport

import grails.test.spock.IntegrationSpec
import test.TestEntity

/**
 * Test persisted import specifications.
 */
class CrmImportConfigurationSpec extends IntegrationSpec {

    def crmImportService

    def "test configuration"() {
        given:
        def file = File.createTempFile("crm", ".csv")
        file.deleteOnExit()
        file.withPrintWriter("UTF-8") { out ->
            out.println('CustomerNo,CompanyName,Address,Zip,City,Phone,Email')
            out.println('1234-0,"ACME Inc","212 Grails Street","789 0","New Valley","+46 555 55555","info@acme.com"')
            out.println('17-32,"Spring Code","31 Enterprise Road","76 54","Alice Springs","+1 555 1234","info@springcode.com"')
            out.println('2013,"Groovy Labs","","12345","Groovytown", "","info@groovylabs.com"')
        }
        def spec = new CrmImportSpecification(name: "Test 1", description: "A simple test")
        def task = new CrmImportTask(importer: "domainImporter")
        def cfg = [
                domain: TestEntity.name,
                match: [number: '$CustomerNo'],
                data: [number: '$CustomerNo',
                        name: '$CompanyName',
                        address: '$Address',
                        postalCode: ['$Zip', ['&replace', " ", ""]],
                        city: '$City',
                        phone: ['$Phone', ['&replace', " ", ""], ['&replace', "-", ""]],
                        email: '$Email',
                        source: 'CrmImportConfigurationSpec'
                ]
        ]
        task.setConfiguration(cfg)
        spec.addToTasks(task)
        spec.save(failOnError: true)
        def res = new CrmImportResource(specification: spec, uri: file.toURI().toString()).save(failOnError: true)

        when:
        println task.configuration.toString()
        def binding = new Binding([:])
        crmImportService.buildFromSpec(spec).load(file, binding)
        println "$binding"

        then:
        res.id != null
    }
}
