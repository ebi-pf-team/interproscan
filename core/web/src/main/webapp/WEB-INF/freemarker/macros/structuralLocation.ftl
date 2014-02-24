<#import "location.ftl" as locationMacro/>
<#import "structuralLocationPopup.ftl" as structuralLocationPopupMacro/>

<#-- Display structural features and predictions -->
<#-- structuralMatchData is a map-->
<#macro structuralLocation structMatchId proteinLength location structuralMatchData databaseMetadata>

    <#assign databaseName=databaseMetadata.sourceName?upper_case>
    <#assign title="">
    <#assign locationDataMap=structuralMatchData.locationDataMap>
<#--Link on classId-->
    <#if databaseName?starts_with("PDB")>
    <#--Link title shows domain Ids "2r0iA, 2r0iB". Link on class Id with strains shown in brackets "2r0i (A, B)" -->
        <#list locationDataMap?keys as dataEntry>
            <#assign title="">
            <#list locationDataMap[dataEntry] as domainId>
                <#if (domainId_index>0)>
                    <#assign title=title+",">
                </#if>
                <#assign title=title+domainId>
            </#list>
        </#list>
    </#if>

<#if standalone>
    <#--If InterProScan 5 HTML output-->
    <@locationMacro.location locationSpanId="match-span-"+structMatchId proteinLength=proteinLength titlePrefix=title location=location colourClass=databaseName/>
    <@structuralLocationPopupMacro.structuralLocationPopup structPopupId="match-popup-"+structMatchId location=location structuralMatchData=structuralMatchData databaseMetadata=databaseMetadata/>
<#else>
<#--If using this HTML in the InterPro website, get the hierarchy popup through an AJAX call-->
<a id="match-location-${structMatchId}"
   style="left:  ${((location.start - 1) / proteinLength) * 100}%;
           width: ${((location.end - location.start + 1) / proteinLength) * 100}%;"
   href="http://localhost:8181/interpro/popup">
    <@locationMacro.location locationSpanId="match-span-"+structMatchId proteinLength=proteinLength titlePrefix=title location=location colourClass=databaseName/>
</a>
</#if>
</#macro>
