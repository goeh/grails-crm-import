package grails.plugins.crm.dataimport

/**
 * Created with IntelliJ IDEA.
 * User: goran
 * Date: 2012-12-16
 * Time: 19:43
 * To change this template use File | Settings | File Templates.
 */
class AstSpecBuilderTests extends GroovyTestCase {

    void test1() {
        def domainClass = CrmImportSpecification
        def spec = new AstSpecBuilder().build("""${domainClass.name}.createCriteria().get(matchCriteria.curry(data))""")
        println spec
    }

    void test2() {
        def spec = new AstSpecBuilder().build("""
        new org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod().invoke(target, 'bind', [target, params].toArray())
    """)
        println spec
    }

    void test3() {
            def spec = new AstSpecBuilder().build("""
            new org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod().invoke(target, 'bind', [target, params, includesExcludes].toArray())
        """)
            println spec
        }
}
