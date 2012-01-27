<%@ page import="uk.ac.ebi.interpro.scan.web.model.EntryType" %>
<%@ page import="uk.ac.ebi.interpro.scan.web.model.MatchDataSource" %>
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

<c:if test="${not empty protein.structuralFeatures}">
    <a name="structural-features"></a>
    <h3>Structural features</h3>
    <ol class="structural-features">
        <c:forEach var="match" items="${protein.structuralFeatures}">
            <li class="structural-feature">
                <p>
                    <c:choose>
                        <c:when test="${match.databaseName == CATH}">
                            <a href="http://www.cathdb.info/cathnode/${match.classId}">${match.classId}</a>
                        </c:when>
                        <c:when test="${match.databaseName == SCOP}">
                            <a href="http://scop.mrc-lmb.cam.ac.uk/scop/search.cgi?key=${match.classId}">${match.classId}</a>
                        </c:when>
                        <c:otherwise>
                            <a href="http://www.ebi.ac.uk/pdbe-srv/view/entry/${match.domainId}/summary">${match.domainId}</a>
                        </c:otherwise>
                    </c:choose>
                    (${match.databaseName})
                </p>
                <div class="match">
                    <c:forEach var="location" items="${match.locations}">
                        <%--TODO: Get background-color for match--%>
                        <h:location protein="${protein}"
                                    location="${location}"
                                    colourClass="${match.databaseName}"/>
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
        <c:forEach var="match" items="${protein.structuralPredictions}">
            <li class="structural-prediction">
                <p>
                    <c:choose>
                        <c:when test="${match.databaseName == MODBASE}">
                            <a href="http://modbase.compbio.ucsf.edu/modbase-cgi-new/model_search.cgi?searchvalue=${protein.ac}&searchproperties=database_id&displaymode=moddetail&searchmode=default">${match.classId}</a>
                        </c:when>
                        <c:otherwise>
                            <a href="http://swissmodel.expasy.org/repository/?pid=smr03&query_1_input=${protein.ac}">${match.domainId}</a>
                        </c:otherwise>
                    </c:choose>
                    (${match.databaseName})
                </p>
                <div class="match">
                    <c:forEach var="location" items="${match.locations}">
                        <%--TODO: Get background-color for match--%>
                        <h:location protein="${protein}"
                                    location="${location}"
                                    colourClass="${match.databaseName}"/>
                    </c:forEach>
                </div>
            </li>
        </c:forEach>
    </ol>
</c:if>

<%--Not sure why we need this break, but table gets right-aligned without it...--%>
<div><br/></div>
