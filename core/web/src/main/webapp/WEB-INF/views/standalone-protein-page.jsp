<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: pjones
  Date: 08/02/12
  Time: 15:00
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <%--TODO: see http://html5boilerplate.com/--%>
    <meta charset="utf-8">
    <c:choose>
        <c:when test="${protein == null}">
            No data for this protein - InterPro
        </c:when>
        <c:otherwise>
            <title>${protein.name} (${protein.ac}) - InterPro</title>
        </c:otherwise>
    </c:choose>
    <meta name="description" content="InterProScan result page: Protein matches and sequence features">
    <meta name="author" content="InterPro Team, European Bioinformatics Institute">


    <link href="<c:url value="/resources/css/type_colours.css" />" rel="stylesheet" type="text/css"/>
    <link href="<c:url value="/resources/css/database.css" />" rel="stylesheet" type="text/css"/>
    <link href="<c:url value="/resources/javascript/qtip2/jquery.qtip.css" />" rel="stylesheet" type="text/css"/>
     <link href="<c:url value="/resources/css/protein.css" />" rel="stylesheet" type="text/css"/>
     <script src="<c:url value="/resources/javascript/protein.js"/>" type="text/javascript"></script>

    <link href="<c:url value="/resources/javascript/jquery/ui/css/ui-lightness/jquery-ui-1.8.17.custom.css" />"
          rel="stylesheet" type="text/css"/>
    <script src="<c:url value="/resources/javascript/jquery/jquery-1.7.1.min.js"/>" type="text/javascript"></script>
    <script src="<c:url value="/resources/javascript/jquery/ui/js/jquery-ui-1.8.17.custom.min.js"/>"
            type="text/javascript"></script>
    <script src="<c:url value="/resources/javascript/qtip2/jquery.qtip.min.js"/>" type="text/javascript"></script>
    <script src="<c:url value="/resources/javascript/jquery.cookie.js"/>" type="text/javascript"></script>
    <script src="<c:url value="/resources/javascript/jquery/jquery.jscroll.min.js"/>" type="text/javascript"></script>

</head>
<body>
<div class="contentsarea">

    <div class="left-menu">
        <c:import url="protein-menu.jsp"/>
    </div>

    <div class="main-content">
        <c:choose>
            <c:when test="${protein == null}">
                Sorry, no data found for this protein.
            </c:when>
            <c:otherwise>
                <%--<header>--%>
                    <%--<nav>--%>
                        <%--<div class="breadcrumb">--%>
                            <%--<a href="<c:url value="/proteins"/>">Proteins</a> > ${protein.name} (${protein.ac})--%>
                        <%--</div>--%>
                    <%--</nav>--%>
                <%--</header>--%>

                            <%--NOTE: Can use import with absolute URLs, so could in theory include content from DBML to aid transition!--%>
                        <c:import url="protein-body.jsp"/>                                  
            </c:otherwise>
        </c:choose>
    </div>
</div>
<script type="text/javascript">
    $(document).ready(function() {
        // Read colour preference from cookie (requires http://plugins.jquery.com/project/Cookie)

        // Retrieve existing cookies and set checkbox states accordingly
        var checkBoxIDs = ["#check-2", "#check-3", "#check-4", "#check-5", "#check-6"];
        for (i = 0; i < checkBoxIDs.length; i++) {
            var checkBoxId = checkBoxIDs[i];
            var cookieVal = $.cookie(checkBoxId);
            if (cookieVal != null) {
                $(checkBoxId).prop("checked", cookieVal == "true");
            }
        }

        // and the radio button group
        var radioCookieVal = $.cookie("colour-by-domain");
        if (radioCookieVal != null) {
            if (radioCookieVal == "true") {
                $('#domainColour').prop("checked", true);
            }
            else {
                $('#databaseColour').prop("checked", true);
            }
        }

        // Keep the filter menu in view
        $("#menu").jScroll();
        // CSS switching
        configureStylesheets($('input[name="blobColour"]:checked').attr('id') == 'domainColour'); // initialise

        $('input[name="blobColour"]').change(function() {
            configureStylesheets($('input[name="blobColour"]:checked').attr('id') == 'domainColour');
        });

        // Change event for type checkboxes (Family, Domain etc.)
        $(".type").change(function() {
            displayType(this);
        });

        // Initialise types
        $(".type").each(function() {
            displayType(this);
        });

        // Change event for un-integrated sig matches checkbox
        $("#check-6").change(function() {
            displayUnintegrated(this);
        });

        // Initialise un-integrated.
        displayUnintegrated($("#check-6"));

        $('span[id*="location-"]').each(
                function(i) {
                    preparePopup(this.id);
                }
        );
    });
</script>
</body>
</html>
