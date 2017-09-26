package grails.plugins.crm.dataimport

import java.util.regex.Pattern

/**
 * Analyze file.
 */
class ImportFileAnalyzer {

    private static final Pattern INTEGER_PATTERN = ~/^\d+$/
    private static final Pattern DECIMAL_PATTERN = ~/^[\d,\.]+$/
    private static final Pattern EMAIL_PATTERN = ~/^[\w\.\-_]+@[\w\.\-_]+$/

    Map<String, Column> analyze(ImportFormat format, int samples = 10) {
        def columns = [:]
        int i = 0
        for (map in format) {
            map.each { key, value ->
                def column = columns.get(key, new Column())
                def len
                if (value != null) {
                    score(column, key, value)
                    len = value.length()
                    if(i < samples) {
                        column.sample(value)
                    }
                } else {
                    len = 0
                }
                if(len < column.min) {
                    column.min = len
                }
                if(len > column.max) {
                    column.max = len
                }
            }
            i++
        }
        return columns
    }

    private void score(Column column, String key, String value) {
        // Everything matches a string.
        column.addScore('string', 1.0)

        if (INTEGER_PATTERN.matcher(value).matches()) {
            column.addScore('integer', 1.1)
        }

        if (DECIMAL_PATTERN.matcher(value).matches()) {
            column.addScore('decimal', 1.2)
        }

        if (EMAIL_PATTERN.matcher(value).matches()) {
            column.addScore('email', 1.1)
        }
    }

    private static class Column {
        Map<String, Double> score = [:]
        int min = Integer.MAX_VALUE
        int max = 0
        List<String> samples = []

        public void addScore(String type, Double value) {
            score.put(type, score.get(type, 0.0) + value)
        }

        String getType() {
            score.max{it.value}?.key
        }

        String toString() {
            "${getType()}($min-$max)"
        }

        public void sample(Object arg) {
            samples << arg.toString()
        }

        public Map toMap() {
            [score: score.asImmutable(), type: getType(), min: min, max: max, samples: samples.asImmutable()]
        }
    }
}
