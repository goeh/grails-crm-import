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

import grails.plugins.crm.dataimport.GrailsImporterClass
import grails.plugins.crm.dataimport.ImporterArtefactHandler

/**
 *
 */
class CrmImportGrailsPlugin {
    def groupId = ""
    // the plugin version
    def version = "2.4.0-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.4 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/domain/test/TestEntity.groovy"
    ]
    def title = "CRM Import Plugin"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''
Import data to GR8 CRM from Excel, CSV, and XML.
'''
    def documentation = "https://github.com/goeh/grails-crm-import"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/goeh/grails-crm-import/issues"]
    def scm = [url: "https://github.com/goeh/grails-crm-import"]

    def loadAfter = ['logging']
    def watchedResources = [
            "file:./grails-app/importers/**/*Importer.groovy",
            "file:./plugins/*/grails-app/importers/**/*Importer.groovy"
    ]
    def artefacts = [new ImporterArtefactHandler()]

    def doWithSpring = {
        // Configure importers defined in the project.
        def importerClasses = application.importerClasses
        importerClasses.each { importerClass ->
            def ci = configureImporter.clone()
            ci.delegate = delegate
            ci.call(importerClass)
        }
        println "Installed importers ${importerClasses*.propertyName}"
    }

    def onChange = { event ->
        if (application.isImporterClass(event.source)) {
            log.debug "Importer ${event.source} modified!"

            def context = event.ctx
            if (!context) {
                log.debug("Application context not found - can't reload.")
                return
            }

            // Make sure the new importer class is registered.
            def importerClass = application.addArtefact(GrailsImporterClass.TYPE, event.source)

            // We clone the closure because we're going to change the delegate.
            def beans = beans(configureImporter.curry(importerClass))
            beans.registerBeans(context)
        }
    }

    def configureImporter = { grailsClass ->
        def importerName = grailsClass.propertyName
        log.debug "Registering importer: ${grailsClass.fullName}"
        // Create the importer bean.
        "$importerName"(grailsClass.clazz) { bean ->
            bean.autowire = "byName"
            bean.singleton = false
        }
        return importerName
    }
}
