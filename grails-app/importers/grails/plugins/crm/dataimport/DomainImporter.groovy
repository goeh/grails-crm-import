package grails.plugins.crm.dataimport

/**
 * Generic importer.
 */
@CrmImport
class DomainImporter {

    private Class domainClass
    private Map matchParams
    private Closure matchCriteria
    private Closure beforeImport
    private Closure afterImport
    String resultKey

    def grailsApplication

    def configure(Map cfg) {
        domain(cfg.domain)
        if (cfg.match) {
            matchCriteria = { data ->
                cfg.match.each { prop, value ->
                    eq(prop, data."$value")
                }
            }
        }
    }

    def analyze(ImportFormat fmt) {
        new ImportFileAnalyzer().analyze(fmt)
    }

    // This importer class is a delegate in it's own DSL parsing
    // so we need a few methods to support the DSL.
    //
    private Closure copy(Closure arg) {
        Closure c = arg.clone()
        c.delegate = this
        c.resolveStrategy = Closure.DELEGATE_FIRST
        return c
    }

    def domain(Class klass) {
        domainClass = klass
    }

    def domain(String className) {
        domainClass = grailsApplication.classLoader.loadClass(className)
    }

    def match(Closure dsl) {
        match([:], dsl)
    }

    def match(Map args, Closure dsl) {
        matchParams = args
        matchCriteria = dsl
    }

    def result(String arg) {
        this.resultKey = arg
    }

    def beforeImport(Closure statements) {
        beforeImport = copy(statements)
    }

    def afterImport(Closure statements) {
        afterImport = copy(statements)
    }

    // Runtime methods follow.
    //
    def parse(Map data, Binding context) {
        def domainInstance
        if (matchCriteria != null) {
            domainInstance = exists(data, matchCriteria)
        }
        if (!domainInstance) {
            domainInstance = domainClass.newInstance()
        } else if (!matchParams?.update) {
            return
        }
        bindData(domainInstance, context.variables)
        bindData(domainInstance, data)
        domainInstance.save(failOnError: true)
        if (resultKey) {
            context.setProperty(resultKey, domainInstance)
        }
        return domainInstance
    }

    def exists(Map data, Closure matchCriteria) {
        domainClass.createCriteria().get(matchCriteria.curry(data))
    }
}
