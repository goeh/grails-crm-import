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

class DataMapper {
    Map mapper
    Map params
    Map storage
    Map values

    DataMapper(Closure arg) {
        this([:], arg)
    }

    DataMapper(Map params, Closure arg) {
        this.params = params
        this.mapper = DataMapperBuilder.build(arg)
        println "mapper=$mapper"
    }

    def methodMissing(String name, args) {
        def value = args.size() ? args[0] : values[name]
        if (value instanceof Closure) {
            value = value.call()
        }
        storage[name] = value
    }

    def propertyMissing(String name, value) {
        if (value instanceof Closure) {
            value = value.call()
        }
        storage[name] = value
    }

    def propertyMissing(String name) {
        values.containsKey(name) ? values[name] : storage[name]
    }

    Map map(Map inputValues) {
        storage = [:]
        values = inputValues
        if (params.include || params.exclude) {
            if (params.include == true) {
                storage.putAll(values)
            } else if (params.include instanceof List) {
                for (p in params.include) {
                    storage[p] = values[p]
                }
            } else if (params.include instanceof String) {
                storage[params.include] = values[params.include]
            } else if (params.exclude) {
                storage.putAll(values)
            }
        }
        if (params.exclude) {
            if (params.exclude instanceof List) {
                for (p in params.exclude) {
                    storage.remove(p)
                }
            } else if (params.exclude instanceof String) {
                storage.remove(params.exclude)
            }
        }

        try {
            mapper.call()
        } finally {
            values = null
        }

        storage
    }
}

class DataMapperBuilder {
    Map<String, Closure> functions = [:]
    Map<String, Object> map = [:]

    DataMapperBuilder() {
        // Initialize default mapper functions.
        functions.replace = StringFunctions.&replace
        functions.lowercase = StringFunctions.&lowercase
        functions.uppercase = StringFunctions.&uppercase
    }

    void addFunction(String name, Closure func) {
        functions[name] = func
    }

    Closure getFunction(String name) {
        functions[name]
    }

    static Map build(Closure arg) {
        def bld = new DataMapperBuilder()
        def dsl = arg.clone()
        dsl.delegate = bld
        dsl.resolveStrategy = Closure.DELEGATE_ONLY
        dsl.call()
        return bld.map
    }

    def propertyMissing(String name) {
        { arg -> delegate."$arg" }.curry(name)
    }

    def propertyMissing(String name, value) {
        if (value instanceof List) {
            def list = []
            value.each { v ->
                if(v instanceof List) {
                    list << functionCall(v)
                } else {
                    switch (v[0]) {
                        case '$':
                            list << this."${v.substring(1)}"
                            break
                        case '&':
                            def f = functions[v.substring(1)]
                            if(f) {
                                list << functionCall([])
                            } else {
                                throw new IllegalArgumentException("No such function: ${v.substring(1)}")
                            }
                            break
                        default:
                            list << v
                    }
                }
            }
            value = list
        }
        map[name] = value
    }

    def functionCall(List args) {

    }
}