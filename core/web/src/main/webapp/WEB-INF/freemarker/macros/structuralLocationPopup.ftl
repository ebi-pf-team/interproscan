<#-- structuralMatchData is a map-->
<#macro structuralLocationPopup structPopupId location structuralMatchData databaseMetadata>

    <#assign databaseName=databaseMetadata.sourceName?upper_case>
    <#assign links="">
    <#assign locationDataMap=structuralMatchData.locationDataMap>
<#--Link on classId-->
    <#if databaseName?starts_with("PDB")>
        <#list locationDataMap?keys as dataEntry>
            <#assign strains="(">
            <#list locationDataMap[dataEntry] as domainId>
                <#if (domainId_index>0)>
                    <#assign strains=strains+", ">
                </#if>
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
    <#if standalone>
    <div id="${structPopupId}" style="display: none;">
    <#else>
    <div id="${structPopupId}">
    </#if>

    <div class="popup_topl"><span class="${databaseName} caption_puce"></span>${location.start} - ${location.end}</div>
    <div class="popup_botl" style="font-size:88%;"> <b>${databaseMetadata.sourceName}</b> <abbr class="icon icon-generic" data-icon="i" title="${databaseMetadata.description}"></abbr> <br/>
    ${links}<br/>
    </div>
</div>
</#macro>
