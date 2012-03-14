<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
<head>
    <title>InterproScan 5 Web Application Data Re-initialisation</title>
</head>
<body>
<header>
</header>
<div id="main">
    <h1>InterproScan 5 Web Application Data Re-initialisation</h1>

    <c:choose>
        <c:when test="${success}">
            Re-initialisation successful! New data is:
        </c:when>
        <c:otherwise>
            Re-initialisation failed (view application logs to debug). Old data left as:
        </c:otherwise>
    </c:choose>

    <h2>InterPro Entry to Colour ID mappings</h2>

    <table>
        <tr>
            <th>Entry Ac</th>
            <th>Colour Id</th>
        </tr>
        <c:forEach var="entry" items="${entryColours}">
            <tr>
                <td>${entry.key}</td>
                <td>${entry.value}</td>
            </tr>
        </c:forEach>
    </table>

    <h2>InterPro Entry Hierarchy Details</h2>

    <table>
        <tr>
            <th>Entry Ac</th>
            <th>Level</th>
            <th>Parent Entry Ac</th>
            <th>Entries In Same Hierarchy</th>
        </tr>
        <c:forEach var="entry" items="${entryHierarchy}">
            <tr>
                <td>${entry.key}</td>
                <td>${entry.value.hierarchyLevel}</td>
                <td>${entry.value.parentEntryAc}</td>
                <td>
                    <c:forEach var="text" items="${entry.value.entriesInSameHierarchy}">
                        ${text}&nbsp;
                    </c:forEach>
                </td>
            </tr>
        </c:forEach>
    </table>

</div>
<footer>
    <%--Copyright ...--%>
</footer>
</body>
</html>
