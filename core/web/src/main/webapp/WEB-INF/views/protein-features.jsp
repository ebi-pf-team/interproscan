<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--Returns protein features for inclusion in DBML--%>

<h3>Domains and sites</h3>

<c:forEach var="entry" items="${protein.entries}">
    <div>
        <p><a href="IEntry?ac=${entry.ac}">${entry.name}</a> (${entry.ac})</p>
        <div class="match">
            <c:forEach var="location" items="${entry.locations}">
                <%--TODO: Get background-color for match--%>
                <span class="match"
                      style="left:  ${(location.start / protein.length) * 100}%;
                             width: ${((location.end - location.start + 1) / protein.length) * 100}%;
                             background-color:#ff9999;"
                      title="${location.start} - ${location.end}">
                </span>
            </c:forEach>
        </div>
        <%--Not sure why we need this break, but next entry gets messed up without it --%>
        <br/>
    </div>
</c:forEach>

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

<h3>Structural predictions</h3>
TODO
<%--TODO: Do we to do anything special here?--%>