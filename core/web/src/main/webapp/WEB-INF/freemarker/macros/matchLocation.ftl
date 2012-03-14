<#import "location.ftl" as locationMacro>
<#macro matchLocation id protein signature location colourClass>
<#assign title=signature.ac>
<#--Signatures like "G3DSA:3.20.20.80" cause issues, remove special characters -->
<#--TODO? Try http://stackoverflow.com/questions/296264/using-regular-expressions-in-jsp-el-->
<#assign prefix=signature.ac?replace(":","")?replace(".","")>

<@locationMacro.location id=prefix+"-location-"+id protein=protein titlePrefix=title location=location colourClass=colourClass/>

<div id="${prefix}-popup-${id}" style="display: none;">
    <div class="popup_topl"><span class="${colourClass} caption_puce"></span>${location.start} - ${location.end}</div>
    <div class="popup_botl">    <acronym title="${signature.dataSource.description}">${signature.dataSource.sourceName}</acronym><br/>
    <a href='${signature.dataSource.linkUrl?replace("$0", signature.ac)}' class="ext">${signature.ac} </a> <span>(${signature.name})</span> <br/>

   </div>

</div>
</#macro>