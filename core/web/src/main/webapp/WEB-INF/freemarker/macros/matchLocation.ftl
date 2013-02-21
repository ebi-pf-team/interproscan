<#import "location.ftl" as locationMacro>
<#macro matchLocation smid protein signature location colourClass>
    <#assign title=signature.ac>
<#--Signatures like "G3DSA:3.20.20.80" cause issues, remove special characters -->
<#--TODO? Try http://stackoverflow.com/questions/296264/using-regular-expressions-in-jsp-el-->
    <#assign prefix=signature.ac?replace(":","")?replace(".","")>

<@locationMacro.location smid=prefix+"-location-"+smid protein=protein titlePrefix=title location=location colourClass=colourClass/>

<div id="${prefix}-popup-${smid}" style="display: none;">
    <div class="popup_topl"><span class="${colourClass} caption_puce"></span>${location.start} - ${location.end}</div>
    <div class="popup_botl" style="font-size:88%;">
        <b>${signature.dataSource.sourceName}</b> <abbr class="icon icon-generic" data-icon="i" title="${signature.dataSource.description}"></abbr> <br/>
        <a href='${signature.dataSource.getLinkUrl(signature.ac)}' class="ext">${signature.ac} </a> <span>(${signature.name})</span>
    </div>
</div>
</#macro>
