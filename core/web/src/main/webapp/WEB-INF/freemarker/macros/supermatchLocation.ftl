<#import "location.ftl" as locationMacro>

<#macro supermatchLocation smid proteinLength supermatch colourClass>
    <#assign title=supermatch.type>

<@locationMacro.location smid="supermatch-location-"+smid proteinLength=proteinLength titlePrefix=title location=supermatch.location colourClass=colourClass/>

<div id="supermatch-popup-${smid}" style="display: none;">

    <div class="popup_topl"><span class="${colourClass} caption_puce"></span>${supermatch.location.start}
        - ${supermatch.location.end}</div>
<div class="rel_tree">${supermatch.entryHierarchyForPopup}</div>
</div>
</#macro>
