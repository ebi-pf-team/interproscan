<#import "location.ftl" as locationMacro/>
<#-- Display structural features and predictions -->
<#-- structuralMatchData is a map-->
<#macro structuralLocation smid proteinLength location structuralMatchData databaseMetadata>

    <#assign databaseName=databaseMetadata.sourceName?upper_case>
    <#assign title="">
    <#assign links="">
    <#assign locationDataMap=structuralMatchData.locationDataMap>
<#--Link on classId-->
    <#if databaseName?starts_with("PDB")>
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
            <#assign linkHref=databaseMetadata.getLinkUrl(dataEntry)>
            <#assign linkItemValue=dataEntry>
            <#assign links=links+"<a class='ext' href='"+linkHref+"'>"+linkItemValue+"</a> "+strains+"<br/>">
        </#list>
    <#else>
        <#list locationDataMap?keys as dataEntry>
            <#assign linkHref=databaseMetadata.getLinkUrl(dataEntry)>
            <#assign links="<a class='ext' href='"+linkHref+"' >"+dataEntry+"</a>">
        </#list>
    </#if>
    <@locationMacro.location smid="match-location-"+smid proteinLength=proteinLength titlePrefix=title location=location colourClass=databaseName/>

<div id="match-popup-${smid}" style="display: none;">

    <div class="popup_topl"><span class="${databaseName} caption_puce"></span>${location.start} - ${location.end}</div>
    <div class="popup_botl" style="font-size:88%;"> <b>${databaseMetadata.sourceName}</b> <abbr class="icon icon-generic" data-icon="i" title="${databaseMetadata.description}"></abbr> <br/>
    ${links}<br/>

    </div>
</div>
</#macro>
