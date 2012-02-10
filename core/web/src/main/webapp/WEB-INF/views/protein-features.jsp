<%@ taglib prefix="h" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

    <p>
        <c:import url="condensed-view.jsp"/>
    </p>

    <a name="domains-sites"></a>

    <h3>Families, domains, repeats and sites</h3>

    <div id="section-domains-sites">
        <!-- Scale markers -->
        <div style="width: 92%; position: relative;">
            <!-- Position labels -->
            <c:forTokens items="${scale}" delims="," var="scaleMarker">
                <span style="left:  ${(scaleMarker / protein.length) * 100}%; position: absolute;">
                        ${scaleMarker}
                </span>
            </c:forTokens>
        </div>
        <br/>

        <div class="scale">
            <!-- Position marker lines -->
            <c:forTokens items="${scale}" delims="," var="scaleMarker">
            <span class="scale"
                  style="left:  ${(scaleMarker / protein.length) * 100}%;"
                  title="${scaleMarker}">
            </span>
            </c:forTokens>
        </div>

        <ol class="entries">
            <c:forEach var="entry" items="${protein.entries}">
                <!-- Prepare required variables for this entry -->
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

                <!-- Now display the entry on the page using these variables -->
                <li class="entry ${entry.type}-row">
                    <p>
                            <%-- Use InterPro 5.2 image paths for now (see mvc-config.xml) --%>
                            <%-- Better to pass in param from DBML instead so can use normal resource: --%>
                            <%--<c:url value="/resources/images/ico_type_uni_small.png"/>--%>
                        <img src="/interpro/images/ico_type_${icon}_small.png" alt="${title}" title="${title}"/>
                        <a href="IEntry?ac=${entry.ac}" title="${title}">${entry.ac}</a> ${entry.name}
                    </p>
                    <ol class="signatures">
                        <c:forEach var="signature" items="${entry.signatures}">
                            <li id="${containerId}" class="signature entry-signatures">
                                <h:signature protein="${protein}"
                                             signature="${signature}"
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
    <div id="unintegrated">
        <a name="unintegrated-signatures"></a>
        <img src="/interpro/images/ico_type_uni_small.png" alt="Unintegrated signatures"
             title="Unintegrated signatures"/> no IPR
        Unintegrated signatures
        <ol class="signatures">
            <c:forEach var="signature" items="${protein.unintegratedSignatures}">
                <li class="signature">
                    <h:signature protein="${protein}"
                                 signature="${signature}"
                                 entryTypeTitle="Unintegrated"
                                 colourClass="uni"/>
                </li>
            </c:forEach>
        </ol>
    </div>
</c:if>

<%--Not sure why we need this break, but table gets right-aligned without it...--%>
<div><br/></div>

<%--TODO: Could use HTML5 Canvas to highlight matches in graphic and table when hover over--%>
<%--<table class="match">--%>
<%--<tr>--%>
<%--<th>Entry</th>--%>
<%--<th>Signature</th>--%>
<%--<th>Start</th>--%>
<%--<th>End</th>--%>
<%--</tr>--%>
<%--<c:forEach var="entry" items="${protein.entries}">--%>
<%--<c:forEach var="location" items="${entry.locations}">--%>
<%--<tr class="entry">--%>
<%--<td><a href="IEntry?ac=${entry.ac}">${entry.name}</a> (${entry.ac})</td>--%>
<%--<td></td>--%>
<%--<td align="right">${location.start}</td>--%>
<%--<td align="right">${location.end}</td>--%>
<%--</tr>--%>
<%--<c:forEach var="signature" items="${entry.signatures}">--%>
<%--<c:forEach var="location" items="${signature.locations}">--%>
<%--<tr>--%>
<%--<td></td>--%>
<%--<td><a href="ISignature?ac=${signature.ac}">${signature.name}</a> (${signature.ac})</td>--%>
<%--<td align="right">${location.start}</td>--%>
<%--<td align="right">${location.end}</td>--%>
<%--</tr>--%>
<%--</c:forEach>--%>
<%--</c:forEach>--%>
<%--</c:forEach>--%>
<%--</c:forEach>--%>
<%--</table>--%>
