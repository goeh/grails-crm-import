package grails.plugins.crm.dataimport

import grails.converters.JSON
import grails.plugins.crm.core.TenantUtils
import grails.plugins.crm.core.WebUtils
import grails.util.GrailsNameUtils
import org.apache.commons.io.FilenameUtils
import org.springframework.dao.DataIntegrityViolationException

import javax.servlet.http.HttpServletResponse

/**
 * Main data import UI controller.
 */
class CrmImportController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    static navigation = [
            [group: 'admin',
                    order: 370,
                    title: 'crmImport.label',
                    action: 'index'
            ],
            [group: 'crmImport',
                    order: 20,
                    title: 'crmImport.create.label',
                    action: 'create',
                    isVisible: { actionName != 'create' }
            ]
    ]

    def selectionService

    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        def baseURI = new URI('gorm://crmImportSpecification/list')
        def query = params.getSelectionQuery()
        def uri

        switch (request.method) {
            case 'GET':
                uri = params.getSelectionURI() ?: selectionService.addQuery(baseURI, query)
                break
            case 'POST':
                uri = selectionService.addQuery(baseURI, query)
                WebUtils.setTenantData(request, 'crmImportQuery', query)
                break
        }

        params.max = Math.min(params.max ? params.int('max') : 20, 100)

        try {
            def result = selectionService.select(uri, params)
            [result: result, totalCount: result.totalCount, selection: uri]
        } catch (Exception e) {
            flash.error = e.message
            [result: [], totalCount: 0, selection: uri]
        }
    }

    def create() {
        switch (request.method) {
            case 'GET':
                break
            case 'POST':
                def importer = params.importer
                if (!importer) {
                    log.error "No importer specified"
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST)
                    return
                }
                def fileItem = request.getFile("file")
                if (fileItem && !fileItem.isEmpty()) {
                    def filename = fileItem.originalFilename
                    def ext = FilenameUtils.getExtension(filename)
                    def is = fileItem.inputStream
                    def tempfile = File.createTempFile("crm", ext)
                    tempfile.deleteOnExit()
                    tempfile.withOutputStream { out ->
                        out << is
                    }
                    is.close()
                    request.session.crmImport = [file: tempfile, filename: filename, contentType: fileItem.contentType]
                    redirect controller: importer, action: "index"
                }
                break
        }
    }

    def createOld() {
        def m = new CrmImportSpecification()
        def importers = grailsApplication.importerClasses*.propertyName
        def formats = ['csvImportFormat', 'xmlImportFormat', 'excelImportFormat']

        switch (request.method) {
            case 'GET':
                [crmImportSpecification: m, importers: importers, formats: formats]
                break
            case 'POST':
                if (!params.name) {
                    params.name = message(code: 'crmImportSpecification.name.example', default: 'Import of {0}', args: ['unknown'])
                }
                m.name = CrmImportSpecification.createUniqueName(params.name)
                def task = new CrmImportTask(orderIndex: 1, importer: params.importer, cfg: '{}')
                m.addToTasks(task)

                if (!m.save(flush: true)) {
                    render view: 'create', model: [crmImportSpecification: m, importers: importers, formats: formats]
                    return
                }

                def fileItem = request.getFile("file")
                if (fileItem && !fileItem.isEmpty()) {
                    def is = fileItem.inputStream
                    def filename = fileItem.originalFilename
                    def size = fileItem.size
                    def contentType = fileItem.contentType
                    def fmt = new CsvImportFormat(is, true)
                    def analyzer = new ImportFileAnalyzer()
                    def columns = analyzer.analyze(fmt)
                    def importer = grailsApplication.mainContext.getBean(params.importer)

                    println "columns=$columns"
                    //columns.size() == 4
                    //columns.keySet().toList() == ['Framework', 'Language', 'Version', 'Email']
                }


                flash.success = message(code: 'crmImportSpecification.created.message',
                        args: [message(code: 'crmImportSpecification.label', default: 'Specification'), m.toString()])
                redirect action: 'edit', id: m.ident()
                break
        }
    }

    def edit(Long id) {
        def m = CrmImportSpecification.findByIdAndTenantId(id, TenantUtils.tenant)
        if (!m) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        switch (request.method) {
            case 'GET':
                [crmImportSpecification: m]
                break
            case 'POST':
                if (params.version) {
                    def version = params.version.toLong()
                    if (m.version > version) {
                        m.errors.rejectValue('version', 'crmImportSpecification.optimistic.locking.failure',
                                [message(code: 'crmImportSpecification.label', default: 'Specification')] as Object[],
                                "Another user has updated this specification while you were editing")
                        render view: 'edit', model: [crmImportSpecification: m]
                        return
                    }
                }
                bindData(m, params, [include: ['name', 'description']])
                if (!m.save(flush: true)) {
                    render view: 'edit', model: [crmImportSpecification: m]
                    return
                }
                flash.success = message(code: 'crmImportSpecification.updated.message',
                        args: [message(code: 'crmImportSpecification.label', default: 'Specification'), m.toString()])
                redirect action: 'list'
                break
        }
    }

    def delete() {
        def m = CrmImportSpecification.findByIdAndTenantId(id, TenantUtils.tenant)
        if (!m) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        try {
            def tombstone = m.toString()
            m.delete(flush: true)
            flash.warning = message(code: 'crmImportSpecification.deleted.message', args: [message(code: 'crmImportSpecification.label', default: 'Import'), tombstone])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'crmImportSpecification.not.deleted.message', args: [message(code: 'crmImportSpecification.label', default: 'Import'), params.id])
            redirect action: 'edit', id: params.id
        }
    }

    def analyzeFile() {
        def result = [:]
        def fileItem = request.getFile("file")
        if (fileItem && !fileItem.isEmpty()) {
            def is = fileItem.inputStream
            def filename = fileItem.originalFilename
            def contentType = fileItem.contentType
            def fmt = ImportFormatFactory.getFormat(is, filename, contentType)
            result.name = message(code: 'crmImportSpecification.name.example', default: 'Import of {0}', args: [FilenameUtils.getBaseName(filename)])
            if (fmt) {
                def analyzer = new ImportFileAnalyzer()
                def columns = analyzer.analyze(fmt)
                result.columns = columns.inject([:]) { map, kv -> map[kv.getKey()] = kv.getValue().toMap(); map }
                result.format = GrailsNameUtils.getPropertyName(fmt.class)
                result.formatName = message(code: result.format + '.label', default: result.format)
                result.bytes = fileItem.size
                result.size = WebUtils.bytesFormatted(result.bytes)
            }
        }
        result.importer = 'domainImporter'
        render result as JSON
    }
}
