package grails.plugins.crm.dataimport.mapper

/**
 * Created with IntelliJ IDEA.
 * User: goran
 * Date: 2012-12-30
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */
class Lowercase {
    static def apply(Object value) {
        value != null ? value.toString().toLowerCase() : null
    }
}
