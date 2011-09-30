<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--Returns protein features for inclusion in DBML--%>

<h3>Domains and sites</h3>

<%--<div class="match-line">--%>
    <%--<span class="match" style="width: 11.580883px; background-color:#e6c4a8;" title="144 - 180"></span>--%>
    <%--<span class="match-gap" style="width: 47.2886px; display: block;"></span>--%>
    <%--<span class="match" style="width: 10.6158085px; background-color:#ff0000;" title="147 - 180"></span>--%>
    <%--<span class="match-gap" style="width: 26.056984px; display: block;"></span>--%>
    <%--<span class="match" style="width: 11.902574px; background-color:#ff0000;" title="261 - 298"></span>--%>
<%--</div>--%>

<c:forEach var="entry" items="${protein.entries}">
    <div>
        <p><a href="IEntry?ac=${entry.ac}">${entry.name}</a> (${entry.ac})</p>
        <div class="match-line">
            <c:forEach var="location" items="${entry.locations}">
                <%--TODO: Put WIDTH in separate JSP so can include here and in protein.jsp to set in CSS--%>
                <%--final int WIDTH = 700; // pixels--%>

                <%--float graphicWidth     = ((matchEnd - matchStart) / proteinLength) * WIDTH;--%>

                <%--TODO: Add checks to see if start is 1 or end is protein.length--%>
                <%--float graphicLineWidth = 0;--%>
                <%--if (matchStart > 1) {--%>
                    <%--graphicLineWidth = (matchStart / proteinLength) * WIDTH;--%>
                <%--}--%>
                <span class="match"     style="width: ${((location.end - location.start + 1) / protein.length) * 700}px; background-color:#e6c4a8;" title="${location.start} - ${location.end}"></span>
                <span class="match-gap" style="width: ${(location.start / protein.length) * 700}px;   display: block;"></span>
            </c:forEach>
        </div>
        <%--Not sure why we need this break, but next entry gets messed up without it --%>
        <br/>
    </div>
</c:forEach>

<%--Not sure why we need this break, but table gets right-aligned without it...--%>
<div><br/></div>

<%--TODO: Could use HTML5 Canvas to highlight matches in graphic and table when hover over--%>
<table>
    <tr>
        <th>Entry</th>        
        <th>Signature</th>
        <th>Start</th>
        <th>End</th>
    </tr>
    <c:forEach var="entry" items="${protein.entries}">
        <c:forEach var="location" items="${entry.locations}">
            <tr>
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