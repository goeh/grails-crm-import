package grails.plugins.crm.dataimport

/**
 * String functions that can be used in import tasks to transform values.
 */
class StringFunctions {

    static def lowercase(Object value) {
        value != null ? value.toString().toLowerCase() : null
    }


    static def uppercase(Object value) {
        value != null ? value.toString().toUpperCase() : null
    }


    static def replace(String searchFor, String replaceWith, Object value) {
        value != null ? value.toString().replaceAll(searchFor, replaceWith) : null
    }
}
