<#macro entryInfo entryType reference title entryAccession entryName filling textDecoration height>
<svg>
    <rect x="1px" y="1px" width="110px" height="${height}"
          style="stroke:#D8D8D8;fill:transparent;fill-opacity:0;stroke-width:0.5"/>

    <use x="6" y="6" xlink:href="#${reference}"/>

    <#if !entryType?lower_case?starts_with("unknown")>
    <a xlink:href="http://www.ebi.ac.uk/interpro/entry/${entryAccession}" target="_top">
    </#if>
    <text x="25px" y="15px" text-decoration="${textDecoration}" style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:11px;stroke:none;fill:${filling}">${entryAccession}</text>
    <#if !entryType?lower_case?starts_with("unknown")>
    </a>
    </#if>
</svg>
    <#if !entryType?lower_case?starts_with("unknown")>
    <a xlink:href="http://www.ebi.ac.uk/interpro/entry/${entryAccession}" target="_top">
    </#if>
    <text x="123px" y="14px" text-decoration="${textDecoration}"
          style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:13px;stroke:none;fill:#525252;">${entryName}
    </text>
    <#if !entryType?lower_case?starts_with("unknown")>
    </a>
    </#if>
</#macro>
