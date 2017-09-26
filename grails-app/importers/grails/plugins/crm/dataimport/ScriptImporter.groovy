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
 * Generic importer implementation that executes a Groovy script for each record parsed.
 */
@CrmImport
class ScriptImporter {

    def grailsApplication
    def binding = new Binding([:])
    Script script

    private Closure beforeImport
    private Closure afterImport
    private Closure parse

    // This importer class is a delegate in it's own DSL parsing
    // so we need a few methods to support the DSL.
    //
    private Closure copy(Closure arg) {
        Closure c = arg.clone()
        c.delegate = this
        c.resolveStrategy = Closure.DELEGATE_FIRST
        return c
    }

    def propertyMissing(String name, value) {
        binding[name] = value
    }

    def propertyMissing(String name) {
        binding[name]
    }

    def script(String code) {
        // Create a new classloader for the script so GC can unload the created script class.
        def scriptClassLoader = new GroovyClassLoader(grailsApplication.classLoader)
        binding.setProperty('grailsApplication', grailsApplication)
        def shell = new GroovyShell(scriptClassLoader, binding)
        script = shell.parse(code, "script" + code.encodeAsMD5())
    }

    def beforeImport(Closure script) {
        beforeImport = copy(script)
    }

    def afterImport(Closure script) {
        afterImport = copy(script)
    }

    def parse(Closure script) {
        parse = copy(script)
    }

    // Runtime methods follow.
    //
    def beforeImport(Binding context) {
        beforeImport?.call(context)
    }

    def afterImport(Binding context) {
        afterImport?.call(context)
    }

    def parse(Map data, Binding context) {
        if (script != null) {
            script.setProperty('data', data)
            script.setProperty('context', context)
            //data.each { key, value ->
            //    script.setProperty(key, value)
            //}
            script.run()
        } else {
            parse?.call(data, context)
        }
    }
}
