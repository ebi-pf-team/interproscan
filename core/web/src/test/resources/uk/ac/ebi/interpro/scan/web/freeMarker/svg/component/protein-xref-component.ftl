<#assign proteinXrefComponentHeight=protein.getProteinXrefComponentHeightForSVG(20,70)/>
<#--Protein GO terms component-->
<g id="goSection">
<#assign tspanDyValue="20px"/>
    <!-- GO terms section title heading -->
    <svg x="40" y="${yPosition?string("0")}" width="1200" height="${proteinXrefComponentHeight}"
         viewBox="0 0 1200 ${proteinXrefComponentHeight}">
        <text font-size="13px" y="13px" fill="#393939" font-family="Verdana, Helvetica, sans-serif"
              font-weight="bold">
            GO Term prediction
        </text>
        <g id="goTable">
            <svg y="30px">
            <#--Biological process-->
                <text id="biologicalProcess" font-size="11px" x="10px" dy="20px"
                      font-family="Verdana, Helvetica, sans-serif">
                    <tspan x="10px" fill="#393939" font-weight="bold">
                        Biological process
                    </tspan>
                <#assign hasGo=false/>
                <#list protein.processGoTerms as goTerm>
                    <tspan x="10px" dy="${tspanDyValue}"
                           style="text-decoration:underline;stroke:none;fill:#0072FE;"><a
                            xlink:href="http://www.ebi.ac.uk/QuickGO/GTerm?id=${goTerm.accession}"
                            target="_top">${goTerm.accession}</a></tspan>
                    <tspan style="stroke:none;fill:#525252;">${goTerm.termName}</tspan>
                    <#assign hasGo=true/>
                </#list>
                <#if !hasGo>
                    <tspan x="10px" dy="${tspanDyValue}" style="fill:#525252;stroke:none;">
                        None predicted.
                    </tspan>
                </#if>
                </text>
            <#--Molecular function-->
                <text id="molecularFunction" font-size="11px" x="436px" dy="20px"
                      font-family="Verdana, Helvetica, sans-serif">
                    <tspan x="436px" fill="#393939" font-weight="bold">
                        Molecular function
                    </tspan>
                <#assign hasGo=false/>
                <#list protein.functionGoTerms as goTerm>
                    <tspan x="436px" dy="${tspanDyValue}"
                           style="text-decoration:underline;stroke:none;fill:#0072FE;"><a
                            xlink:href="http://www.ebi.ac.uk/QuickGO/GTerm?id=${goTerm.accession}"
                            target="_top">${goTerm.accession}</a></tspan>
                    <tspan style="stroke:none;fill:#525252;">${goTerm.termName}</tspan>
                    <#assign hasGo=true/>
                </#list>
                <#if !hasGo>
                    <tspan x="436px" dy="${tspanDyValue}" style="fill:#525252;stroke:none;">
                        None predicted.
                    </tspan>
                </#if>
                </text>
            <#--Cellular component-->
                <text font-size="11px" x="862px" dy="20px"
                      font-family="Verdana, Helvetica, sans-serif">
                    <tspan x="862px" fill="#393939" font-weight="bold">
                        Cellular component
                    </tspan>
                <#assign hasGo=false/>
                <#list protein.componentGoTerms as goTerm>
                    <tspan x="862px" dy="${tspanDyValue}"
                           style="text-decoration:underline;stroke:none;fill:#0072FE;"><a
                            xlink:href="http://www.ebi.ac.uk/QuickGO/GTerm?id=${goTerm.accession}"
                            target="_top">${goTerm.accession}</a></tspan>
                    <tspan style="stroke:none;fill:#525252;">${goTerm.termName}</tspan>
                    <#assign hasGo=true/>
                </#list>
                <#if !hasGo>
                    <tspan x="862px" dy="${tspanDyValue}" style="fill:#525252;stroke:none;">
                        None predicted.
                    </tspan>
                </#if>
                </text>
            </svg>
        </g>
    </svg>
</g>
