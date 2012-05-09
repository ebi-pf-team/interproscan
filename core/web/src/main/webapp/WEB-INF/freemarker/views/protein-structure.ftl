<!doctype html>
<html>
<head>
<#--TODO: see http://html5boilerplate.com/-->
    <meta charset="utf-8">
<#--Check if protein exists-->
    <title>
    <#if protein?? && protein.structuralDatabases?has_content>
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
        <div class="tab">
            <div class="Protein_tab">Protein Structure</div>
        </div>

        <div class="main-box">
        <#if protein?? && protein.structuralDatabases?has_content>
            <h1>
                <#if standalone>
                ${protein.ac}
                    <#else>
                    ${protein.name} <span>(${protein.ac})</span>
                </#if>
            </h1>
        </#if>
        <#include "protein-structure-body.ftl"/>
        </div>
    </div>
</div>

<script type="text/javascript">

    // Tie the extra popup DIV to it's match SPAN
    $(document).ready(function() {
        $('span[id*="location-"]').each(
                function(i) {
                    <#if condensedView??>
                        preparePopup(this.id, ${condensedView.numSuperMatchBlobs});
                    <#else>
                        // No supermatches for this protein, but there are structural matches (e.g. B7ZMM2 as of InterPro release 37.0)
                        preparePopup(this.id, 0);
                    </#if>
                }
        );
    });
</script>
</body>
</html>
