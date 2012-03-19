<%@ attribute name="id" required="true" %>
<%@ attribute name="protein" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleProtein" %>
<%@ attribute name="supermatch" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleSuperMatch" %>
<%@ attribute name="colourClass" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="entry" uri="http://www.ebi.ac.uk/interpro/i5/tld/entryHierarchy" %>


<c:set var="title" value="${supermatch.type}"/>

<h:location id="supermatch-location-${id}"
            protein="${protein}"
            titlePrefix="${title}: "
            location="${supermatch.location}"
            colourClass="${colourClass}"/>

<div id="supermatch-popup-${id}" style="display: none;">

    <div class="popup_topl"><span class="${colourClass} caption_puce"></span>${supermatch.location.start}
        - ${supermatch.location.end}</div>
    <entry:supermatchHierarchyTag supermatch="${supermatch}"/>
</div>

