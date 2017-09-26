package grails.plugins.crm.dataimport

import org.springframework.transaction.interceptor.TransactionAspectSupport

/**
 * Main data import service for Grails CRM.
 */
class CrmImportService {

    def sessionFactory
    def grailsApplication

    def createSpecification(String name) {
        def spec = new CrmImportSpecification(name: name)
        return spec
    }

    def buildFromSpec(Closure dsl) {
        new ImportBuilder(grailsApplication.mainContext).buildFromSpec(dsl)
    }

    def buildFromSpec(CrmImportSpecification spec) {
        def importBuilder = new ImportBuilder(grailsApplication.mainContext)
        for(t in spec?.tasks) {
            def task = importBuilder."${t.importer}"()
            final Map cfg = t.configuration
            task.configure(cfg)
        }
        return importBuilder
    }

    def test(File file, Closure dsl) {
        def binding = new Binding([:])
        try {
            buildFromSpec(dsl).load(file, binding)
            TransactionAspectSupport.currentTransactionInfo().getTransactionStatus().setRollbackOnly()
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) {
                e = new RuntimeException(e)
            }
            throw e
        } finally {
            cleanUp()
        }
        return binding
    }

    def load(File file, Closure dsl) {
        def binding = new Binding([:])
        try {
            buildFromSpec(dsl).load(file, binding)
        } finally {
            cleanUp()
        }
        return binding
    }

    def cleanUp = {
        def session = sessionFactory.getCurrentSession()
        if (session) {
            session.flush()
            session.clear()
        }
        // TODO Fixed in Grails 2.0?
        org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP.get().clear()
    }
}
