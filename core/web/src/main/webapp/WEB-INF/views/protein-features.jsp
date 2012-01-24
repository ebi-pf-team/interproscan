<%@ page import="uk.ac.ebi.interpro.scan.web.model.EntryType" %>
<%@ page import="uk.ac.ebi.interpro.scan.web.model.MatchDataSource" %>
<%@ taglib prefix="h"  tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
    // Entry type names
    pageContext.setAttribute("FAMILY", uk.ac.ebi.interpro.scan.web.model.EntryType.FAMILY.toString());
    pageContext.setAttribute("DOMAIN", uk.ac.ebi.interpro.scan.web.model.EntryType.DOMAIN.toString());
    pageContext.setAttribute("REPEAT", uk.ac.ebi.interpro.scan.web.model.EntryType.REPEAT.toString());
    pageContext.setAttribute("REGION", uk.ac.ebi.interpro.scan.web.model.EntryType.REGION.toString());
    pageContext.setAttribute("UNKNOWN", uk.ac.ebi.interpro.scan.web.model.EntryType.UNKNOWN.toString());

    // Data source names
    pageContext.setAttribute("CATH", uk.ac.ebi.interpro.scan.web.model.MatchDataSource.CATH.toString());
    pageContext.setAttribute("SCOP", uk.ac.ebi.interpro.scan.web.model.MatchDataSource.SCOP.toString());
    pageContext.setAttribute("MODBASE", uk.ac.ebi.interpro.scan.web.model.MatchDataSource.MODBASE.toString());
%>

<%--Returns protein features for inclusion in DBML--%>

<c:if test="${not empty protein.entries}">
    <a name="domains-sites"></a>
    <h3>Families, domains, repeats and sites</h3>
    <div id="section-domains-sites">
        <div class="entry-signatures">
            <input id="database" name="colour" type="checkbox" value="database" />
            <label for="database">Colour signature matches by database name</label>
        </div>
        <ol class="entries">
        <c:forEach var="entry" items="${protein.entries}">
            <c:set var="icon">
                <c:choose>
                    <%--TODO: Use enum--%>
                    <c:when test="${entry.type == FAMILY or entry.type == DOMAIN or
                                    entry.type == REGION or entry.type == REPEAT}">
                        ${fn:toLowerCase(entry.type)}
                    </c:when>
                    <%--Use unintegrated signature icon if unknown (probably needs own icon)--%>
                    <c:when test="${entry.type == UNKNOWN}">
                        uni
                    </c:when>
                    <c:otherwise>
                        site
                    </c:otherwise>
                </c:choose>
            </c:set>
            <c:set var="title" value="${fn:replace(entry.type, '_', ' ')}"/>
            <c:set var="colourClass">
                <c:choose>
                    <c:when test="${entry.type == DOMAIN}">
                        c${entryColours[entry.ac]} ${entry.type}
                    </c:when>
                    <c:when test="${entry.type == REPEAT}">
                        c${entryColours[entry.ac]} ${entry.type}
                    </c:when>
                    <c:otherwise>
                        ${entry.type}
                    </c:otherwise>
                </c:choose>
            </c:set>
            <c:set var="containerId" value="${entry.ac}-signatures"/>
                <li class="entry">
                    <p>
                        <%-- Use InterPro 5.2 image paths for now (see mvc-config.xml) --%>
                        <%-- Better to pass in param from DBML instead so can use normal resource: --%>
                        <%--<c:url value="/resources/images/ico_type_uni_small.png"/>--%>
                        <img src="/interpro/images/ico_type_${icon}_small.png" alt="${title}" title="${title}"/>
                        <a href="IEntry?ac=${entry.ac}" title="${title}">${entry.name}</a> (${entry.ac})
                    </p>
                    <div class="match">
                        <c:forEach var="location" items="${entry.locations}">
                            <h:location protein="${protein}" location="${location}" colourClass="${colourClass}"/>
                        </c:forEach>
                    </div>
                    <ol class="signatures">
                        <%--Add show/hide button--%>
                        <%--TODO: Figure out how to do show/hide for individual entries without interference with show/hide for all --%>
                        <%--<script type="text/javascript">--%>
                            <%--createSingleEntryShowHideButton('${containerId}');--%>
                        <%--</script>--%>
                        <c:forEach var="signature" items="${entry.signatures}">
                        <li id="${containerId}" class="signature entry-signatures">
                            <h:signature protein="${protein}"
                                         signature="${signature}"
                                         entryTypeIcon="${icon}"
                                         entryTypeTitle="${title}"
                                         colourClass="${colourClass}"/>
                        </li>
                        </c:forEach>
                    </ol>
                </li>
        </c:forEach>
        </ol>
    </div>
</c:if>

<c:if test="${not empty protein.unintegratedSignatures}">
    <a name="unintegrated-signatures"></a>
    <h3>Unintegrated signatures</h3>
    <ol class="signatures">
    <c:forEach var="signature" items="${protein.unintegratedSignatures}">
        <li class="signature">
            <h:signature protein="${protein}"
                         signature="${signature}"
                         entryTypeIcon="uni"
                         entryTypeTitle="Unintegrated"
                         colourClass="uni"/>
        </li>
    </c:forEach>
    </ol>
</c:if>

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
