<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
<head>
    <%--TODO: see http://html5boilerplate.com/--%>
    <meta charset="utf-8">
    <title>${protein.name} (${protein.ac}) - InterPro</title>
    <meta name="description" content="Protein matches">
    <meta name="author"      content="InterPro Team, European Bioinformatics Institute">
    <%--<link href="http://www.ebi.ac.uk/inc/css/contents.css"      rel="stylesheet" type="text/css" />--%>
    <%--<link href="http://www.ebi.ac.uk/inc/css/userstyles.css"    rel="stylesheet" type="text/css" />--%>
    <%--<link href="http://wwwdev.ebi.ac.uk/interpro/toolkits/interpro/interpro.css" rel="stylesheet" type="text/css" />--%>
    <script src="http://wwwdev.ebi.ac.uk/interpro/js/jquery/jquery-1.4.3.min.js" type="text/javascript"></script>
    <script type="text/javascript">
        $(document).ready(function() {

            // Set up the button to show and hide all signatures
            var toggleAllId = "all-signatures-toggle";
            var toggleAllClass = ".entry-signatures";
            var showAllText = "Show all signatures »";            
            var hideAllText = "Hide all signatures «";
            // Add button to show and hide signatures
            $("<button id='" + toggleAllId + "'></button>").prependTo("#section-domains-sites");
            var toggleAllButton = $("#" + toggleAllId);
            toggleAllButton.text(showAllText);
            // Hide signatures after 0 milliseconds -- leaves visible if JavaScript off or not available
            $(toggleAllClass).slideToggle(0);
            // Show or hide all signature matches
            toggleAllButton.click(function () {
                //console.log("clicked");
                var delay = 400; // milliseconds
                $(toggleAllClass).slideToggle(delay);
                // Togglw
                setTimeout(function() {
                    var s = (toggleAllButton.text() == showAllText ? hideAllText : showAllText);
                    toggleAllButton.text(s);
                }, delay / 2);
            });
            // TODO: Toggle individual sections based on entry ac
//            $("#toggle-entry-signatures").click(function () {
//                $(".entry-signatures").slideToggle("slow", function () {
//                    alert("1");
//                    var button = $("#toggle-entry-signatures");
//                    $(this).is(":visible") ? button.html("Hide signatures «") : button.html("Show signatures »");
//                });
//            });
            
        });
    </script>
    <style type="text/css">
        abbr {
            border-bottom: 1px dotted black;
            cursor:        help;
        }
        div.entry {
            background-color: #f9f9f9;     
        }
        /* Line for entire length of protein sequence */
        div.match {
            border:     0.1em solid black;  /* Line height and colour */
            position:   relative;
            width:      95%;
        }
        /* Individual match */
        span.match {
            border: 0.1em solid black;
            /* Round edges: */
            border-radius:          4px;
            -moz-border-radius:     4px;
            -khtml-border-radius:   4px;
            -webkit-border-radius:  4px;
            height:     0.5em;
            position:   absolute;
            top:        -0.3em;             /* Shows in middle of line (0.5 * match.height plus a bit of judgment by eye) */
            z-index:    1;                  /* Show above match line */
        }
        /* Colours */
        span.c-entry {
            background-color: #ff9999;
        }
        span.c-signature {
            background-color: #aa9999;
        }
        span.c-signature-pfam, span.c-signature-pfamb {
            background-color: #ff721d;
        }        
        span.c-structure {
            background-color: #cc9999;
        }
        span.c-entry:hover, span.c-structure:hover, span.c-signature:hover {
            /* Enlarge slightly: */
            height:     0.7em;
            top:        -0.4em;
            /* Show overlaps */
            z-index:    5;
            /* Make it glow: */
            box-shadow:         0 0 0.5em rgba(0, 0, 255, 1);
            -webkit-box-shadow: 0 0 0.5em rgba(0, 0, 255, 1);
            -moz-box-shadow:    0 0 0.5em rgba(0, 0, 255, 1);
        }        
        table.match {
            border-width:   1px;
            border-style:   solid;
            border-color:   gray;
            border-collapse: collapse;
            display: none;
        }
        table.match th {
            background-color: silver;
            border-width:   1px;
            padding:        2px;
            border-style:   solid;
            border-color:   gray;
        }
        table.match td {
            border-width:   1px;
            padding:        2px;
            border-style:   solid;
            border-color:   gray;
        }
        table.match tr.entry {
            background-color: #dcdcdc;
        }
    </style>
</head>
<body>
    <header>
        <nav>
            <%--<ol id="toc">--%>
                <%--<li><a href="#domains-sites">Domains and sites</a></li>--%>
                <%--<li><a href="#unintegrated-signatures">Unintegrated signatures</a></li>--%>
                <%--<li><a href="#structural-features">Structural features</a></li>--%>
                <%--<li><a href="#structural-predictions">Structural predictions</a></li>--%>
            <%--</ol>--%>
        </nav>
    </header>
    <div id="main" role="main" class="main-content">
        <div class="contents" id="contents">
            <%--TODO: Include protein-features.jsp - just for testing at moment--%>
            <%--NOTE: Can use import with absolute URLs, so could in theory include content from DBML to aid transition!--%>
            <c:import url="protein-body.jsp"/>
        </div>
    </div>
    <footer>

    </footer>
</body>
</html>
