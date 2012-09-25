<#--Protein family membership component-->
<svg id="proteinFamilyTree" x="40px" y="210px" width="1200" height="100" viewBox="0 0 1200 100">
    <text y="13px"
          style="fill:#393939;font-family:Verdana,Helvetica,sans-serif;font-size:13px;font-weight:bold">
        Protein family membership:
    </text>
<#if (protein.familyEntries?has_content)>
${protein.familyHierarchyForSvg}
    <#else>
        <text x="180px" y="13px" style="fill:#838383">none</text>
</#if>
</svg>