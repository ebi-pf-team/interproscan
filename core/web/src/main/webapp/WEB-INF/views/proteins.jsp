<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
<head>
    <title>Proteins - InterPro</title>
</head>
<body>
    <header>
        <nav>
            <%--<ol id="toc">--%>
                <%--<li><a href="/">Home</a></li>--%>
            <%--</ol>--%>
        </nav>
    </header>
    <div id="main">
        <h1>Proteins</h1>
        <ul>
            <li>
                <a href="<c:url value="/proteins/P12345"/>" title="Aspartate aminotransferase, mitochondrial">AATM_RABIT</a>
            </li>
            <li>
                <a href="<c:url value="/proteins/P38398"/>" title="Breast cancer type 1 susceptibility protein">BRCA1_HUMAN</a>
            </li>
            <li>
                <a href="<c:url value="/proteins/P99999"/>" title="Cytochrome c">CYC_HUMAN</a>
            </li>
            <li>
                <a href="<c:url value="/proteins/A0AIY7"/>" title="Glycerol kinase">GLPK_LISW6</a>
            </li>
            <li>
                <a href="<c:url value="/proteins/A0A314"/>" title="30S ribosomal protein S12, chloroplastic">RR12_COFAR</a>
            </li>            
        </ul>
    </div>
    <footer>
        <%--Copyright ...--%>
    </footer>
</body>
</html>