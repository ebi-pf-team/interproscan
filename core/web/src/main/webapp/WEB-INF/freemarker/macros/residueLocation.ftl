<#import "location.ftl" as locationMacro>
<#import "residueLocationPopup.ftl" as residueLocationPopupMacro>

<#macro residueLocation residueId proteinAc proteinLength residue site>
    <#assign title=residue.residue>
    <#assign locationObj=residue.location>

    <#assign dbClass>
    <#-- Make the data source name lowercase and replace whitespace and underscores with hyphens,
e.g. "PROSITE_PROFILES" becomes "prosite-profiles" -->
    ${site.signature.dataSource?lower_case?replace(" ","-")?replace("_","-")}
    </#assign>
    <#assign entry=site.entry>
    <#assign colourClass>
        <#if entryColours[entry.ac]?? && (entry.type?lower_case?starts_with("domain") || entry.type?lower_case?starts_with("repeat"))>
        ${dbClass} c${entryColours[entry.ac]} ${entry.type}
        <#elseif entry.type?lower_case?starts_with("unknown")>
        ${dbClass} uni
        <#else>
        ${dbClass} ${entry.type}
        </#if>
    </#assign>


    <#if standalone>
    <#--If InterProScan 5 HTML output-->
        <@locationMacro.location locationSpanId="residue-span-"+residueId proteinLength=proteinLength titlePrefix=title location=locationObj colourClass=colourClass/>
        <@residueLocationPopupMacro.residueLocationPopup residuePopupId="residue-popup-"+residueId residueLocation=residue site=site colourClass=colourClass/>
    <#else>
    <#--If using this HTML in the InterPro website, get the hierarchy popup through an AJAX call-->
   <#assign prefix="residue">
    <#if viewId?? && viewId?has_content>
        <#assign prefix=viewId+"-"+prefix>
    </#if>
    <a id="${prefix}-location-${residueId}"
       href="/interpro/popup/residue?id=${prefix}-popup-${residueId}&proteinAc=${proteinAc}&siteId=${site.id?c}&residue=${title}&start=${locationObj.start?c}&end=${locationObj.end?c}"
       title="${title} ${locationObj.start} - ${locationObj.end}"
       class="match ${colourClass}"
       style="left:  ${((locationObj.start - 1) / proteinLength) * 100}%;
       width: ${((locationObj.end - locationObj.start + 1) / proteinLength) * 100}%;">
    </a>

    </#if>

</#macro>
