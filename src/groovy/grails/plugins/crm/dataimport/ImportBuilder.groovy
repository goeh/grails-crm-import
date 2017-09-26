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

package grails.plugins.crm.dataimport

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * Import DSL.
 */
class ImportBuilder {

    private ApplicationContext applicationContext

    ImportBuilder(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext
    }

    private Map<String, Object> globals = [:]
    private List<ImportTask> tasks = []
    private DataMapper mapper

    def call(Closure dsl) {
        buildFromSpec { dsl }
    }

    def buildFromSpec(Closure dsl) {
        dsl.delegate = this
        dsl.resolveStrategy = Closure.DELEGATE_FIRST
        dsl.call()
        return this
    }

    def data(Closure dsl) {
        mapper = new DataMapper([:], dsl)
    }

    def data(Map args, Closure dsl) {
        mapper = new DataMapper(args, dsl)
    }

    def methodMissing(String name, args) {
        def bean = applicationContext.getBean(name)
        def params
        def t
        switch (args.size()) {
            case 0:
                t = new ImportTask(importer: bean)
                break
            case 1:
                if (args[0] instanceof Closure) {
                    t = new ImportTask(importer: bean)
                    t.call(args[0])
                } else if (args[0] instanceof Map) {
                    t = new ImportTask(importer: bean)
                    params = args[0]
                } else {
                    throw new IllegalArgumentException("Parameter must be a Map or a Closure")
                }
                break
            case 2:
                if(args[0] instanceof String) {
                    t = new ImportTask(importer: bean)
                    params = [description: args[0]]
                } else if(args[0] instanceof Map) {
                    t = new ImportTask(importer: bean)
                    params = args[0]
                } else {
                    throw new IllegalArgumentException("Parameter must be a String, Map or Closure")
                }
                t.call(args[1])
                break
        }

        if (t) {
            params.each{k, v->
                t."$k" = v
            }
            tasks << t
        }
        return t
    }

    def propertyMissing(String name, value) {
        globals[name] = value
    }

    def propertyMissing(String name) {
        globals[name]
    }

    private ImportFormat getImportFormat(file) {
        new CsvImportFormat(file.newInputStream(), true)
    }

    def load(File file, Binding binding) {
        println "load($file) using $tasks"
        globals.each{k, v->
            if(! binding.hasVariable(k)) {
                binding.setProperty(k, v)
            }
        }
        try {
            def format = getImportFormat(file)
            for (t in tasks) {
                t.doBeforeImport(binding)
            }
            def data
            while (data = format.next()) {
                for (t in tasks) {
                    t.doParse(data, binding)
                }
            }
            for (t in tasks) {
                t.doAfterImport(binding)
            }
        } catch (Exception e) {
            log.warn("Exception during import: ${e.message}")
            for (t in tasks) {
                t.doOnError(e, binding)
            }
            throw e
        }
        return this
    }
}
