<#--Protein info component-->
<svg id="proteinInfo" x="30" y="${yPosition}" width="1280" height="${proteinInfoComponentHeight}"
     viewBox="0 0 1280 ${proteinInfoComponentHeight}" xmlns="http://www.w3.org/2000/svg" version="1.1">
    <text x="10px" y="25px">
        <tspan x="10px" dy="10px" font-size="22" fill="#284ADB" font-family="Verdana, Helvetica, sans-serif"
               font-weight="normal">
        ${protein.ac}
        </tspan>
    </text>
    <#--Protein accession, species and fragment/complete not necessarily known for a submitted sequence, therefore commented out...-->
    <text y="50px" font-size="13" font-family="Verdana, Helvetica, sans-serif">
        <#--<tspan font-weight="bold" x="10px" style="fill: #393939">-->
            <#--Accession-->
        <#--</tspan>-->
        <#--<tspan x="10px" dx="100px" fill="#838383">${protein.ac}</tspan>-->
        <#--<tspan font-weight="bold" x="10px" dy="30px" style="fill: #393939">-->
            <#--Species-->
        <#--</tspan>-->
        <#--<tspan x="10px" dx="100px" fill="#838383">${protein.taxFullName}</tspan>-->
        <tspan font-weight="bold" x="10px" dy="30px" style="fill: #393939">
            Length
        </tspan>
    <#--&lt;#&ndash;Work out if protein sequence is a fragment or not&ndash;&gt;-->
    <#--<#assign isProteinFragment=protein.proteinFragment />-->
        <tspan x="10px" dx="100px" fill="#838383">${protein.length} amino acids
        <#--<#if isProteinFragment>(fragment)<#else>(complete)</#if>-->
        </tspan>
    </text>
    <#--<text x="1065" y="145" font-size="10">-->
        <#--<tspan fill="#525252" font-family="Verdana, Helvetica, sans-serif">Source:</tspan>-->
        <#--<tspan fill="#838383" font-family="Verdana, Helvetica, sans-serif">UniProtKB</tspan>-->
    <#--</text>-->

    <line x1="10" y1="100" x2="${(globalDocumentWidth - 80)?string("0")}" y2="100" stroke="#CDCDCD" stroke-width="1"/>
</svg>
