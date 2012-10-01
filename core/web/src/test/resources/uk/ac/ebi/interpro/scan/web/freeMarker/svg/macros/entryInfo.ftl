<#macro entryInfo entryType resource title entryAccession entryName filling textDecoration>
<svg>
    <rect x="1px" y="1px" width="110px" height="18px"
          style="stroke:#D8D8D8;fill:transparent;stroke-width:0.5"/>
    <image x="3px" y="4px" width="13px" height="13px"
           xlink:href="${resource}"><title>${title}</title>
    </image>
    <#if !entryType?lower_case?starts_with("unknown")>
    <a xlink:href="http://www.ebi.ac.uk/interpro/IEntry?ac=${entryAccession}" target="_top">
    </#if>
    <text x="25px" y="15px" text-decoration="${textDecoration}" style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:11px;stroke:none;fill:${filling}">${entryAccession}</text>
    <#if !entryType?lower_case?starts_with("unknown")>
    </a>
    </#if>
</svg>
    <#if !entryType?lower_case?starts_with("unknown")>
    <a xlink:href="http://www.ebi.ac.uk/interpro/IEntry?ac=${entryAccession}" target="_top">
    </#if>
    <text x="123px" y="14px" text-decoration="${textDecoration}"
          style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:13px;stroke:none;fill:#525252;">${entryName}
    </text>
    <#if !entryType?lower_case?starts_with("unknown")>
    </a>
    </#if>
</#macro>