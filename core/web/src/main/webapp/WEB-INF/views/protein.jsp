<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<title>${protein.name} (${protein.ac}) - InterPro</title>
<head>
    <%--<link href="http://www.ebi.ac.uk/inc/css/contents.css"      rel="stylesheet" type="text/css" />--%>
    <%--<link href="http://www.ebi.ac.uk/inc/css/userstyles.css"    rel="stylesheet" type="text/css" />--%>
    <%--<link href="http://wwwdev.ebi.ac.uk/interpro/toolkits/interpro/interpro.css" rel="stylesheet" type="text/css" />--%>
    <style type="text/css">
        div.match {
            /* Line for entire length of protein sequence */
            border:     0.1em solid black;  /* Line height and colour */
            position:   relative;
            width:      95%;
        }
        span.match {
            /* Individual match */
            height:     1em;
            position:   absolute;
            top:        -0.5em;             /* Shows in middle of line (0.5 * match.height) */
            z-index:    1;                  /* Show above match line */
        }
        table.match {
            border-width:   1px;
            border-style:   solid;
            border-color:   gray;
            border-collapse: collapse;
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

<div class="main-content">

<div class="contents" id="contents">

<%--TODO: Include protein-features.jsp - just for testing at moment--%>
<%--NOTE: Can use import with absolute URLs, so could in theory include content from DBML to aid transition!--%>
<c:import url="protein-body.jsp"/>

</div>

</div>

</body>
</html>