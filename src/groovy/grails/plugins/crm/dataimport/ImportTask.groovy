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

/**
 * Import task defines operations during an import.
 */
class ImportTask {
    def name
    def importer
    DataMapper mapper

    def call(Closure dsl) {
        def c = dsl.clone()
        c.delegate = this
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.call()
    }

    def propertyMissing(String name, value) {
        importer[name] = value
    }

    def propertyMissing(String name) {
        importer[name]
    }

    def methodMissing(String name, args) {
        importer.invokeMethod(name, args)
    }

    def data(Closure dsl) {
        mapper = new DataMapper([:], dsl)
    }

    def data(Map args, Closure dsl) {
        mapper = new DataMapper(args, dsl)
    }


    def configure(Map cfg) {
        if (cfg.data) {
            final Map map = cfg.data
            def cl = {
                map.each { prop, value ->
                    if(value =~ /^\{\w+\}$/) {
                        value = {value[1..-2]}
                    }
                    delegate."$prop" = value
                }
            }
            mapper = new DataMapper(cl)
        }
        importer.configure(cfg)
    }

    protected void doBeforeImport(Binding context) {
        if (importer.respondsTo('beforeImport', [Binding].toArray())) {
            importer.beforeImport(context)
        } else {
            println "${importer.class.name} has no beforeImport() method"
        }
    }

    protected void doAfterImport(Binding context) {
        if (importer.respondsTo('afterImport', [Binding].toArray())) {
            importer.afterImport(context)
        } else {
            println "${importer.class.name} has no afterImport() method"
        }
    }

    protected void doParse(Map values, Binding context) {
        if (importer.respondsTo('parse', [Map, Binding].toArray())) {
            if (mapper != null) {
                values = mapper.map(values)
            } else {
                println "NO MAPPER!"
            }
            println "parse $values with binding ${context.variables}"
            importer.parse(values, context)
        } else {
            println "${importer.class.name} has no parse() method"
        }
    }

    protected void doOnError(Exception e, Binding context) {
        if (importer.respondsTo('onError', [Exception, Binding].toArray())) {
            importer.onError(e, context)
        }
    }

    String toString() {
        "${importer}($name)"
    }
}
