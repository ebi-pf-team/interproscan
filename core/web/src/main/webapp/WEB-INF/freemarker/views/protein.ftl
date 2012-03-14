<!doctype html>
<html>
<head>
    <!--TODO: see http://html5boilerplate.com/-->
    <meta charset="utf-8">
<#--Checks if protein exists-->
<#if protein??>
    <title>${protein.name} (${protein.ac}) - InterPro</title>
    <#else>
    No data for this protein - InterPro
</#if>
    <meta name="description" content="Protein matches">
    <meta name="author" content="InterPro Team, European Bioinformatics Institute">
    <link href="${css_resource_jquery_qtip2}" rel="stylesheet" type="text/css"/>
    <link href="${css_resource_protein}" rel="stylesheet" type="text/css"/>
    <link href="${css_resource_type_colours}" rel="stylesheet" type="text/css"/>
    <link class="database" href="${css_resource_database}" rel="stylesheet" type="text/css"/>
    <script src="${js_resource_jquery171}" type="text/javascript"></script>
    <script src="${js_resource_jquery_qtip2}" type="text/javascript"></script>
    <script src="${js_resource_protein}" type="text/javascript"></script>
    <script src="${js_resource_jquery_jscroll}" type="text/javascript"></script>
</head>
<body>
<#if protein??>
    <header>
        <nav>
            <div class="breadcrumb">
                <a href="/proteins">Proteins</a> > ${protein.name} (${protein.ac})
            </div>
        </nav>
    </header>
    <div id="main" role="main" class="main-content">
        <div class="contents" id="contents">
            <!--NOTE: Can use import with absolute URLs, so could in theory include content from DBML to aid
            transition!-->
            <#include "protein-body.ftl"/>
        </div>
    </div>
    <footer>

    </footer>
<#else>
Sorry, no data found for this protein.
</#if>
<!-- JavaScript placed near the end </body> tag as this ensures the DOM is loaded before manipulation
of it occurs. This is not a requirement, simply a useful tip!

 Actually - the JQuery $(document).ready event is fired immediately after the DOM is loaded, so it doesn't matter
 where you put the script.  (You can also have as many $(document).ready events as you like in the same page,
 however there is no guarantee of the order in which they are run).

 http://docs.jquery.com/Tutorials:Introducing_$(document).ready()-->
<script type="text/javascript">
    $(document).ready(function() {
        // Match all <A/> links with a title tag and use it as the content (default).
        $('a[title]').qtip({
            position: {
                viewport: $(window) // Keep the tooltip on-screen at all times
            }
        });
        $('img[title]').qtip({
            position: {
                viewport: $(window) // Keep the tooltip on-screen at all times
            }
        });
        $('acronym[title]').qtip({
            position: {
                viewport: $(window) // Keep the tooltip on-screen at all times
            }
        });

        // Tie the extra popup DIV to it's match SPAN
        $('span[id*="location-"]').each(
                function(i) {
                    preparePopup(this.id);
                }
        );
    });
</script>
</body>
</html>
