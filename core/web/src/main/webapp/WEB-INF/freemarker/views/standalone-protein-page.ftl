<#--
  Created by IntelliJ IDEA.
  User: pjones
  Date: 08/02/12
  Time: 15:00
  To change this template use File | Settings | File Templates.
-->
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <#--TODO: see http://html5boilerplate.com/-->
    <meta charset="utf-8" content="text/html">
    <#--Check if protein exists-->
    <#if protein??>
        <title>${protein.name} (${protein.ac}) - InterPro</title>
    <#else>
        No data for this protein - InterPro
    </#if>
    <meta name="description" content="InterProScan result page: Protein matches and sequence features">
    <meta name="author" content="InterPro Team, European Bioinformatics Institute">


    <link href="${css_resource_type_colours}" rel="stylesheet" type="text/css"/>
    <link class="database" href="${css_resource_database}" rel="stylesheet" type="text/css"/>
    <link href="${css_resource_jquery_qtip2}" rel="stylesheet" type="text/css"/>
    <link href="${css_resource_protein}" rel="stylesheet" type="text/css"/>
    <link href="${css_resource_jquery_ui1817_custom}" rel="stylesheet" type="text/css"/>
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
<div class="contentsarea">

    <div class="left-menu">
        <#include "protein-menu.ftl"/>
    </div>

    <div class="main-content">
        <#if protein??>
            <#--<header>-->
                <#--<nav>-->
                <#--<div class="breadcrumb">-->
                <#--<a href="/proteins">Proteins</a> > ${protein.name} (${protein.ac})-->
                <#--</div>-->
                <#--</nav>-->
                <#--</header>-->

                <#--NOTE: Can use import with absolute URLs, so could in theory include content from DBML to aid transition!-->
                <#include "protein-body.ftl"/>
        <#else>
            Sorry, no data found for this protein.
        </#if>
    </div>
</div>
<script type="text/javascript">
    $(document).ready(function() {
        // Read colour preference from cookie (requires http://plugins.jquery.com/project/Cookie)

        // Retrieve existing cookies and set checkbox states accordingly
        var checkBoxIDs = ["#check-2", "#check-3", "#check-4", "#check-5", "#check-6"];
        for (i = 0; i < checkBoxIDs.length; i++) {
            var checkBoxId = checkBoxIDs[i];
            var cookieVal = $.cookie(checkBoxId);
            if (cookieVal != null) {
                $(checkBoxId).prop("checked", cookieVal == "true");
            }
        }

        // and the radio button group
        var radioCookieVal = $.cookie("colour-by-domain");
        if (radioCookieVal != null) {
            if (radioCookieVal == "true") {
                $('#domainColour').prop("checked", true);
            }
            else {
                $('#databaseColour').prop("checked", true);
            }
        }

        // Keep the filter menu in view
        $("#menu").jScroll();
        // CSS switching
        configureStylesheets($('input[name="blobColour"]:checked').attr('id') == 'domainColour'); // initialise

        $('input[name="blobColour"]').change(function() {
            configureStylesheets($('input[name="blobColour"]:checked').attr('id') == 'domainColour');
        });

        // Change event for type checkboxes (Family, Domain etc.)
        $(".type").change(function() {
            displayType(this);
        });

        // Initialise types
        $(".type").each(function() {
            displayType(this);
        });

        // Change event for un-integrated sig matches checkbox
        $("#check-6").change(function() {
            displayUnintegrated(this);
        });

        // Initialise un-integrated.
        displayUnintegrated($("#check-6"));

        $('span[id*="location-"]').each(
                function(i) {
                    preparePopup(this.id);
                }
        );
    });
</script>
</body>
</html>