package grails.plugins.crm.dataimport

/**
 *
 */
class ImportFormatFactory {

    static ImportFormat getFormat(InputStream is, String filename, String contentType) {
        if (contentType.startsWith('text')) {
            if (filename.toLowerCase().endsWith('.csv')) {
                return new CsvImportFormat(is)
            }
            if (filename.toLowerCase().endsWith('.xml')) {
                return new XmlImportFormat(is)
            }
        }
        return null
    }
}
