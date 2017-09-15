<#--
  This freemarker page contains code specific to the protein page for standalone InterProScan 5.
  Other code shared between the I5 HTML ouptut and the InterPro beta website is imported.
-->
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#--TODO: see http://html5boilerplate.com/-->
    <meta charset="utf-8" content="text/html">
<#--Check if protein exists-->
    <title>
    <#if protein?? && protein.ac?has_content>
    ${protein.ac} - InterPro
        <#else>
            Unknown protein - InterPro
    </#if>
    </title>
    <meta name="description" content="InterProScan result page: Protein matches and sequence features">
    <meta name="author" content="InterPro Team, European Bioinformatics Institute">


    <link href="${css_resource_type_colours}" rel="stylesheet" type="text/css"/>
    <link class="database" href="${css_resource_database}" rel="stylesheet" type="text/css"/>
    <link href="${css_resource_jquery_qtip2}" rel="stylesheet" type="text/css"/>
    <link href="${css_resource_protein}" rel="stylesheet" type="text/css"/>
    <script src="${js_resource_jquery171}" type="text/javascript"></script>
    <script src="${js_resource_jquery_ui1817_custom}" type="text/javascript"></script>
    <script src="${js_resource_jquery_qtip2}" type="text/javascript"></script>
    <script src="${js_resource_common}" type="text/javascript"></script>
    <script src="${js_resource_protein}" type="text/javascript"></script>
    <script src="${js_resource_protein_popups}" type="text/javascript"></script>
    <script src="${js_resource_protein_jquery_cookie}" type="text/javascript"></script>
    <script src="${js_resource_jquery_jscroll}" type="text/javascript"></script>

</head>
<body>
<div class="container_24">
<div class="grid_24 clearfix" id="content" >

<#if protein?? && protein.entries?has_content>
<#-- There are matches for this protein accession, therefore we can show the match filter menu -->
    <div class="grid_5 alpha">
        <#include "protein-menu.ftl"/>
    </div>
</#if>

<#if protein?? && protein.ac?has_content>
<#-- This protein accession exists, now start to display the data  -->
    <div class="grid_19 omega main-content">
        <div class="tab">
            <div class="Protein_tab">Protein</div>
        </div>

        <div class="main-box">
            <#if protein??>
                <h2 class="strapline">${protein.ac} </h2>
                <#include "protein-header.ftl"/>
            </#if>

            <#--NOTE: Can use import with absolute URLs, so could in theory include content from DBML to aid transition!-->
            <#include "protein-body.ftl"/>
        </div>
    </div>

    <#else>
    <#-- We have no information for the specified protein accession at all - not found -->
        <p>
            Protein accession not found.
        </p>
</#if>

</div>

<#if standalone>
    <div class="grid_19 omega main-content"><i>Generated with InterProScan ${interproscanVersion}</i></div>
</#if>
</div>
</body>
</html>
