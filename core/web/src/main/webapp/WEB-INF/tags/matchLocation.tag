<%@ attribute name="id" required="true" %>
<%@ attribute name="protein"       required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleProtein" %>
<%@ attribute name="signature" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleSignature" %>
<%@ attribute name="location" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleLocation" %>
<%@ attribute name="colourClass"   required="true"  %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h"  tagdir="/WEB-INF/tags" %>

<c:set var="title" value="${signature.ac}"/>
<c:set var="links" value="<a href='ISignature?ac=${signature.ac}'>${signature.ac}</a>"/>

<%--Signatures like "G3DSA:3.20.20.80" cause issues, remove special characters --%>
<%--TODO? Try http://stackoverflow.com/questions/296264/using-regular-expressions-in-jsp-el--%>
<c:set var="prefix" value="${fn:replace(signature.ac,':','')}"/>
<c:set var="prefix" value="${fn:replace(prefix,'.','')}"/>

<h:location id="${prefix}-location-${id}"
            protein="${protein}"
            titlePrefix="${title}: "
            location="${location}"
            colourClass="${colourClass}"/>

<div id="${prefix}-popup-${id}" style="display: none;">
    ${links}<br />
    Start: ${location.start}<br />
    End: ${location.end}
</div>
