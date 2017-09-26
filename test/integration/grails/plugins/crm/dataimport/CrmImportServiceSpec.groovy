package grails.plugins.crm.dataimport

import grails.test.spock.IntegrationSpec
import test.TestEntity

/**
 * Test various imports.
 */
class CrmImportServiceSpec extends IntegrationSpec {

    def crmImportService

    def "test scriptImporter"() {
        given:
        def file = File.createTempFile("crm", ".csv")
        file.deleteOnExit()
        file.withPrintWriter("UTF-8") { out ->
            out.println('CustomerNo,CompanyName,Address,Zip,City,Phone,Email')
            out.println('1234-0,"ACME Inc","212 Grails Street","789 0","New Valley","+46 555 55555","info@acme.com"')
            out.println('17-32,"Spring Code","31 Enterprise Road","76 54","Alice Springs","+1 555 1234","info@springcode.com"')
            out.println('2013,"Groovy Labs","","12345","Groovytown", "","info@groovylabs.com"')
        }

        when:
        def result = crmImportService.load(file) {
            data {
                username = 'demo'
            }
            scriptImporter("A script can do anything") {
                data {
                    number = { CustomerNo }
                    name = CompanyName
                    address = Address
                    postalCode = Zip?.replaceAll(' ', '')
                    city = ['$City', '&uppercase']
                    telephone = Phone?.replaceAll(' ', '')?.replaceAll('-', '')
                    email = ['$Email', ['&replace', ' ', '']]
                    tags = ['red', 'green', 'blue']
                }
                beforeImport { context ->
                    println "Before script import"
                }
                afterImport { context ->
                    println "After script import"
                }
                script """
                    println "Customer Number=\${data.number}"
                    context.value = data.number
                """
            }
        }

        then:
        result?.value == '2013'
    }

    def "test domainImporter"() {
        given:
        def count = TestEntity.count()
        def file = File.createTempFile("crm", ".csv")
        file.deleteOnExit()
        file.withPrintWriter("UTF-8") { out ->
            out.println('CustomerNo,CompanyName,Address,Zip,City,Phone,Email')
            out.println('1234-0,"ACME Inc","212 Grails Street","789 0","New Valley","+46 555 55555","info@acme.com"')
            out.println('17-32,"Spring Code","31 Enterprise Road","76 54","Alice Springs","+1 555 1234","info@springcode.com"')
            out.println('2013,"Groovy Labs","","12345","Groovytown", "","info@groovylabs.com"')
        }

        when:
        def result = crmImportService.load(file) {
            domainImporter(name: "The domain importer can import any domain class") {
                domain TestEntity
                data {
                    number(CustomerNo)
                    name(CompanyName)
                    address(Address)
                    postalCode(Zip)
                    city(City)
                    phone(Phone)
                    email(Email)
                }
                result "company"
            }
        }

        then:
        result.company.name == 'Groovy Labs'
        TestEntity.count() == (count + 3)
    }
}
