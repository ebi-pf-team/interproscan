<#import "../macros/supermatchLocation.ftl" as supermatchLocationMacro>
<#if condensedView??>

    <#--<#assign superMatchId="0" scope="request">-->
    <#global superMatchId=0>

           <#list condensedView.lines as line>
 <#assign type=line.type>
 <li class="signature entry-signatures">
 <!-- the order of the divs is important , first right column fixed-->
    <div class="bot-row-signame">${type}</div>
    <div class="bot-row-line">
    <div class="matches">
                <#list line.superMatchList as superMatch>
                    <#global superMatchId=superMatchId + 1>
                    <@supermatchLocationMacro.supermatchLocation id="supermatch-location-"+superMatchId protein=protein supermatch=superMatch colourClass="c"+entryColours[superMatch.firstEntry.ac]+" "+type />
                </#list>
    </div>
    </div>
 </li>
        </#list>

</#if>