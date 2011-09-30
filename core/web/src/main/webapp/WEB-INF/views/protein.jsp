<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<title>${protein.name} (${protein.ac}) - InterPro</title>
<head>
    <%--<link href="http://www.ebi.ac.uk/inc/css/contents.css"      rel="stylesheet" type="text/css" />--%>
    <%--<link href="http://www.ebi.ac.uk/inc/css/userstyles.css"    rel="stylesheet" type="text/css" />--%>
    <%--<link href="http://wwwdev.ebi.ac.uk/interpro/toolkits/interpro/interpro.css" rel="stylesheet" type="text/css" />--%>
    <style type="text/css">
        /*TODO: See "4. position:relative + position:absolute" [http://www.barelyfitz.com/screencast/html-training/css/positioning/]*/
        .match-line {
            border:         0.1em solid black;  /* Line height and colour */
            /*TODO: Try ex instead of px? [http://www.w3.org/TR/CSS21/syndata.html#length-units]*/
            width:          700px;              /* Relative units (%, em) not accurate enough - shame */
            z-index:        -1;                 /* Show underneath match */
        }
        .match {
            float:      left;
            height:     1em;
            top:        -0.5em;     /* Shows in middle of line (0.5 * match.height) */
            position:   relative;
        }        
        .match-gap {
            float:      left;
            margin-top: 1%;
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