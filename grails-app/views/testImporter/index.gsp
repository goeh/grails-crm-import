<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="testImporter.label"/></title>
    <r:script>
        $(document).ready(function () {
            $.event.props.push('dataTransfer');
            $('button.field').on({
                dragstart: function (e) {
                    console.log("dragstart");
                    e.dataTransfer.setData('type', 'field');
                    e.dataTransfer.setData('label', $(e.target).text());
                }
            });
            $('button.function').on({
                dragstart: function (e) {
                    console.log("dragstart");
                    e.dataTransfer.setData('type', 'function');
                    e.dataTransfer.setData('label', $(e.target).text());
                }
            });
            $('.target').on({
                dragover: function (e) {
                    e.preventDefault();
                    $(this).addClass('over');
                },
                dragleave: function (e) {
                    $(this).removeClass('over');
                },
                dragenter: function (e) {
                    $(this).addClass('over');
                },
                drop: function (e) {
                    var type = e.dataTransfer.getData('type');
                    if (type == 'field' || type == 'function') {
                        var btn = $('<button></button>');
                        btn.addClass('btn span12');
                        if (type == 'field') {
                            btn.addClass('btn-success');
                        } else {
                            //btn.addClass('btn-warning');
                        }
                        var label = e.dataTransfer.getData('label');
                        btn.css('margin-left', '0');
                        btn.text(label);
                        $(this).children('.match').append(btn);
                        var config = $(this).find('input[type="hidden"]');
                        var value = config.val();
                        if (value) {
                            value = value + ',' + type + ':' + label;
                        } else {
                            value = type + ':' + label;
                        }
                        config.val(value);
                    }
                    $(this).removeClass('over');
                }
            });
        });
    </r:script>
    <style type="text/css">
    .over {
        background-color: #10acde;
        opacity: 0.7;
    }

    #functions button {
        margin-bottom: 5px;
    }
    </style>
</head>

<body>

<crm:header title="testImporter.label"/>

<g:form action="index">
    <div class="row-fluid form-horizontal">
        <div class="span6">

            <div id="fields">
                <h3>Datafält i ${filename.encodeAsHTML()}</h3>

                <g:each in="${columns}" var="col">
                    <div class="row-fluid">
                        <button class="btn btn-success span5 draggable field" draggable="true">${col.key}</button>

                        <input type="text" readonly="" class="span7" value="${col.value.samples.head()}"/>
                    </div>
                </g:each>
            </div>

            <div id="functions">
                <h3>Funktioner</h3>

                <div class="row-fluid">
                    <g:each in="${functions}" var="f">
                        <button class="btn draggable function" draggable="true">${f}</button>
                    </g:each>
                </div>
            </div>
        </div>

        <div class="span6">
            <h3>Testa import</h3>

            <div class="row-fluid target">
                <div class="match span6"></div>

                <div class="span1" style="text-align: center;"><i class="icon-arrow-right"></i></div>

                <button class="btn btn-primary span5">Ramverk*</button>

                <input type="hidden" name="framework"/>
            </div>

            <div class="row-fluid target">
                <div class="match span6"></div>

                <div class="span1" style="text-align: center;"><i class="icon-arrow-right"></i></div>

                <button class="btn btn-primary span5">Språk*</button>

                <input type="hidden" name="language"/>
            </div>

            <div class="row-fluid target">
                <div class="match span6"></div>

                <div class="span1" style="text-align: center;"><i class="icon-arrow-right"></i></div>

                <button class="btn btn-primary span5">Version*</button>

                <input type="hidden" name="version"/>
            </div>

            <div class="row-fluid target">
                <div class="match span6"></div>

                <div class="span1" style="text-align: center;"><i class="icon-arrow-right"></i></div>

                <button class="btn btn-info span5">E-post</button>

                <input type="hidden" name="email"/>
            </div>

        </div>
    </div>

    <div class="form-actions">
        <crm:button visual="success" icon="icon-file icon-white"
                    label="crmImport.button.save.label"
                    permission="crmImport:create"/>
    </div>

</g:form>

</body>
</html>
