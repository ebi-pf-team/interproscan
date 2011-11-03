<%@ taglib prefix="h"  tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%--<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="protein"   required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleProtein" %>
<%@ attribute name="signature" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleSignature" %>
<%@ attribute name="entryTypeIcon"  required="false" %>
<%@ attribute name="entryTypeTitle" required="false" %>
<%@ attribute name="colourClass"    required="false" %>

<div>
    <p>
        <c:if test="${not empty entryTypeIcon}">
            <%--TODO: Make icon stuff a tag--%>
            <img src="/interpro/images/ico_type_${entryTypeIcon}_small.png"
                 alt="${entryTypeTitle}" title="${entryTypeTitle}"/>
        </c:if>
        <a href="ISignature?ac=${signature.ac}" title="${entryTypeTitle}">${signature.name}</a>
        <c:set var="prefix">
            <c:choose>
                <c:when test="${not empty signature.databaseDescription}">
                    <abbr title="${signature.databaseDescription}">${signature.database}</abbr>
                </c:when>
                <c:otherwise>
                    ${signature.database}
                </c:otherwise>
            </c:choose>
        </c:set>
        (${prefix}: ${signature.ac})
    </p>
    <div class="match">
        <c:forEach var="location" items="${signature.locations}">
            <c:set var="dbClass">
                <c:if test="${colourClass != 'uni'}">
                    ${fn:replace(fn:toLowerCase(signature.database), ' ', '-')}
                </c:if>
            </c:set>
            <h:location protein="${protein}"
                        location="${location}"
                        colourClass="${dbClass} ${colourClass}"/>
        </c:forEach>
    </div>
</div>
