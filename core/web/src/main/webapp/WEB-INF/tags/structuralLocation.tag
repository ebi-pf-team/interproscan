<%@ attribute name="id" required="true" %>
<%@ attribute name="protein"       required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleProtein" %>
<%@ attribute name="location" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleLocation" %>
<%@ attribute name="structuralMatchData" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleStructuralMatchData" %>
<%@ attribute name="databaseName"   required="true"  %>
<%@ taglib prefix="h"  tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="title" value=""/>
<c:set var="links" value=""/>
<c:choose>
    <%-- locationDataMap = ${structuralMatchData.locationDataMap} --%>

    <%-- Structural features --%>
    <c:when test="${databaseName == 'CATH'}">
        <c:forEach var="dataEntry" items="${structuralMatchData.locationDataMap}" varStatus="vs">
            <%-- classId = ${dataEntry.key} --%>
            <c:if test="${!vs.first}">
                <c:set var="title" value="${title}, "/>
            </c:if>
            <c:set var="title" value="${title}${dataEntry.key}"/>
            <c:set var="links" value="${links}<a href='http://www.cathdb.info/cathnode/${dataEntry.key}'>${dataEntry.key}</a><br/>"/>
        </c:forEach>
    </c:when>
    <c:when test="${databaseName == 'SCOP'}">
        <c:forEach var="dataEntry" items="${structuralMatchData.locationDataMap}" varStatus="vs">
            <%-- classId = ${dataEntry.key} --%>
            <c:if test="${!vs.first}">
                <c:set var="title" value="${title}, "/>
            </c:if>
            <c:set var="title" value="${title}${dataEntry.key}"/>
            <c:set var="links" value="${links}<a href='http://scop.mrc-lmb.cam.ac.uk/scop/search.cgi?key=${dataEntry.key}'>${dataEntry.key}</a><br/>"/>
        </c:forEach>
    </c:when>
    <c:when test="${databaseName == 'PDB'}">
        <c:forEach var="dataEntry" items="${structuralMatchData.locationDataMap}">
            <%-- classId = ${dataEntry.key} --%>
            <%-- domainIds = ${dataEntry.value} --%>
            <c:forEach var="domainId" items="${dataEntry.value}" varStatus="vs">
                <c:if test="${!vs.first}">
                    <c:set var="title" value="${title}, "/>
                </c:if>
                <c:set var="title" value="${title}${domainId}"/>
                <c:set var="links" value="${links}<a href='http://www.ebi.ac.uk/pdbe-srv/view/entry/${dataEntry.key}/summary'>${domainId}</a><br/>"/>
            </c:forEach>
        </c:forEach>
    </c:when>
    <%-- Structural predictions --%>
    <c:when test="${databaseName == 'MODBASE'}">
        <c:forEach var="dataEntry" items="${structuralMatchData.locationDataMap}" varStatus="vs">
            <%-- classId = ${dataEntry.key} --%>
            <c:if test="${!vs.first}">
                <c:set var="title" value="${title}, "/>
            </c:if>
            <c:set var="title" value="${title}${dataEntry.key}"/>
            <c:set var="links" value="${links}<a href='http://modbase.compbio.ucsf.edu/modbase-cgi-new/model_search.cgi?searchvalue=${protein.ac}&searchproperties=database_id&displaymode=moddetail&searchmode=default'>${dataEntry.key}</a><br/>"/>
        </c:forEach>
    </c:when>
    <c:when test="${databaseName == 'SWISSMODEL'}">
        <c:forEach var="dataEntry" items="${structuralMatchData.locationDataMap}">
            <%-- domainIds = ${dataEntry.value} --%>
            <c:forEach var="domainId" items="${dataEntry.value}" varStatus="vs">
                <c:if test="${!vs.first}">
                    <c:set var="title" value="${title}, "/>
                </c:if>
                <c:set var="title" value="${title}${domainId}"/>
                <c:set var="links" value="${links}<a href='http://swissmodel.expasy.org/repository/?pid=smr03&query_1_input=${protein.ac}'>${domainId}</a><br/>"/>
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
    ${links}<br />
    ${location.start} - ${location.end}<br />
</div>
