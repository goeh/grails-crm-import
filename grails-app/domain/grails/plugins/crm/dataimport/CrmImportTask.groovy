package grails.plugins.crm.dataimport

import groovy.json.JsonSlurper


/**
 * During a data import one or more tasks are executed.
 */
class CrmImportTask {
    int orderIndex
    String importer
    String cfg
    static belongsTo = [specification: CrmImportSpecification]
    static constraints = {
        importer(maxSize: 80, blank: false)
        cfg(maxSize: 102400, nullable: true)
    }
    static mapping = {
        sort 'orderIndex'
    }
    static transients = ['configuration']

    def beforeValidate() {
        if (orderIndex == 0 && specification != null) {
            def mx
            withNewSession {
                mx = this.getClass().createCriteria().get {
                    projections {
                        max "orderIndex"
                    }
                    eq('specification', specification)
                }
            }
            orderIndex = mx ? mx + 1 : 1
        }
    }

    String toString() {
        importer.toString()
    }

    transient Map getConfiguration() {
        cfg ? new JsonSlurper().parseText(cfg) : null
    }

    /**
     * Set this task's configuration.
     * Configuration is serialized with <code>groovy.json.JsonOutput</code> and thus persisted as JSON strings.
     * @param arg the configuration value to set
     */
    void setConfiguration(Map arg) {
        cfg = arg ? groovy.json.JsonOutput.toJson(arg) : null
    }
}
