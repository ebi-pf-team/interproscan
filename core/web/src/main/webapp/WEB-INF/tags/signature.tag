<%@ taglib prefix="h"  tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="protein"   required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleProtein" %>
<%@ attribute name="signature" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleSignature" %>
<%@ attribute name="entryTypeTitle" required="false" %>
<%@ attribute name="colourClass"    required="true" %>
     
    <c:set var="locationId" value="0" scope="request" />

    <!-- the order of the divs is important , first right column fixed-->
        <div class="bot-row-signame"><!-- link to modify -->
         <%--
        Setup variables ready for displaying signature information,
        e.g. could condense "G3DSA:2.40.20.10 (Pept_Ser_Cys)" to "G3DSA:2.40.2... (Pept_S...)".
        NOTE: PLEASE ENSURE THAT maxOverallLength >= maxAcLength + 4
        --%>
        <c:set var="maxAcLength" value="16" /><%-- Maximum signature accession length --%>
        <c:set var="maxOverallLength" value="20" /> <%-- Maximum allowed length of accession and name --%>
        <c:set var="acLength" value="${fn:length(signature.ac)}" /> <%-- Actual length of the signature accession, e.g. G3DSA:2.40.20.10 --%>
        <c:set var="nameLength" value="${fn:length(signature.name)}" /> <%-- Actual length of the signature name, e.g. Pept_Ser_Cys --%>
        <c:set var="maxNameLength" value="${maxOverallLength - acLength}" /> <%-- Initialise the maximum allowed length of name --%>
        <c:if test="${acLength > maxAcLength}">
            <%-- Accession was too long therefore should actually subtract maxAcLength instead of acLength --%>
            <c:set var="maxNameLength" value="${maxOverallLength - maxAcLength}" />
        </c:if>

        <%-- Now display the signature accession --%>
        <c:choose>
        <c:when test="${signature.ac != signature.name}">
        <a href="${fn:replace(signature.dataSource.linkUrl, '$0', signature.ac)}" title="${signature.ac} (${signature.name})"  class="neutral">
            </c:when>
            <c:otherwise>
            <a href="${fn:replace(signature.dataSource.linkUrl, '$0', signature.ac)}" title="${signature.ac} (${signature.name})" class="neutral" >
                </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${acLength > maxAcLength}">
                        <%--Accession is too long, need to truncate it--%>
                        ${fn:substring(signature.ac, 0, maxAcLength - 3)}...
                    </c:when>
                    <c:otherwise>
                        ${signature.ac}
                    </c:otherwise>
                </c:choose>
            </a>

            <%-- Now display the signature name (if not identical to the accession) --%>
            <c:if test="${signature.ac != signature.name}">
             <c:choose>
            <c:when test="${nameLength > maxNameLength}">
                <%--Name is too long, need to truncate it--%>
                   <span>(${fn:substring(signature.name, 0, maxNameLength - 3)}...)</span>
            </c:when>
            <c:otherwise>
               <span>(${signature.name})</span>
            </c:otherwise>
            </c:choose>
            </c:if>

           </div>
        <div class="bot-row-line">
        <div class="matches">

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