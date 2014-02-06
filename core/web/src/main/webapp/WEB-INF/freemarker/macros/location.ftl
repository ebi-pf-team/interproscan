<#--Location macro definition-->
<#macro location smid proteinLength titlePrefix location colourClass>
<span id="${smid}"
      class="match ${colourClass}"
      style="left:  ${((location.start - 1) / proteinLength) * 100}%;
              width: ${((location.end - location.start + 1) / protein.length) * 100}%;"
      title="${titlePrefix} ${location.start} - ${location.end}">
</span>

    <#list scale?split(",") as scaleMarker>
    <span class="grade" style="left:${(scaleMarker?number?int / proteinLength) * 100}%;" title="${scaleMarker}"></span>
    </#list>
</#macro>
