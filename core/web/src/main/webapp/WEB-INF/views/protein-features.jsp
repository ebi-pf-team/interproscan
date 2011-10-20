<%@ page import="uk.ac.ebi.interpro.scan.web.model.MatchDataSources" %>
<%@ taglib prefix="h"  tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% pageContext.setAttribute("MODBASE", uk.ac.ebi.interpro.scan.web.model.MatchDataSources.MODBASE.toString()); %>

<%--Returns protein features for inclusion in DBML--%>

<%--TODO: Easier if SimpleProtein returns separate collections for each section, even if based on two underlying collections?--%>

<a name="domains-sites"></a>
<h3>Domains and sites</h3>
<div id="section-domains-sites">
<c:forEach var="entry" items="${protein.entries}">
    <c:if test="${not empty entry.type}">
        <c:set var="icon">
            <c:choose>
                <c:when test="${entry.type == 'Family' or entry.type == 'Domain' or
                                entry.type == 'Region' or entry.type == 'Repeat'}">
                    ${fn:toLowerCase(entry.type)}
                </c:when>
                <c:otherwise>
                    site
                </c:otherwise>
            </c:choose>
        </c:set>
        <c:set var="title" value="${fn:replace(entry.type, '_', ' ')}"/>
        <div class="entry">
            <p>
                <%-- Use InterPro 5.2 image paths for now (see mvc-config.xml) --%>
                <%-- Better to pass in param from DBML instead so can use normal resource: --%>
                <%--<c:url value="/resources/images/ico_type_uni_small.png"/>--%>
                <img src="/interpro/images/ico_type_${icon}_small.png" alt="${title}" title="${title}"/>
                <a href="IEntry?ac=${entry.ac}" title="${title}">${entry.name}</a> (${entry.ac})
            </p>
            <%--<p>--%>
                <%--${entryColours[entry.ac]}--%>
            <%--</p>--%>
            <div class="match">
                <c:forEach var="location" items="${entry.locations}">
                    <%--TODO: Get class for background colour--%>
                    <h:location protein="${protein}" location="${location}" colourClass="c-entry"/>
                </c:forEach>
            </div>
            <%--Add show/hide button--%>
            <c:set var="containerId" value="${entry.ac}-signatures"/>
            <%--TODO: Figure out how to do show/hide for individual entries without interference with show/hide for all --%>
            <%--<script type="text/javascript"> --%>
                <%--createSingleEntryShowHideButton('${containerId}');--%>
            <%--</script>--%>
            <div id="${containerId}" class="entry-signatures">
                <c:forEach var="signature" items="${entry.signatures}">
                    <h:signature protein="${protein}"
                                 signature="${signature}"
                                 entryTypeIcon="${icon}"
                                 entryTypeTitle="${title}"/>
                </c:forEach>
            </div>
            <%--Not sure why we need this break, but next entry gets messed up without it --%>
            <br/>
        </div>
    </c:if>
</c:forEach>
</div>

<a name="unintegrated-signatures"></a>
<h3>Unintegrated signatures</h3>
<c:forEach var="entry" items="${protein.entries}">
    <c:if test="${empty entry.type}">
    <div>
        <c:forEach var="signature" items="${entry.signatures}">
            <h:signature protein="${protein}"
                         signature="${signature}"                         
                         entryTypeIcon="uni"
                         entryTypeTitle="Unintegrated"/>
        </c:forEach>
    </div>
    </c:if>
</c:forEach>

<c:if test="${not empty protein.structuralFeatures}">

</c:if>

<c:if test="${not empty protein.structuralFeatures}">
    <a name="structural-features"></a>
    <h3>Structural features</h3>
    <c:forEach var="match" items="${protein.structuralFeatures}">
        <div>
            <p>
                <c:choose>
                    <%--TODO: Get dbnames from enum? (so can share with SimpleProtein.getStructuralFeatures)--%>
                    <c:when test="${match.databaseName == 'CATH'}">
                        <a href="http://www.cathdb.info/cathnode/${match.classId}">${match.classId}</a>
                    </c:when>
                    <c:when test="${match.databaseName == 'SCOP'}">
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
                                colourClass="c-structure"/>
                </c:forEach>
            </div>
            <%--Not sure why we need this break, but next entry gets messed up without it --%>
            <br/>
        </div>
    </c:forEach>
</c:if>

<c:if test="${not empty protein.structuralPredictions}">
    <a name="structural-predictions"></a>
    <h3>Structural predictions</h3>
    <c:forEach var="match" items="${protein.structuralPredictions}">
        <div>
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
                                colourClass="c-structure"/>
                </c:forEach>
            </div>
            <%--Not sure why we need this break, but next entry gets messed up without it --%>
            <br/>
        </div>
    </c:forEach>
</c:if>    

<%--Not sure why we need this break, but table gets right-aligned without it...--%>
<div><br/></div>

<%--TODO: Could use HTML5 Canvas to highlight matches in graphic and table when hover over--%>
<table class="match">
    <tr>
        <th>Entry</th>
        <th>Signature</th>
        <th>Start</th>
        <th>End</th>
    </tr>
    <c:forEach var="entry" items="${protein.entries}">
        <c:forEach var="location" items="${entry.locations}">
            <tr class="entry">
                <td><a href="IEntry?ac=${entry.ac}">${entry.name}</a> (${entry.ac})</td>
                <td></td>
                <td align="right">${location.start}</td>
                <td align="right">${location.end}</td>
            </tr>
            <c:forEach var="signature" items="${entry.signatures}">
                <c:forEach var="location" items="${signature.locations}">
                    <tr>
                        <td></td>
                        <td><a href="ISignature?ac=${signature.ac}">${signature.name}</a> (${signature.ac})</td>
                        <td align="right">${location.start}</td>
                        <td align="right">${location.end}</td>
                    </tr>
                </c:forEach>
            </c:forEach>
        </c:forEach>
    </c:forEach>
</table>