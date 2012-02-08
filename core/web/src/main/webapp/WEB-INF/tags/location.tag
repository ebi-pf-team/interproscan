<%@ attribute name="id" required="true" %>
<%@ attribute name="protein" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleProtein" %>
<%@ attribute name="titlePrefix" required="false" %>
<%@ attribute name="location" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleLocation" %>
<%@ attribute name="colourClass" required="true"  %>

<span id="${id}"
      class="match ${colourClass}"
      style="left:  ${(location.start / protein.length) * 100}%;
             width: ${((location.end - location.start + 1) / protein.length) * 100}%;"
      title="${titlePrefix} ${location.start} - ${location.end}">
</span>
