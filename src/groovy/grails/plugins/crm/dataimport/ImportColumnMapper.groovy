package grails.plugins.crm.dataimport

/**
 * Map column names to domain properties.
 */
class ImportColumnMapper {

    def mapping = [:]

    void add(String name, Closure analyzer) {
        mapping.get(name, []) << analyzer
    }

    def map(Map columns) {
        def result = [:]
        columns.each {name, column->
            def entry = mapping.find{
                for(m in it.value) {
                    if(m.call(name.toLowerCase())) {
                        return true
                    }
                    return false
                }
            }
            if(entry) {
                result[name] = entry.key
            }
        }
        return result
    }
}
