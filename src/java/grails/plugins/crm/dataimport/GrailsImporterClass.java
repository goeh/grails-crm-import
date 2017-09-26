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

package grails.plugins.crm.dataimport;

import groovy.lang.Binding;
import groovy.lang.Script;

import org.codehaus.groovy.grails.commons.InjectableGrailsClass;

import java.util.Map;

/**
 *
 * @author Goran Ehrsson
 */
public interface GrailsImporterClass extends InjectableGrailsClass {

    public static final String TYPE = "Importer";

    void configure(Map cfg);
    void beforeImport(Binding context);
    void afterImport(Binding context);
    void parse(Map data, Binding context);
    void onError(Exception e, Binding context);
}
