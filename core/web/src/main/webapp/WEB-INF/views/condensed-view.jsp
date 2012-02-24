<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="h" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${condensedView != null}">

    <c:set var="superMatchId" value="0" scope="request"/>



 <c:forEach var="line" items="${condensedView.lines}">
 <c:set var="type" value="${line.type}"/>
 <li id="${containerId}" class="signature entry-signatures">
 <!-- the order of the divs is important , first right column fixed-->
    <div class="bot-row-signame">${type}</div>
    <div class="bot-row-line">
    <div class="matches">
                <c:forEach var="superMatch" items="${line.superMatchList}">
                    <c:set var="superMatchId" value="${superMatchId + 1}"/>
                    <h:supermatchLocation id="supermatch-location-${superMatchId}"
                                          colourClass="c${entryColours[superMatch.firstEntry.ac]} ${type}"
                                          protein="${protein}"
                                          supermatch="${superMatch}"/>

                </c:forEach>
    </div>
    </div>
 </li>
        </c:forEach>

</c:if>


