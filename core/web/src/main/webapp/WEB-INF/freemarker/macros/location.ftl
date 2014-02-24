<#--Location macro definition-->
<#macro location locationSpanId proteinLength titlePrefix location colourClass>
<span id="${locationSpanId}"
      class="match ${colourClass}"
      style="left:  ${((location.start - 1) / proteinLength) * 100}%;
              width: ${((location.end - location.start + 1) / proteinLength) * 100}%;"
      title="${titlePrefix} ${location.start} - ${location.end}" >
</span>
</#macro>
