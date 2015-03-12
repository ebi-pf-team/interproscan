<#assign icon>
    <#if entry.type?lower_case?starts_with("family") || entry.type?lower_case?starts_with("domain") || entry.type?lower_case?starts_with("region") || entry.type?lower_case?starts_with("repeat")>
    ${entry.type?lower_case}
        <#elseif entry.type?lower_case?starts_with("unknown")>uni
        <#else>site
    </#if>
</#assign>
<#assign icon=icon?trim>
<#assign title=entry.type?replace("_"," ")>
<#assign colourClass>
    <#if entry.type?lower_case?starts_with("domain")>
    c${entryColours[entry.ac]} ${entry.type}
        <#elseif entry.type?lower_case?starts_with("repeat")>
        c${entryColours[entry.ac]} ${entry.type}
        <#else>
        ${entry.type}
    </#if>
</#assign>