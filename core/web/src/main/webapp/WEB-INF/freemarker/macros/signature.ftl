<#import "matchLocation.ftl" as matchLocationMacro>
<#import "signatureText.ftl" as signatureTextMacro>
<#macro signature proteinLength signature entryTypeTitle scale entryAc colourClass>
    <#global locationId=0>

<#-- the order of the divs is important , first right column fixed-->
<@signatureTextMacro.signatureText signature=signature/>
<div class="bot-row-line">
    <div class="matches">

        <#list signature.locations as location>
            <#assign locationId=locationId + 1>
            <#assign dbClass>
        <#-- Make the data source name lowercase and replace whitespace and underscores with hyphens,
    e.g. "PROSITE_PROFILES" becomes "prosite-profiles" -->
        ${signature.dataSource?lower_case?replace(" ","-")?replace("_","-")}
        </#assign>
            <#assign dbClass=dbClass?trim>
            <@matchLocationMacro.matchLocation matchId=locationId proteinLength=proteinLength signature=signature location=location entryAc=entryAc colourClass=dbClass+" "+colourClass/>
        </#list>


        <#--Draw in scale markers for this line-->
        <#list scale?split(",") as scaleMarker>
            <span class="grade" style="left:${((scaleMarker?number?int / proteinLength) * 100)?c}%;" title="${scaleMarker}"></span>
        </#list>

    </div>

</div>
</#macro>
