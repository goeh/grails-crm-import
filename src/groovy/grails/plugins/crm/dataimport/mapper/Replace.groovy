package grails.plugins.crm.dataimport.mapper

/**
 * Created with IntelliJ IDEA.
 * User: goran
 * Date: 2012-12-30
 * Time: 23:11
 * To change this template use File | Settings | File Templates.
 */
class Replace {
    static def apply(Object value, List args) {
        if(value != null) {
            value = value.toString().replaceAll(args[0], args[1])
        }
        value
    }
}
