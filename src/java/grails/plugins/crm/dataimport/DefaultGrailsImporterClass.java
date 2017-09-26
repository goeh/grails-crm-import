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

import org.codehaus.groovy.grails.commons.AbstractInjectableGrailsClass;

import java.util.Map;

/**
 * @author Goran Ehrsson
 */
public class DefaultGrailsImporterClass extends AbstractInjectableGrailsClass implements GrailsImporterClass {

    public DefaultGrailsImporterClass(Class clazz) {
        super(clazz, GrailsImporterClass.TYPE);
    }

    public void configure(Map cfg) {
        getMetaClass().invokeMethod(getReferenceInstance(), "configure", new Object[]{cfg});
    }

    public void beforeImport(Binding context) {
        getMetaClass().invokeMethod(getReferenceInstance(), "beforeImport", new Object[]{context});
    }

    public void afterImport(Binding context) {
        getMetaClass().invokeMethod(getReferenceInstance(), "afterImport", new Object[]{context});
    }

    public void parse(Map data, Binding context) {
        getMetaClass().invokeMethod(getReferenceInstance(), "parse", new Object[]{data, context});
    }

    public void onError(Exception e, Binding context) {
        getMetaClass().invokeMethod(getReferenceInstance(), "onError", new Object[]{e, context});
    }
}
