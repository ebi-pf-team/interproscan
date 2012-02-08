<%@ page import="uk.ac.ebi.interpro.scan.web.model.EntryType" %>
<%@ page import="uk.ac.ebi.interpro.scan.web.model.MatchDataSource" %>
<%@ page import="uk.ac.ebi.interpro.scan.web.model.SimpleLocation" %>
<%@ taglib prefix="h"  tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
    // Entry type names
//    pageContext.setAttribute("FAMILY", uk.ac.ebi.interpro.scan.web.model.EntryType.FAMILY.toString());
//    pageContext.setAttribute("DOMAIN", uk.ac.ebi.interpro.scan.web.model.EntryType.DOMAIN.toString());
//    pageContext.setAttribute("REPEAT", uk.ac.ebi.interpro.scan.web.model.EntryType.REPEAT.toString());
//    pageContext.setAttribute("REGION", uk.ac.ebi.interpro.scan.web.model.EntryType.REGION.toString());
//    pageContext.setAttribute("UNKNOWN", uk.ac.ebi.interpro.scan.web.model.EntryType.UNKNOWN.toString());

    // Data source names
    pageContext.setAttribute("CATH", uk.ac.ebi.interpro.scan.web.model.MatchDataSource.CATH.toString());
    pageContext.setAttribute("SCOP", uk.ac.ebi.interpro.scan.web.model.MatchDataSource.SCOP.toString());
    pageContext.setAttribute("MODBASE", uk.ac.ebi.interpro.scan.web.model.MatchDataSource.MODBASE.toString());
%>

<p>
    <b>TODO: Supermatch summary view goes here!</b>
</p>

<%--Returns protein structure features for inclusion in DBML--%>

<c:set var="locationId" value="0" scope="request"/>

<c:if test="${not empty protein.structuralFeatures}">
    <a name="structural-features"></a>
    <h3>Structural features</h3>
    <ol class="structural-features">
        <c:forEach var="database" items="${protein.structuralFeatures}">
            <li class="structural-feature">

                <!--
                    Structural match data structure:

                    database (SimpleStructuralMatchDatabase)
                    -- databaseName (String)
                    -- structuralMatches (Map<SimpleLocation, SimpleStructuralMatchData>)

                    location (SimpleLocation)
                    -- start (int)
                    -- end (int)

                    locationData (SimpleStructuralMatchData)
                    -- locationDataMap (Map<String, List<String>>)
                -->
                <c:set var="databaseName">${database.databaseName}</c:set>
                    ${databaseName}
                <div class="match">
                    <c:forEach var="structuralMatch" items="${database.structuralMatches}">
                        <%-- location = ${structuralMatch.key} --%>
                        <%-- structuralMatchData = ${structuralMatch.value} --%>
                        <c:set var="locationId" value="${locationId + 1}" />
                        <h:structuralLocation id="${locationId}"
                                              protein="${protein}"
                                              location="${structuralMatch.key}"
                                              structuralMatchData="${structuralMatch.value}"
                                              databaseName="${databaseName}"/>
                    </c:forEach>

                </div>
            </li>
        </c:forEach>
    </ol>
</c:if>

<c:if test="${not empty protein.structuralPredictions}">
    <a name="structural-predictions"></a>
    <h3>Structural predictions</h3>
    <ol class="structural-predictions">
        <c:forEach var="database" items="${protein.structuralPredictions}">
            <li class="structural-prediction">
                <!--
                    Structural match data structure:

                    database (SimpleStructuralMatchDatabase)
                    -- databaseName (String)
                    -- structuralMatches (Map<SimpleLocation, SimpleStructuralMatchData>)

                    location (SimpleLocation)
                    -- start (int)
                    -- end (int)

                    locationData (SimpleStructuralMatchData)
                    -- locationDataMap (Map<String, List<String>>)
                -->
                <c:set var="databaseName">${database.databaseName}</c:set>
                    ${databaseName}
                <div class="match">
                    <c:forEach var="structuralMatch" items="${database.structuralMatches}">
                        <%-- location = ${structuralMatch.key} --%>
                        <%-- structuralMatchData = ${structuralMatch.value} --%>
                        <c:set var="locationId" value="${locationId + 1}" />
                        <h:structuralLocation id="${locationId}"
                                              protein="${protein}"
                                              location="${structuralMatch.key}"
                                              structuralMatchData="${structuralMatch.value}"
                                              databaseName="${databaseName}"/>
                    </c:forEach>

                </div>
            </li>
        </c:forEach>
    </ol>
</c:if>
