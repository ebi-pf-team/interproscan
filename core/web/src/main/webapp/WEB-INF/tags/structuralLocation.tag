<%@ attribute name="id" required="true" %>
<%@ attribute name="protein"       required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleProtein" %>
<%@ attribute name="location" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleLocation" %>
<%@ attribute name="structuralMatchData" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleStructuralMatchData" %>
<%@ attribute name="databaseMetadata"   required="true" type="uk.ac.ebi.interpro.scan.web.model.MatchDataSource" %>
<%@ taglib prefix="h"  tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- Display structural features and predictions --%>

<c:set var="databaseName" value="${databaseMetadata.name}" />
<c:set var="title" value=""/>
<c:set var="links" value=""/>
<c:choose>
    <%-- locationDataMap = ${structuralMatchData.locationDataMap} --%>

    <%--Link on classId--%>
    <c:when test="${databaseName == 'CATH' || databaseName == 'SCOP' || databaseName == 'MODBASE'}">
        <c:forEach var="dataEntry" items="${structuralMatchData.locationDataMap}" varStatus="vs">
            <%-- classId = ${dataEntry.key} --%>
            <c:if test="${!vs.first}">
                <c:set var="title" value="${title}, "/>
            </c:if>
            <c:set var="title" value="${dataEntry.key}"/>
            <c:set var="links" value="${links}<a href='${fn:replace(databaseMetadata.linkUrl,'$0',dataEntry.key)}'  title='${title} (${databaseMetadata.name})' class='ext'>${dataEntry.key}</a><br/>"/>
        </c:forEach>
    </c:when>

    <%--Link on domainId--%>
    <c:when test="${databaseName == 'PDB' || databaseName == 'SWISS-MODEL'}">
        <c:forEach var="dataEntry" items="${structuralMatchData.locationDataMap}">
            <%-- classId = ${dataEntry.key} --%>
            <%-- domainIds = ${dataEntry.value} --%>
            <c:forEach var="domainId" items="${dataEntry.value}" varStatus="vs">
                <c:if test="${!vs.first}">
                    <c:set var="title" value="${title}, "/>
                </c:if>
                <c:set var="title" value="${domainId}"/>
                <c:set var="links" value="${links}<a href='${fn:replace(databaseMetadata.linkUrl, '$0', dataEntry.key)}'  title='${title} (${databaseMetadata.name})' class='ext'>${domainId}</a><br/>"/>
            </c:forEach>
        </c:forEach>
    </c:when>

    <c:otherwise>
        Unknown database: ${databaseName}.
    </c:otherwise>
</c:choose>

<h:location id="match-location-${id}"
            protein="${protein}"
            titlePrefix="${title}: "
            location="${location}"
            colourClass="${databaseName}"/>


<div id="match-popup-${id}" style="display: none;">

    <div class="popup_topl"><span class="${databaseName} caption_puce"></span>${location.start} - ${location.end}</div>
     <div class="popup_botl"><strong>${databaseMetadata.name}</strong> <img src="images/ico_help.png" alt="help" title="${databaseMetadata.description}" > <br/>
     ${links} <br/>

   </div>
    <%--(<a href="${databaseMetadata.homeUrl}"></a>)<br />--%>

</div>
