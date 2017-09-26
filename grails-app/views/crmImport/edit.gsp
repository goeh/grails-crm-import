<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmImportSpecification.label', default: 'Import Specification')}"/>
    <title><g:message code="crmImportSpecification.edit.title" args="[entityName]"/></title>
</head>

<body>

<crm:header title="crmImportSpecification.edit.title" args="[entityName, crmImportSpecification]"/>

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

<g:form>
    <g:hiddenField name="id" value="${crmImportSpecification.id}"/>

    <f:with bean="${crmImportSpecification}">
        <f:field property="name" input-class="input-xlarge" input-autofocus=""/>
        <f:field property="description" input-class="input-xlarge" input-rows="3"/>
    </f:with>

    <div class="form-actions">
        <crm:button action="edit" visual="success" icon="icon-ok icon-white"
                    label="crmImportSpecification.button.save.label"
                    permission="crmImportSpecification:edit"/>
        <crm:button action="delete" visual="danger" icon="icon-trash icon-white"
                    label="crmImportSpecification.button.delete.label"
                    confirm="crmImportSpecification.button.delete.confirm.message"
                    permission="crmImportSpecification:delete"/>
        <crm:button type="link" action="list"
                    icon="icon-remove"
                    label="crmImportSpecification.button.cancel.label"/>
    </div>
</g:form>

</body>
</html>
