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
	<link href="<c:url value="/resources/protein.css" />" rel="stylesheet"  type="text/css" />
    <link href="<c:url value="/resources/domain.css" />"  rel="stylesheet"  type="text/css" />
    <link class="database" href="<c:url value="/resources/database.css" />" rel="stylesheet"  type="text/css" />
    <script src="<c:url value="/resources/jquery/jquery-1.4.3.min.js"/>" type="text/javascript"></script>
    <script src="<c:url value="/resources/protein.js"/>" type="text/javascript"></script>
</head>
<body>
    <header>
        <nav>
            <div class="breadcrumb">
                <a href="<c:url value="/proteins"/>">Proteins</a> > ${protein.name} (${protein.ac})
            </div>
        </nav>
    </header>
    <div id="main" role="main" class="main-content">
        <div class="contents" id="contents">
            <%--NOTE: Can use import with absolute URLs, so could in theory include content from DBML to aid transition!--%>
            <c:import url="protein-body.jsp"/>
        </div>
    </div>
    <footer>

    </footer>
</body>
</html>