<#assign summaryHSViewComponentHeight=condensedHSView.getCondensedViewComponentHeightForSVG(17,70)/>
<#--Protein homologous superfamily summary view component-->
<svg id="summaryHSView" x="40" y="${yPosition}" width="1200" height="${summaryHSViewComponentHeight}"
     viewBox="0 0 1200 ${summaryHSViewComponentHeight}">
    <text y="13px" style="font-family:Verdana,Helvetica,sans-serif;font-size:13px">
        <tspan style="fill:#393939;font-weight:bold">Homologous superfamilies</tspan>
    <#if (condensedHSView?? && !condensedHSView.lines?has_content)>
        <tspan dx="15" style="fill:#838383;font-size:13px">none</tspan>
    </#if>
    </text>
<#if condensedHSView??>
${condensedHSView.getCondensedViewForSVG(entryColours,scale)}
</#if>
</svg>