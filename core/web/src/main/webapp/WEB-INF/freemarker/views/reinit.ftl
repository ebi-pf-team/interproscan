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

    <#if success>
        Re-initialisation successful! New data is:
    <#else>
        Re-initialisation failed (view application logs to debug). Old data left as:
    </#if>
    <h2>InterPro Entry to Colour ID mappings</h2>

    <table>
        <tr>
            <th>Entry Ac</th>
            <th>Colour Id</th>
        </tr>
        <#list entryColours as entry>
            <tr>
                <td>${entry.key}</td>
                <td>${entry.value}</td>
            </tr>
        </#list>
    </table>

    <h2>InterPro Entry Hierarchy Details</h2>

    <table>
        <tr>
            <th>Entry Ac</th>
            <th>Level</th>
            <th>Parent Entry Ac</th>
            <th>Entries In Same Hierarchy</th>
        </tr>
        <#list entryHierarchy as entry>
            <tr>
                <td>${entry.key}</td>
                <td>${entry.value.hierarchyLevel}</td>
                <td>${entry.value.parentEntryAc}</td>
                <td>
                    <#list entry.value.entriesInSameHierarchy as text>
                        ${text}&nbsp;
                    </#list>
                </td>
            </tr>
        </#list>
    </table>

</div>
<footer>
    <#--Copyright ...-->
</footer>
</body>
</html>
