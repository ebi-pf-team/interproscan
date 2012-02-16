<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
<head>
    <%--TODO: see http://html5boilerplate.com/--%>
    <meta charset="utf-8">
    <c:choose>
        <c:when test="${protein == null}">
            No data for this protein - InterPro
        </c:when>
        <c:otherwise>
            <title>${protein.name} (${protein.ac}) - InterPro</title>
        </c:otherwise>
    </c:choose>
    <meta name="description" content="Protein matches">
    <meta name="author"      content="InterPro Team, European Bioinformatics Institute">
    <%--<link href="http://www.ebi.ac.uk/inc/css/contents.css"      rel="stylesheet" type="text/css" />--%>
    <%--<link href="http://www.ebi.ac.uk/inc/css/userstyles.css"    rel="stylesheet" type="text/css" />--%>
    <%--<link href="http://wwwdev.ebi.ac.uk/interpro/toolkits/interpro/interpro.css" rel="stylesheet" type="text/css" />--%>
    <link href="<c:url value="/resources/css/protein.css" />" rel="stylesheet"  type="text/css" />
    <link href="<c:url value="/resources/css/domain.css" />"  rel="stylesheet"  type="text/css" />
    <link class="database" href="<c:url value="/resources/css/database.css" />" rel="stylesheet"  type="text/css" />
    <link href="<c:url value="/resources/javascript/qtip2/jquery.qtip.css" />"  rel="stylesheet"  type="text/css" />
    <script src="<c:url value="/resources/javascript/jquery/jquery-1.7.1.min.js"/>" type="text/javascript"></script>
    <script src="<c:url value="/resources/javascript/qtip2/jquery.qtip.min.js"/>" type="text/javascript"></script>
    <script src="<c:url value="/resources/javascript/protein.js"/>" type="text/javascript"></script>
</head>
<body>

<header>
    <nav>
        <div class="breadcrumb">
            <a href="<c:url value="/protein-structures"/>">Protein structures</a> > ${protein.name} (${protein.ac})
        </div>
    </nav>
</header>
<div id="main" role="main" class="main-content">
    <div class="contents" id="contents">
        <%--NOTE: Can use import with absolute URLs, so could in theory include content from DBML to aid transition!--%>
        <c:import url="protein-structure-body.jsp"/>
    </div>
</div>
<footer>

</footer>

<!-- JavaScript placed near the end </body> tag as this ensures the DOM is loaded before manipulation
of it occurs. This is not a requirement, simply a useful tip! -->
<script type="text/javascript">
    // Match all <A/> links with a title tag and use it as the content (default).
    $('a[title]').qtip();

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
