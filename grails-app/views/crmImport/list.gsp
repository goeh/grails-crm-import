<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmImportSpecification.label', default: 'Imports')}"/>
    <title><g:message code="crmImportSpecification.list.title" args="[entityName]"/></title>
</head>

<body>

<crm:header title="crmImportSpecification.list.title" args="[entityName]"/>

<table class="table table-striped">
    <thead>
    <tr>

        <crm:sortableColumn property="name"
                            title="${message(code: 'crmImportSpecification.name.label', default: 'Name')}"/>

        <th>Senast körd</th>
        <th>Status</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${result}" var="m">
        <tr>
            <td>
                <g:link action="edit" id="${m.id}">
                    ${fieldValue(bean: m, field: "name")}
                </g:link>
            </td>
            <td></td>
            <td></td>
        </tr>
    </g:each>
    </tbody>
</table>

<crm:paginate total="${totalCount}"/>

<div class="form-actions">
    <crm:button type="link" action="create" visual="success" icon="icon-file icon-white"
                label="crmImportSpecification.button.create.label"
                permission="crmImportSpecification:create"/>
</div>

</body>
</html>
