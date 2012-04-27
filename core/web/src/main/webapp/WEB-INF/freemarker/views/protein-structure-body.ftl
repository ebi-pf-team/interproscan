<#--Returns main body of protein page for inclusion in DBML-->

<#if protein??>
    <#include "protein-header.ftl"/>
    <#include "protein-structure-features.ftl"/>
<#else>
    <div>
        Sorry, no data found for this protein.
    </div>
</#if>
