<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--Returns protein features for inclusion in DBML--%>

<h3>Domains and sites</h3>
<%--TODO: Show graphics instead of table (will show table in future) - see --%>
<table>
    <tr>
        <th>Signature</th>
        <th>Start</th>
        <th>End</th>
    </tr>
    <c:forEach var="match" items="${protein.matches}">
        <%--TODO: Sort on location.start, either in model code by implementing Comparable [1], or by returning a holder--%>
        <%--TODO: from the Controller with the separate objects we need for each section? --%>
        <%--TODO: [1] http://forums.devshed.com/java-help-9/sorting-with-jstl-340581.html--%>
        <c:forEach var="location" items="${match.locations}">
            <tr>
                <td>${match.signature.name} (${match.signature.accession})</td>
                <td>${location.start}</td>
                <td>${location.end}</td>
            </tr>
        </c:forEach>
    </c:forEach>
</table>

<h3>Structural predictions</h3>
TODO
<%--TODO: Do we to do anything special here?--%>