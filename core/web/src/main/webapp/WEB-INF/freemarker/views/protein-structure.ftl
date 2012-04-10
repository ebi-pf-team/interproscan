<!doctype html>
<html>
<head>
    <#--TODO: see http://html5boilerplate.com/-->
    <meta charset="utf-8">
        <#--Check if protein exists-->
        <title>
        <#if protein??>
            ${protein.ac} - InterPro
        <#else>
            No data for this protein - InterPro
        </#if>
        </title>
    <meta name="description" content="Protein matches">
    <meta name="author" content="InterPro Team, European Bioinformatics Institute">
    <link href="${css_resource_jquery_qtip2}" rel="stylesheet" type="text/css"/>
    <link href="${css_resource_protein}" rel="stylesheet" type="text/css"/>
    <link href="${css_resource_type_colours}" rel="stylesheet" type="text/css"/>
    <script src="${js_resource_jquery171}" type="text/javascript"></script>
    <script src="${js_resource_jquery_qtip2}" type="text/javascript"></script>
    <script src="${js_resource_common}" type="text/javascript"></script>
    <script src="${js_resource_protein_popups}" type="text/javascript"></script>
</head>
<body>
<div id="main" role="main" class="main-content">
    <div class="contents" id="contents">
        <#--NOTE: Can use import with absolute URLs, so could in theory include content from DBML to aid transition!-->
            <#include "protein-structure-body.ftl"/>
    </div>
</div>

<script type="text/javascript">
    // Tie the extra popup DIV to it's match SPAN
    $(document).ready(function() {
        $('span[id*="location-"]').each(
                function(i) {
                    preparePopup(this.id);
                }
        );
    });
</script>
</body>
</html>
