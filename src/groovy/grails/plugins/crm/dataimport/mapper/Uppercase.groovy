package grails.plugins.crm.dataimport.mapper

/**
 * Created with IntelliJ IDEA.
 * User: goran
 * Date: 2012-12-30
 * Time: 23:14
 * To change this template use File | Settings | File Templates.
 */
class Uppercase {
    static def apply(Object value, List args) {
        value != null ? value.toString().toUpperCase() : null
    }
}
