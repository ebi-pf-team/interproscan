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
                Structural features and predictions:
                <a href="<c:url value="/proteins/P38398"/>" title="Breast cancer type 1 susceptibility protein">BRCA1_HUMAN</a>
            </li>
            <li>
                MODBASE and SWISS-MODEL:
                <a href="<c:url value="/proteins/A0AIY7"/>" title="Glycerol kinase">GLPK_LISW6</a>
            </li>
            <li>
                No unintegrated signatures:
                <a href="<c:url value="/proteins/A0A314"/>" title="30S ribosomal protein S12, chloroplastic">RR12_COFAR</a>
            </li>
            <li>
                Active sites:
                <a href="<c:url value="/proteins/A5ITX5"/>" title="Serine protease splF">SPLF_STAA9</a>
            </li>
            <li>
                Binding sites:
                <a href="<c:url value="/proteins/A0MP03"/>" title="Myosin-Ic-A">MY1CA_XENLA</a>
            </li>
            <li>
                PTMs:
                <a href="<c:url value="/proteins/A0A1F4"/>" title="Protein eyes shut">EYS_DROME</a>
            </li>
            <li>
                Regions and conserved sites:
                <a href="<c:url value="/proteins/A2ARV4"/>" title="Low-density lipoprotein receptor-related protein 2">LRP2_MOUSE</a>
            </li>            
        </ul>
    </div>
    <footer>
        <%--Copyright ...--%>
    </footer>
</body>
</html>