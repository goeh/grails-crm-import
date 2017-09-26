<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmImportSpecification.label', default: 'Import Specification')}"/>
    <title><g:message code="crmImportSpecification.create.title" args="[entityName]"/></title>
    <r:script>
        function analyzeFile(input) {
            var elem = $(input);
            var form = elem.closest("form").get(0);
            var formData = new FormData(form);
            $.ajax({
                url: "${createLink(action: 'analyzeFile')}",
                type: 'POST',
                xhr: function() {  // custom xhr
                    myXhr = $.ajaxSettings.xhr();
                    if(myXhr.upload){ // check if upload property exists
                        // for handling the progress of the upload
                        myXhr.upload.addEventListener('progress', progressHandlingFunction, false);
                    }
                    return myXhr;
                },
                success: function(result)
                {
                    if(result.format) {
                        $("#format").val(result.format);
                        $("#formatName").text(result.formatName);
                        $("#progress").text(result.size);
                    }
                    if(result.importer) {
                        $("#importer").val(result.importer);
                    }
                    var elem = $("#name");
                    if(result.name && ! elem.val()) {
                        elem.val(result.name);
                    }
                    $("#columns").append(createSampleTable(result.columns));
                },
                // Form data
                data: formData,
                //Options to tell JQuery not to process data or worry about content-type
                cache: false,
                contentType: false,
                processData: false
            });
        }

        function createSampleTable(columns) {
            var keys = Object.keys(columns);
            var headerRow = $('<tr></tr>');
            for(i = 0; i < keys.length; i++) {
                headerRow.append($("<th></th>").text(keys[i]));
            }
            var tbody = $('<tbody></tbody>');
            var keepGoing = true;
            var i = 0;
            while(keepGoing) {
                var tr = $('<tr></tr>');
                for(j = 0; j < keys.length; j++) {
                    var values = columns[keys[j]].samples;
                    if(values.length > i) {
                        var v = values[i];
                        if(v.length > 100) {
                            v = v.substring(0, 99) + '...';
                        }
                        tr.append($('<td></td>').text(v));
                    } else {
                        keepGoing = false;
                    }
                }
                if(keepGoing) {
                    tbody.append(tr);
                }
                i++;
            }
            var thead = $('<thead></thead>');
            thead.append(headerRow);
            var table = $('<table class="table table-striped"></table>');
            table.append(thead);
            table.append(tbody);
            return table;
        }

        function progressHandlingFunction(e){
            if(e.lengthComputable){
                $("#progress").text(e.loaded + " bytes");
            }
        }
    </r:script>
</head>

<body>

<crm:header title="crmImportSpecification.create.title" args="[entityName]"/>

<g:hasErrors bean="${crmImportSpecification}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${crmImportSpecification}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:uploadForm action="create" name="uploadForm">

    <fieldset>

        <g:hiddenField name="name"/>
        <g:hiddenField name="format"/>

        <div class="row-fluid">
            <div class="span7">
                <div class="control-group">
                    <label class="control-label">Välj fil att importera</label>

                    <div class="controls">
                        <input type="file" name="file" autofocus="" onchange="analyzeFile(this)"/>
                    </div>
                </div>

                <p>
                    <span id="formatName"></span>
                    <span id="progress"></span>
                </p>
            </div>

            <div class="span5">
                <div class="control-group">
                    <label class="control-label">Välj vad som ska importeras</label>

                    <div class="controls">
                        <g:select from="${importers}" name="importer"
                                  optionValue="${{ message(code: it + '.label', default: it) }}"/>
                    </div>
                </div>

            </div>

        </div>
    </fieldset>

    <div id="columns"></div>

    <div class="form-actions">
        <crm:button visual="success" icon="icon-arrow-right icon-white"
                    label="crmImportSpecification.button.continue.label"
                    permission="crmImportSpecification:create"/>
    </div>
</g:uploadForm>

</body>
</html>
