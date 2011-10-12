<%@ taglib prefix="h"  tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--Returns protein features for inclusion in DBML--%>

<a name="domains-sites"></a>
<h3>Domains and sites</h3>
<c:forEach var="entry" items="${protein.entries}">
    <c:if test="${not empty entry.type}">
    <div>
        <p><a href="IEntry?ac=${entry.ac}">${entry.name}</a> (${entry.ac})</p>
        <div class="match">
            <c:forEach var="location" items="${entry.locations}">
                <%--TODO: Get background-color for match--%>
                <h:match proteinLength="${protein.length}"
                         start="${location.start}"
                         end="${location.end}"
                         colour="#ff9999"/>
            </c:forEach>
        </div>
        <%--Not sure why we need this break, but next entry gets messed up without it --%>
        <br/>
    </div>
    </c:if>
</c:forEach>

<a name="unintegrated-signatures"></a>
<h3>Unintegrated signatures</h3>
<c:forEach var="entry" items="${protein.entries}">
    <c:if test="${empty entry.type}">
    <div>
        <c:forEach var="signature" items="${entry.signatures}">
        <div>
            <p><a href="ISignature?ac=${signature.ac}">${signature.name}</a> (${signature.ac})</p>
            <div class="match">
                <c:forEach var="location" items="${signature.locations}">
                    <%--TODO: Get background-color for match--%>
                    <h:match proteinLength="${protein.length}"
                             start="${location.start}"
                             end="${location.end}"
                             colour="#ff9999"/>
                </c:forEach>
            </div>
            <%--Not sure why we need this break, but next entry gets messed up without it --%>
            <br/>
        <div>
        </c:forEach>
    </div>
    </c:if>
</c:forEach>

<a name="structural-features"></a>
<h3>Structural features</h3>
<%--TODO: Show CATH, SCOP and PDB features only --%>

<c:forEach var="structuralMatch" items="${protein.structuralMatches}">
    <div>
        <%--TODO: Build URL based on database name: http://www.ebi.ac.uk/pdbe-srv/view/entry/1jm7/summary, http://www.cathdb.info/cathnode/3.30.40.10, http://scop.mrc-lmb.cam.ac.uk/scop/search.cgi?key=g.44.1.1--%>
        <%--<p><a href="IEntry?ac=${entry.ac}">${entry.name}</a> (${entry.ac})</p>--%>
        <p><a href="${structuralMatch.domainId}">${structuralMatch.classId}</a> (${structuralMatch.databaseName})</p>
        <div class="match">
            <c:forEach var="location" items="${structuralMatch.locations}">
                <%--TODO: Get background-color for match--%>
                <h:match proteinLength="${protein.length}"
                         start="${location.start}"
                         end="${location.end}"
                         colour="#ff9999"/>
            </c:forEach>
        </div>
        <%--Not sure why we need this break, but next entry gets messed up without it --%>
        <br/>
    </div>
</c:forEach>

<a name="structural-predictions"></a>
<h3>Structural predictions</h3>
TODO
<%--TODO: Show ModBase predictions --%>

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