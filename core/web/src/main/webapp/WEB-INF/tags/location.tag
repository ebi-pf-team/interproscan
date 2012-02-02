<%@ attribute name="protein" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleProtein" %>
<%@ attribute name="location" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleLocation" %>
<%@ attribute name="colourClass" required="true" %>

<%--TODO: Pass in collection of locations instead?     No - want different behaviour for supermatches, so leave as-is.--%>

<span class="match ${colourClass}"
      style="left:  ${(location.start / protein.length) * 100}%;
              width: ${((location.end - location.start + 1) / protein.length) * 100}%;"
      title="${location.start} - ${location.end}">
</span>
