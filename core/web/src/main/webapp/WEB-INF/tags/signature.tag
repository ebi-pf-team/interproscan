<%@ taglib prefix="h"  tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%--<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="protein"   required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleProtein" %>
<%@ attribute name="signature" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleSignature" %>
<%@ attribute name="entryTypeTitle" required="false" %>
<%@ attribute name="colourClass"    required="true" %>

<div>
    <p>
        ${signature.ac} (<a href="ISignature?ac=${signature.ac}" title="${entryTypeTitle} (${signature.database})">${signature.name}</a>)
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
