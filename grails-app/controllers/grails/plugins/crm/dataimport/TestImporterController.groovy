package grails.plugins.crm.dataimport

/**
 * Test importer
 */
class TestImporterController {

    def index() {
        def crmImport = request.session.crmImport
        if (!crmImport) {
            redirect controller: "crmImport", action:"create", params: [importer: controllerName]
            return
        }
        switch (request.method) {
            case 'GET':
                def columns = crmImport.file.withInputStream { is ->
                    def fmt = ImportFormatFactory.getFormat(is, crmImport.filename, crmImport.contentType)
                    def analyzer = new ImportFileAnalyzer()
                    return analyzer.analyze(fmt)
                }
                def functions = ['Text', 'VERSALER', 'gemener', 'Delmängd', 'Prefix', 'Suffix', 'Aktuell tid', 'Hoppa över', 'Nyckelfält']
                [filename: crmImport.filename, contentType: crmImport.contentType, size: crmImport.file.length(),
                        columns: columns, functions: functions]
                break
            case 'POST':
                break
        }
    }
}
