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
     <div class="popup_topl"><span class="${colourClass} caption_puce"></span>${supermatch.location.start} - ${supermatch.location.end}</div>
    <c:forEach var="entry" items="${supermatch.entriesHierarchyOrder}">
    <c:if test="${depth eq 0 or (entry.hierarchyLevel != null and currentLevel < entry.hierarchyLevel)}">
        <c:if test="${depth eq 0}">

        </c:if>
        <ul>
        <c:set var="depth" value="${depth + 1}"/>
        </c:if>
        <li>
      <a href="http://www.ebi.ac.uk/interpro/IEntry?ac=${entry.ac}" class="neutral" title=" ${entry.name} (${entry.ac})">${entry.name} <span>(${entry.ac})</span></a>
        </li>
        <c:set var="currentLevel" value="${entry.hierarchyLevel}"/>
        </c:forEach>
        <c:forEach begin="0" end="${depth}">
    </ul>
    </c:forEach>

</div>
