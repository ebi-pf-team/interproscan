<#--TODO DELETE - DON'T THINK THIS FILE IS USED!-->

<#--&lt;#&ndash;Entry hierarchy macro definition&ndash;&gt;-->
<#--<#macro hierachy supermatch>-->
<#--<span id="${smid}"-->
      <#--class="match ${colourClass}"-->
      <#--style="left:  ${(location.start / protein.length) * 100}%;-->
              <#--width: ${((location.end - location.start + 1) / protein.length) * 100}%;"-->
      <#--title="${titlePrefix} ${location.start} - ${location.end}">-->
<#--</span>-->

    <#--<#list scale?split(",") as scaleMarker>-->
    <#--<span class="grade" style="left:${(scaleMarker?number?int / protein.length) * 100}%;" title="${scaleMarker}"></span>-->
    <#--</#list>-->
<#--</#macro>-->
