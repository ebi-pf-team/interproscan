<#import "location.ftl" as locationMacro>
<#import "residueLocationPopup.ftl" as residueLocationPopupMacro>

<#macro residueLocation residueId proteinLength residue site colourClass>
    <#assign title=residue.residue>
    <#assign locationObj=residue.location>

    <#if standalone>
    <#--If InterProScan 5 HTML output-->
        <@locationMacro.location locationSpanId="residue-span-"+residueId proteinLength=proteinLength titlePrefix=title location=locationObj colourClass=colourClass/>
        <@residueLocationPopupMacro.residueLocationPopup residuePopupId="residue-popup-"+residueId residueLocation=residue site=site colourClass=colourClass/>
    <#else>
    <#--If using this HTML in the InterPro website, get the hierarchy popup through an AJAX call-->
    <#--
    <#assign entryAcs="">
    <#assign firstEntry = true>
    <#list residue.entries as entry>
        <#if firstEntry>
            <#assign firstEntry = false>
        <#else>
            <#assign entryAcs=entryAcs + ",">
        </#if>
        <#assign entryAcs=entryAcs + entry.ac>
    </#list>
    -->
    <#assign prefix="residue">
    <#if viewId?? && viewId?has_content>
        <#assign prefix=viewId+"-"+prefix>
    </#if>
    <a id="${prefix}-location-${residueId}"
       href="/interpro/popup/residue?id=${prefix}-popup-${residueId}&start=${locationObj.start?c}&end=${locationObj.end?c}"
       title="${title} ${locationObj.start} - ${locationObj.end}"
       class="match ${colourClass}"
       style="left:  ${((locationObj.start - 1) / proteinLength) * 100}%;
       width: ${((locationObj.end - locationObj.start + 1) / proteinLength) * 100}%;">
        <#--<@locationMacro.location locationSpanId="${prefix}-span-"+residueId proteinLength=proteinLength titlePrefix=title location=locationObj colourClass=colourClass/>-->
    </a>

    </#if>

</#macro>
