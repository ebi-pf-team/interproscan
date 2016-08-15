<#--Location macro definition-->
<#macro location locationSpanId proteinLength titlePrefix location colourClass>

<#if standalone>
<#--If rendering I5 HTML output, attach a title to the span-->
<span id="${locationSpanId}"
      class="match ${colourClass}"
      style="left:  ${(((location.start - 1) / proteinLength) * 100)?c}%;
              width: ${(((location.end - location.start + 1) / proteinLength) * 100)?c}%;"
      title="${titlePrefix} ${location.start} - ${location.end}" >
<#else>
<#--If rendering through the website, the title is attached to the AJAX link instead of this span-->
<#--TODO When structural match popups can be called through AJAX too then add shared code here?-->
<#--<a id="${locationSpanId}"-->
       <#--href="/interpro/popup/supermatch?id=${prefix}-popup-${supermatchId}&entryAc=${entryAc}&start=${locationObj.start?c}&end=${locationObj.end?c}"-->
       <#--title="${title} ${locationObj.start} - ${locationObj.end}"-->
       <#--class="match ${colourClass}"-->
       <#--style="left:  ${(((locationObj.start - 1) / proteinLength) * 100)?c}%;-->
       <#--width: ${(((locationObj.end - locationObj.start + 1) / proteinLength) * 100)?c}%;">-->
</#if>
</span>
</#macro>
