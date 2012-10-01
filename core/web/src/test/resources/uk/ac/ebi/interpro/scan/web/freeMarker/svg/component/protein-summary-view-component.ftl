<#--Protein summary view component-->
<svg id="summaryView" x="40px" y="340px" width="1200" height="250" viewBox="0 0 1200 250">
    <text y="13px"
          style="fill:#393939;font-family:Verdana,Helvetica,sans-serif;font-size:13px;font-weight:bold">
        Sequence features summary
    </text>
<#if condensedView??>
${condensedView.getCondensedViewForSvg(entryColours,scale)}
</#if>
</svg>