<#import "location.ftl" as locationMacro>
<#import "supermatchLocationPopup.ftl" as supermatchLocationPopupMacro>

<#macro supermatchLocation supermatchId proteinLength supermatch colourClass>
    <#assign title=supermatch.type>
    <#assign locationObj=supermatch.location>

    <#if standalone>
    <#--If InterProScan 5 HTML output-->
        <@locationMacro.location locationSpanId="supermatch-span-"+supermatchId proteinLength=proteinLength titlePrefix=title location=locationObj colourClass=colourClass/>
        <@supermatchLocationPopupMacro.supermatchLocationPopup superMatchPopupId="supermatch-popup-"+supermatchId supermatch=supermatch colourClass=colourClass/>
    <#else>
    <#--If using this HTML in the InterPro website, get the hierarchy popup through an AJAX call-->
    <#assign entryAc="">
    <#list supermatch.entries as entry>
        <#assign entryAc=entry.ac>
        <#break>
    </#list>
    <a id="supermatch-location-${supermatchId}"
       style="left:  ${((locationObj.start - 1) / proteinLength) * 100}%;
               width: ${((locationObj.end - locationObj.start + 1) / proteinLength) * 100}%;"
       href="http://localhost:8181/interpro/popup/supermatch?id=supermatch-popup-${supermatchId}&ac=${entryAc}&start=${locationObj.start?c}&end=${locationObj.end?c}">
        <@locationMacro.location locationSpanId="supermatch-span-"+supermatchId proteinLength=proteinLength titlePrefix=title location=locationObj colourClass=colourClass/>
    </a>

    </#if>

</#macro>
