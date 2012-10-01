<#--Protein info component-->
<svg id="proteinInfo" x="30px" y="30px">
    <text x="10px" y="25px">
        <tspan x="10px" dy="10px" font-size="22" fill="#284ADB" font-family="Verdana, Helvetica, sans-serif"
               font-weight="normal">
        ${protein.ac}
        </tspan>
    </text>
    <text y="75px" font-size="13" fill="#393939" font-family="Verdana, Helvetica, sans-serif">
        <tspan font-weight="bold" x="10px">
            Accession
        </tspan>
        <a xlink:href="http://www.uniprot.org/uniprot/${protein.ac}" target="_top">
            <tspan x="10px" dx="100px" fill="#0072FE" text-decoration="underline"
                   onmouseover="ShowTooltip(evt, '${protein.ac}', 140, 77)"
                   onmouseout="HideTooltip(evt)">${protein.ac}</tspan>
        </a>
        <tspan font-weight="bold" x="10px" dy="30px">
            Species
        </tspan>
        <tspan x="10px" dx="100px" fill="#838383">${protein.taxFullName}</tspan>
        <tspan font-weight="bold" x="10px" dy="30px">
            Length
        </tspan>
    <#--Work out if protein sequence is a fragment or not-->
    <#assign isProteinFragment=protein.proteinFragment />
        <tspan x="10px" dx="100px" fill="#838383">${protein.length} amino acids
        <#if isProteinFragment>(fragment)<#else>(complete)</#if>
        </tspan>
    </text>
    <text x="1084px" y="145px" font-size="10">
        <tspan fill="#525252" font-family="Verdana, Helvetica, sans-serif">Source:</tspan>
        <tspan fill="#838383" font-family="Verdana, Helvetica, sans-serif">UniProtKB/Swiss-Prot</tspan>
    </text>

    <line x1="10px" y1="150px" x2="1220px" y2="150px" stroke="#CDCDCD" stroke-width="1"/>
</svg>