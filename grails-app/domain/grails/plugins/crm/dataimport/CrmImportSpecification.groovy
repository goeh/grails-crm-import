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

import grails.plugins.crm.core.TenantEntity
import grails.plugins.crm.core.TenantUtils

@TenantEntity
class CrmImportSpecification {
    String name
    String description
    static hasMany = [tasks: CrmImportTask]
    static constraints = {
        name(maxSize: 80, blank: false, unique: 'tenantId')
        description(maxSize: 2000, nullable: true, widget: 'textarea')
    }
    static mapping = {
        tasks sort: 'orderIndex'
    }

    static String createUniqueName(name) {
        CrmImportSpecification.withNewSession {
            while(CrmImportSpecification.countByTenantIdAndName(TenantUtils.tenant, name)) {
                def m = name =~ /\-(\d+)$/
                if(m.find()) {
                    def start = m.start()
                    def currentNumber = Integer.valueOf(m.group(1))
                    name = "${name[0..start]}${++currentNumber}"
                } else {
                    name = "${name}-1"
                }
            }
        }
        return name
    }

    CrmImportResource addSource(URI uri) {
        if (!ident()) {
            throw new IllegalArgumentException("Specification must be saved before sources can be added")
        }
        new CrmImportResource(specification: this, uri: uri.toURL().toExternalForm()).save(failOnError: true)
    }

    List<CrmImportResource> getSources() {
        if (!ident()) {
            throw new IllegalArgumentException("Specification must be saved before sources can be accessed")
        }
        CrmImportResource.findAllBySpecification(this)
    }

    String toString() {
        name.toString()
    }
}
