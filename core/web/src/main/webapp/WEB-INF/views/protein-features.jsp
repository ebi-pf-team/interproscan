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
%>

<%--Returns protein features for inclusion in DBML--%>

<c:if test="${not empty protein.entries}">

    <div class="prot_sum">
            <%--<h1>Sequence summary</h1>--%>

                <div class="top-row">
                    <div class="top-row-id"><h1>Summary view</h1></div>
                    <div class="top-row-opt"><a href="#" title="Open sequence summary view in a new window"><span class="opt1"></span></a></div>
                </div>

                <div class="bot-row">
                    <div class="bot-row-line-top"></div>
                    <ol class="signatures">

                    <c:import url="condensed-view.jsp"/>

                    </ol>
                    <div class="bot-row-line-bot"></div>
                </div>

        <div class="prot_scale">
          <div class="bot-row">

                  <div class="bot-row-line">
                        <div style="position:relative;">
                        <!-- Position marker lines -->
                        <c:forTokens items="${scale}" delims="," var="scaleMarker">
                       <!-- to build an exception for 0 -->
                       <span class="scale_bar" style="left:${(scaleMarker / protein.length) * 100}%;" title="${scaleMarker}"></span>
                       <span class="scale_numb" style="left:${(scaleMarker / protein.length) * 100}%;">${scaleMarker}</span>
                       </c:forTokens>
                       </div>
             </div>

          </div>
     </div>
    </div>

    <div class="prot_entries">
        <h1>Sequence features</h1>
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

                <div class="top-row">
                    <div class="top-row-id"><img src="/interpro/images/ico_type_${icon}_small.png" alt="${title}" title="${title}"/> <a href="http://www.ebi.ac.uk/interpro/IEntry?ac=${entry.ac}">${entry.ac}</a> </div>
                    <div class="top-row-name"><a href="http://www.ebi.ac.uk/interpro/IEntry?ac=${entry.ac}" class="neutral">${entry.name}</a></div>
                </div>

                <div class="bot-row">
                    <div class="bot-row-line-top"></div>
                    <ol class="signatures"  style="border:0px solid pink;">

                        <c:forEach var="signature" items="${entry.signatures}">

                            <li id="${containerId}" class="signature entry-signatures" >
                                <h:signature protein="${protein}"
                                             signature="${signature}"
                                             entryTypeTitle="${title}"
                                             colourClass="${colourClass}"/>
                            </li>
                        </c:forEach>
                    </ol>
                    <div class="bot-row-line-bot"></div>
                </div>
                </li>
            </c:forEach>
        </ol>
    </div>
</c:if>

<c:if test="${not empty protein.unintegratedSignatures}">
    <div class="prot_entries" id="uni">
        <div class="top-row">
             <div class="top-row-id"><img src="/interpro/images/ico_type_uni_small.png" alt="Unintegrated signatures" title="Unintegrated signatures"/> no IPR </div>
             <div class="top-row-name">Unintegrated signatures</div>
        </div>
        <div class="bot-row">
            <div class="bot-row-line-top"></div>
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
            <div class="bot-row-line-bot"></div>
        </div>
    </div>
</c:if>

