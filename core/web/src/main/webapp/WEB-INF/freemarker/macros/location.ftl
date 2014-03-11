<#--Location macro definition-->
<#macro location locationSpanId proteinLength titlePrefix location colourClass>

<#if standalone>
<#--If rendering I5 HTML output, attach a title to the span-->
<span id="${locationSpanId}"
      class="match ${colourClass}"
      style="left:  ${((location.start - 1) / proteinLength) * 100}%;
              width: ${((location.end - location.start + 1) / proteinLength) * 100}%;"
      title="${titlePrefix} ${location.start} - ${location.end}" >
<#else>
<#--If rendering through the website, the title is attached to the AJAX link instead of this span-->
<span id="${locationSpanId}"
      class="match ${colourClass}"
      style="left:  ${((location.start - 1) / proteinLength) * 100}%;
              width: ${((location.end - location.start + 1) / proteinLength) * 100}%;">
</#if>
</span>
</#macro>
