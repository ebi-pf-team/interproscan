<#assign familyComponentHeight=protein.getFamilyComponentHeight(14,50)/>
<#--Protein family membership component-->
<svg id="proteinFamilyTree" x="40" y="${yPosition}" width="1200" height="${familyComponentHeight}"
     viewBox="0 0 1200 ${familyComponentHeight}">
    <text y="13px" style="font-family:Verdana,Helvetica,sans-serif;font-size:13px">
        <tspan style="fill:#393939;font-weight:bold">
            Protein family membership
        </tspan>
    <#if (!protein.familyEntries?has_content)>
        <tspan dx="15" style="fill:#838383">None predicted.</tspan>
    </#if>
    </text>
<#if (protein.familyEntries?has_content)>
${protein.familyHierarchyForSvg}
</#if>
</svg>
