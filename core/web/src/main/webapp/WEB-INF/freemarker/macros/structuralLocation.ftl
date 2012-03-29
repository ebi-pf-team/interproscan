<#import "location.ftl" as locationMacro/>
<#-- Display structural features and predictions -->
<#-- structuralMatchData is a map-->
<#macro structuralLocation smid protein location structuralMatchData databaseMetadata>

    <#assign databaseName=databaseMetadata.sourceName?upper_case>
    <#assign title="">
    <#assign links="">
    <#assign locationDataMap=structuralMatchData.locationDataMap>
<#--Link on classId-->
    <#if databaseName?starts_with("CATH") || databaseName?starts_with("SCOP") || databaseName?starts_with("MODBASE")>

        <#list locationDataMap?keys as dataEntry>
            <#assign link=dataEntry?replace("MB_", '')> <#--MODBASE MB_P38398 converted to P38398-->
            <#assign linkHref=databaseMetadata.linkUrl?replace("$0", link)>
            <#assign linkItemValue=dataEntry>
            <#assign links="<a class='ext' href='"+linkHref+"'>"+linkItemValue+"</a><br/>">
        <#--<a href="${links}" class="ext">${linkValue}</a><br/>-->
        </#list>

    <#--Link on domainId-->
        <#elseif databaseName?starts_with("SWISS-MODEL")>

            <#list locationDataMap?keys as dataEntry>
                <#list locationDataMap[dataEntry] as domainId>
                    <#assign link=dataEntry?replace("SW_", '')> <#--SWISS-MODEL SW_P38398 converted to P38398-->
                    <#assign linkHref=databaseMetadata.linkUrl?replace("$0", link)>
                    <#assign linkItemValue=domainId>
                    <#assign links=links+"<a class='ext' href='"+linkHref+"'>"+linkItemValue+"</a><br/>">
                </#list>
            </#list>

        <#elseif databaseName?starts_with("PDB")>

        <#--Link title shows domain Ids "2r0iA, 2r0iB". Link on class Id with strains shown in brackets "2r0i (A, B)" -->
            <#list locationDataMap?keys as dataEntry>
                <#assign title="">
                <#assign strains="(">
                <#list locationDataMap[dataEntry] as domainId>
                    <#if (domainId_index>0)>
                        <#assign title=title+",">
                        <#assign strains=strains+", ">
                    </#if>
                    <#assign title=title+domainId>
                    <#assign strains=strains+domainId?replace(dataEntry, '')>
                </#list>
                <#assign strains=strains+")">
                <#assign linkHref=databaseMetadata.linkUrl?replace("$0", dataEntry)>
                <#assign linkItemValue=dataEntry>
                <#assign links=links+"<a class='ext' href='"+linkHref+"'>"+linkItemValue+"</a> "+strains+"<br/>">
            </#list>
        <#else>
        Unknown database: ${databaseName}.
    </#if>

<@locationMacro.location smid="match-location-"+smid protein=protein titlePrefix=title location=location colourClass=databaseName/>

<div id="match-popup-${smid}" style="display: none;">

    <div class="popup_topl"><span class="${databaseName} caption_puce"></span>${location.start} - ${location.end}</div>
    <div class="popup_botl"><acronym
            title="${databaseMetadata.description}">${databaseMetadata.sourceName}</acronym><br/>
    ${links}<br/>

    </div>
</div>
</#macro>
