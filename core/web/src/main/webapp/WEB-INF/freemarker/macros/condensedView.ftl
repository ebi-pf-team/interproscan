<#import "../macros/supermatchLocation.ftl" as supermatchLocationMacro>

<#macro condensedView condensedView scale entryColours>

    <#if condensedView?? && (condensedView.numSuperMatchBlobs > 0)>
    <div class="prot_sum">
    <div class="bot-row">
        <div class="bot-row-line-top"></div>
        <ol class="signatures">


    <#global superMatchId=0>
    <#assign proteinLength=condensedView.proteinLength>

    <#--Show all condensed view information or just the basics?-->
    <#assign showAll=true>
    <#if showFullInfo?has_content && !showFullInfo>
        <#assign showAll=false>
    </#if>

    <#list condensedView.lines as line>
        <#assign type=line.type>
    <li class="signature entry-signatures">
        <#-- the order of the divs is important , first right column fixed-->
        <#if showAll>
        <div class="bot-row-signame">${type}</div>
        </#if>
        <div class="bot-row-line">
            <div class="matches">
                <#list line.superMatchList as superMatch>
                    <#global superMatchId=superMatchId + 1>
                    <#--TODO: Is the next check the right thing to do???  Might not be...-->
                    <#--This check ensures that the entry is in the colour mapping file.  If not, uses a default class.-->
                    <#if entryColours[superMatch.firstEntry.ac]??>
                    <@supermatchLocationMacro.supermatchLocation supermatchId=superMatchId proteinLength=proteinLength supermatch=superMatch colourClass="c"+entryColours[superMatch.firstEntry.ac]+" "+type />
                <#else>
                    <@supermatchLocationMacro.supermatchLocation supermatchId=superMatchId proteinLength=proteinLength supermatch=superMatch colourClass="c1 "+type />
                </#if>
                </#list>

                <#--Draw in scale markers for this line-->
                <#list scale?split(",") as scaleMarker>
                    <span class="grade" style="left:${((scaleMarker?number?int / proteinLength) * 100)?c}%;" title="${scaleMarker}"></span>
                </#list>

            </div>
        </div>
    </li>
    </#list>

        </ol>
        <div class="bot-row-line-bot"></div>
    </div>

    <#if showAll>
    <div class="prot_scale">
        <div class="bot-row">

            <div class="bot-row-line">
                <div style="position:relative;">
                    <#-- Position marker lines -->
                    <#list scale?split(",") as scaleMarker>
                        <#-- to build an exception for 0 -->
                        <#if scaleMarker?number == 0>
                            <span class="scale_bar" style="left:${((scaleMarker?number / proteinLength) * 100)?c}%;"
                                  title="1"></span>
                                <span class="scale_numb"
                                      style="left:${((scaleMarker?number / proteinLength) * 100+1)?c}%;">1</span>
                        <#else>
                            <span class="scale_bar" style="left:${((scaleMarker?number / proteinLength) * 100)?c}%;"
                                  title="${scaleMarker}"></span>
                                <span class="scale_numb"
                                      style="left:${((scaleMarker?number / proteinLength) * 100)?c}%;">${scaleMarker}</span>
                        </#if>
                    </#list>
                </div>
            </div>

        </div>
    </div>
    </#if>

    </div>

    <#else>
    <div class="prot_sum" style="background:none;">
    <div class="bot-row">
        None predicted.
    </div>
    </div>
    </#if>

</#macro>
