<#import "matchLocation.ftl" as matchLocationMacro>
<#import "signatureText.ftl" as signatureTextMacro>
<#macro feature proteinAc proteinLength signature entryTypeTitle scale entryAc colourClass>
    <#global locationId=0>

    <#if signature.dataSource?lower_case?replace(" ","-") == "mobidb-lite">
    <#--For MobiDB we don't list one line of locations per signature as they can overlap - use one row per feature instead-->
        <#assign featureLocationsMap=signature.getFeatureLocationsMap()>
        <#list featureLocationsMap?keys as featureKey>
        <li class="signature">
        <#-- the order of the divs is important , first right column fixed-->
            <@signatureTextMacro.signatureText signature=signature proteinAc=proteinAc feature=featureKey/>
            <div class="bot-row-line">
                <div class="matches">
                    <#list featureLocationsMap[featureKey] as location>
                        <#assign locationId=locationId + 1>
                        <#assign dbClass>
                        <#-- Make the data source name lowercase and replace whitespace and underscores with hyphens,
                    e.g. "PROSITE_PROFILES" becomes "prosite-profiles" -->
                        ${signature.dataSource?lower_case?replace(" ","-")?replace("_","-")}
                        </#assign>
                        <#assign dbClass=dbClass?trim>
                        <@matchLocationMacro.matchLocation matchId=locationId proteinAc=proteinAc proteinLength=proteinLength signature=signature location=location entryAc=entryAc colourClass=dbClass+" "+colourClass/>
                    </#list>
                <#--Draw in scale markers for this line-->
                    <#list scale?split(",") as scaleMarker>
                        <span class="grade" style="left:${((scaleMarker?number?int / proteinLength) * 100)?c}%;" title="${scaleMarker}"></span>
                    </#list>
                </div>
            </div>
        </li>
        </#list>
    <#else>
    <li class="signature">
    <#-- the order of the divs is important , first right column fixed-->
        <@signatureTextMacro.signatureText signature=signature proteinAc=proteinAc feature=""/>
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
            <@matchLocationMacro.matchLocation matchId=locationId proteinAc=proteinAc proteinLength=proteinLength signature=signature location=location entryAc=entryAc colourClass=dbClass+" "+colourClass/>
        </#list>

    <#--Draw in scale markers for this line-->
        <#list scale?split(",") as scaleMarker>
            <span class="grade" style="left:${((scaleMarker?number?int / proteinLength) * 100)?c}%;" title="${scaleMarker}"></span>
        </#list>
    </div>
    </div>
    </li>


    </#if>

</#macro>
