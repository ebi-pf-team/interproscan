<%@ attribute name="proteinLength" required="true" type="java.lang.Integer" %>
<%@ attribute name="start"         required="true" type="java.lang.Integer" %>
<%@ attribute name="end"           required="true" type="java.lang.Integer" %>
<%@ attribute name="colour"        required="true"  %>

<%--TODO: Pass in collection of locations instead--%>

<span class="match"
      style="left:  ${(start / proteinLength) * 100}%;
             width: ${((end - start + 1) / proteinLength) * 100}%;
             background-color: ${colour};"
      title="${start} - ${end}">
</span>