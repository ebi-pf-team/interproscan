<%@ taglib prefix="h"  tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="protein"   required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleProtein" %>
<%@ attribute name="signature" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleSignature" %>
<%@ attribute name="entryTypeTitle" required="false" %>
<%@ attribute name="colourClass"    required="true" %>

<div>
    <p>
        ${signature.ac} (<a href="${fn:replace(signature.dataSource.linkUrl, '$0', signature.ac)}" title="${entryTypeTitle} (${signature.dataSource})">${signature.name}</a>)
    </p>

    <c:set var="locationId" value="0" scope="request" />

    <div class="match">
        <c:forEach var="location" items="${signature.locations}">
            <c:set var="locationId" value="${locationId + 1}" />
            <c:set var="dbClass">
                <c:if test="${colourClass != 'uni'}">
                    ${fn:replace(fn:toLowerCase(signature.dataSource), ' ', '-')}
                </c:if>
            </c:set>
            <h:matchLocation  id="${locationId}"
                              protein="${protein}"
                              signature="${signature}"
                              location="${location}"
                              colourClass="${dbClass} ${colourClass}"/>
        </c:forEach>
    </div>
</div>
