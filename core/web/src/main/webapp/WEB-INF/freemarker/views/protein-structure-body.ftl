<#--Returns main body of protein page for inclusion in DBML-->

<#if protein?? && protein.structuralDatabases?has_content>
    <#include "protein-header.ftl"/>
    <#include "protein-structure-features.ftl"/>
<#else>
    <div>
        Sorry, no structural data found for this protein.
    </div>
</#if>
