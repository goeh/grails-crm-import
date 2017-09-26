package grails.plugins.crm.dataimport

/**
 * XML Import format.
 */
class XmlImportFormat implements ImportFormat {

    private Iterator itor

    XmlImportFormat(InputStream inputStream) {
        itor = new XmlSlurper().parse(inputStream).childNodes()
    }

    private Map toMap(node) {
        def map = [:]
        def children = node.children()
        for (child in children) {
            def value
            if (child.childNodes()) {
                value = child.childNodes().collect{toMap(it)}
            } else {
                value = child.text()
            }
            map.put(child.name(), value)
        }
        return [(node.name()): map]
    }

    Map next() {
        def node = itor.next()
        node != null ? toMap(node) : null
    }

    void close() {
    }

    Iterator<Map> iterator() {
        itor
    }
}
