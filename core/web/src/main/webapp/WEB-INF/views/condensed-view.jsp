<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="h" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${condensedView != null}">

    <c:set var="superMatchId" value="0" scope="request"/>

    <div id="condensed">
        <c:forEach var="line" items="${condensedView.lines}">
            <c:set var="type" value="null"/><%-- Need the type var scoped outside the forEach loop --%>
            <div class="supermatchline">
                <c:forEach var="superMatch" items="${line.superMatchList}">
                    <c:set var="superMatchId" value="${superMatchId + 1}"/>
                    <c:set var="type" value="${superMatch.type}"/>
                    <h:supermatchLocation id="supermatch-location-${superMatchId}"
                                          colourClass="c${entryColours[superMatch.firstEntry.ac]} ${type}"
                                          protein="${protein}"
                                          supermatch="${superMatch}"/>
                </c:forEach>
            </div>
            <span class="supermatchType">${type}</span>
        </c:forEach>
    </div>
</c:if>
