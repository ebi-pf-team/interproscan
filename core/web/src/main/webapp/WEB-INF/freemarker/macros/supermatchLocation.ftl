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
    <#assign entryAcs="">
    <#assign firstEntry = true>
    <#list supermatch.entries as entry>
        <#if firstEntry>
            <#assign firstEntry = false>
        <#else>
            <#assign entryAcs=entryAcs + ",">
        </#if>
        <#assign entryAcs=entryAcs + entry.ac>
    </#list>
    <#assign prefix="supermatch">
    <#if viewId?? && viewId?has_content>
        <#assign prefix=viewId+"-"+prefix>
    </#if>
    <a id="${prefix}-location-${supermatchId}"
       href="/interpro/popup/supermatch?id=${prefix}-popup-${supermatchId}&entryAcs=${entryAcs}&start=${locationObj.start?c}&end=${locationObj.end?c}"
       title="${title} ${locationObj.start} - ${locationObj.end}"
       class="match ${colourClass}"
       style="left:  ${(((locationObj.start - 1) / proteinLength) * 100)?c}%;
       width: ${(((locationObj.end - locationObj.start + 1) / proteinLength) * 100)?c}%;">
        <#--<@locationMacro.location locationSpanId="${prefix}-span-"+supermatchId proteinLength=proteinLength titlePrefix=title location=locationObj colourClass=colourClass/>-->
    </a>

    </#if>

</#macro>
