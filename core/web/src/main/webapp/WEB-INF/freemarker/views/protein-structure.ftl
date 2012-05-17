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
<div class="contentsarea">

<#if protein?? && protein.ac?has_content>
<#-- This protein accession exists, now start to display the data  -->
<div class="main-content">
    <div class="tab">
        <div class="Protein_tab">Protein</div>
    </div>

    <div class="main-box">

        <#include "protein-header.ftl"/>
        <#include "protein-structure-body.ftl"/>
    </div>
</div>

    <#else>
    <#-- We have no information for the specified protein accession at all - not found -->
    <p>
        Protein accession not found.
    </p>
</#if>

</div>

</body>
</html>
