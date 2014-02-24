<#import "../macros/supermatchLocation.ftl" as supermatchLocationMacro>

<#macro condensedView condensedView proteinLength scale entryColours>

    <#if condensedView?? && (condensedView.numSuperMatchBlobs > 0)>
    <div class="prot_sum">
    <#else>
    <div class="prot_sum" style="background:none;">
    </#if>

    <#if condensedView?? && (condensedView.numSuperMatchBlobs > 0)>
    <div class="bot-row">
        <div class="bot-row-line-top"></div>
        <ol class="signatures">


<#--<#assign superMatchId="0" scope="request">-->
    <#global superMatchId=0>

    <#list condensedView.lines as line>
        <#assign type=line.type>
    <li class="signature entry-signatures">
        <#-- the order of the divs is important , first right column fixed-->
        <div class="bot-row-signame">${type}</div>
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
                    <span class="grade" style="left:${(scaleMarker?number?int / proteinLength) * 100}%;" title="${scaleMarker}"></span>
                </#list>

            </div>
        </div>
    </li>
    </#list>

        </ol>
        <div class="bot-row-line-bot"></div>
    </div>

    <div class="prot_scale">
        <div class="bot-row">

            <div class="bot-row-line">
                <div style="position:relative;">
                    <#-- Position marker lines -->
                    <#list scale?split(",") as scaleMarker>
                        <#-- to build an exception for 0 -->
                        <#if scaleMarker?number == 0>
                            <span class="scale_bar" style="left:${(scaleMarker?number / proteinLength) * 100}%;"
                                  title="1"></span>
                                <span class="scale_numb"
                                      style="left:${(scaleMarker?number / proteinLength) * 100+1}%;">1</span>
                        <#else>
                            <span class="scale_bar" style="left:${(scaleMarker?number / proteinLength) * 100}%;"
                                  title="${scaleMarker}"></span>
                                <span class="scale_numb"
                                      style="left:${(scaleMarker?number / proteinLength) * 100}%;">${scaleMarker}</span>
                        </#if>
                    </#list>
                </div>
            </div>

        </div>
    </div>

    <#else>
    <div class="bot-row">
        None predicted.
    </div>
    </#if>

</#macro>
