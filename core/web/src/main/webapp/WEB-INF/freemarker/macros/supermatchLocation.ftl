<#--<#assign entry=JspTaglibs["http://www.ebi.ac.uk/interpro/i5/tld/entryHierarchy"]>-->
<#import "location.ftl" as locationMacro>

<#macro supermatchLocation id protein supermatch colourClass>
    <#assign title=supermatch.type>

<@locationMacro.location id="supermatch-location-"+id protein=protein titlePrefix=title location=supermatch.location colourClass=colourClass/>

<div id="supermatch-popup-${id}" style="display: none;">

    <div class="popup_topl"><span class="${colourClass} caption_puce"></span>${supermatch.location.start}
        - ${supermatch.location.end}</div>
    ${supermatch.entryHierachy}
</div>
</#macro>