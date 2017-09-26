<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="crmImport.create.title"/></title>
</head>

<body>

<crm:header title="crmImport.create.title" args="${[params.importer]}"/>

<g:uploadForm action="create" name="uploadForm">

    <g:hiddenField name="importer" value="${params.importer}"/>

    <div class="control-group">
        <label class="control-label"><g:message code="crmImport.file.label"/></label>

        <div class="controls">
            <input type="file" name="file" autofocus=""/>
        </div>
    </div>

    <div class="form-actions">
        <crm:button visual="success" icon="icon-arrow-right icon-white"
                    label="crmImport.button.continue.label"
                    permission="crmImport:create"/>
    </div>
</g:uploadForm>

</body>
</html>
