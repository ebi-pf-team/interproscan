<#macro residueLocationPopup residuePopupId residueLocation site colourClass>
    <#assign title=residueLocation.residue>
    <#assign locationObj=residueLocation.location>
    <#assign signature=site.signature>

    <#--Show detailed hierarchy information in a popup using hidden DIV-->
    <#if standalone>
        <div id="${residuePopupId}" style="display: none;">
    <#else>
        <div id="${residuePopupId}">
    </#if>
        <div class="popup_topl">
            <span class="${colourClass} caption_puce"></span>
    <#if locationObj.start==locationObj.end>
    ${locationObj.start}${title}
    <#else>
    ${locationObj.start}-${locationObj.end}${title}
    </#if>
        </div>
    <div class="popup_botl" style="font-size:88%;">
        <#if site.description?has_content>
            <b>${site.description}</b>
            <br/>
        </#if>
        <#list site.siteLocations as residueMatch>
            <#assign iterLocationObj=residueMatch.location>
            <#if iterLocationObj.start == locationObj.start && iterLocationObj.end == locationObj.end && title == residueMatch.residue>
            <b>
            </#if>
            <#if iterLocationObj.start==iterLocationObj.end>
            ${iterLocationObj.start}${residueMatch.residue}<#if residueMatch_has_next>,</#if>
            <#else>
            ${iterLocationObj.start}-${iterLocationObj.end}${residueMatch.residue}<#if residueMatch_has_next>,</#if>
            </#if>
            <#if iterLocationObj.start == locationObj.start && iterLocationObj.end == locationObj.end && title == residueMatch.residue>
            </b>
            </#if>
        </#list>
        <br/>
        <b>${signature.dataSource.sourceName}</b> <abbr class="icon icon-generic" data-icon="i" title="${signature.dataSource.description}"></abbr> <br/>
        <a href='${signature.dataSource.getLinkUrl(signature.ac)}' class="ext">${signature.ac} </a>
        <span>(<#if signature.name??>${signature.name}<#else>${signature.ac}</#if>)</span>
        </div>
    </div>

</#macro>
