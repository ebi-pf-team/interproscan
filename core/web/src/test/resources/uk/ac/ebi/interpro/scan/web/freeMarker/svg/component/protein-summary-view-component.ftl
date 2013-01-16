<#assign summaryViewComponentHeight=condensedView.getCondensedViewComponentHeightForSVG(17,70)/>
<#--Protein summary view component-->
<svg id="summaryView" x="40" y="${yPosition}" width="1200" height="${summaryViewComponentHeight}"
     viewBox="0 0 1200 ${summaryViewComponentHeight}">
    <text y="13px" style="font-family:Verdana,Helvetica,sans-serif;font-size:13px">
        <tspan style="fill:#393939;font-weight:bold">Domains and repeats</tspan>
    <#if (condensedView?? && !condensedView.lines?has_content)>
        <tspan dx="15" style="fill:#838383;font-size:13px">none</tspan>
    </#if>
    </text>
<#if condensedView??>
${condensedView.getCondensedViewForSVG(entryColours,scale)}
</#if>
</svg>