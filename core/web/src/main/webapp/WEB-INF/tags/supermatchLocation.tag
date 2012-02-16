<%@ attribute name="id" required="true" %>
<%@ attribute name="protein" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleProtein" %>
<%@ attribute name="supermatch" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleSuperMatch" %>
<%@ attribute name="colourClass" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" tagdir="/WEB-INF/tags" %>

<c:set var="title" value="${supermatch.type}"/>

<h:location id="supermatch-location-${id}"
            protein="${protein}"
            titlePrefix="${title}: "
            location="${supermatch.location}"
            colourClass="${colourClass}"/>

<div id="supermatch-popup-${id}" style="display: none;">
    <c:set var="currentLevel" value="0"/>
    <c:set var="depth" value="0"/>
    <%--<c:set var="first" value="true"/>--%>
    <c:forEach var="entry" items="${supermatch.entriesHierarchyOrder}">
    <c:if test="${depth eq 0 or (entry.hierarchyLevel != null and currentLevel < entry.hierarchyLevel)}">
    <ul class="hierarch">
        <c:set var="depth" value="${depth + 1}"/>
        </c:if>
        <li>
                <a href="http://wwwdev.ebi.ac.uk/interpro/IEntrySummary?ac=${entry.ac}" title="View entry ${entry.ac} on the InterPro website">${entry.name}</a> ${entry.ac}
        </li>
        <c:set var="currentLevel" value="${entry.hierarchyLevel}"/>
        </c:forEach>
        <c:forEach begin="0" end="${depth}">
    </ul>
    </c:forEach>
    ${supermatch.location.start} - ${supermatch.location.end}<br/>

</div>
