<#macro signatureText signature proteinAc>
<div class="bot-row-signame"><#-- link to modify -->
<#--Setup variables ready for displaying signature information,-->
<#--e.g. could condense "G3DSA:2.40.20.10 (Pept_Ser_Cys)" to "G3DSA:2.40.2... (Pept_S...)".-->
<#--NOTE: PLEASE ENSURE THAT maxOverallLength >= maxAcLength + 4-->
<#assign maxAcLength=16><#--Maximum signature accession length-->
<#assign maxOverallLength=20> <#-- Maximum allowed length of accession and name -->
<#assign acLength=signature.ac?length> <#-- Actual length of the signature accession, e.g. G3DSA:2.40.20.10 -->
<#if signature.name??>
    <#assign nameLength=signature.name?length> <#-- Actual length of the signature name, e.g. Pept_Ser_Cys -->
<#else>
    <#assign nameLength=0> <#-- No signature name -->
</#if>
<#assign maxNameLength=maxOverallLength?number - acLength?number> <#-- Initialise the maximum allowed length of name -->
<#if ((acLength?number) > (maxAcLength?number))>
<#-- Accession was too long therefore should actually subtract maxAcLength instead of acLength -->
    <#assign maxNameLength=maxOverallLength?number - maxAcLength?number>
</#if>


    <#assign linkUrl=signature.dataSource.getLinkUrl(signature.ac)>
    <#if signature.ac?starts_with("mobidb")>
        <!-- Always link using signature accession except for MobiDB: in InterProScan HTML output show homepage, in web protein page show use protein accession-->
        <#if standalone>
            <#assign linkUrl=signature.dataSource.getHomeUrl()>
        <#else>
            <#assign linkUrl=signature.dataSource.getLinkUrl(proteinAc)>
        </#if>
    </#if>


<#-- Now display the signature accession -->
    <#if signature.name?? && signature.ac != signature.name>
<#--Link text may be abbreviated therefore need to display the full text in the link title-->
<a href="${linkUrl}" title="${signature.ac} (${signature.name})"
   class="neutral">
<#else>
<a href="${linkUrl}" title="${signature.ac}"
   class="neutral">
</#if>
<#if ((acLength?number) > (maxAcLength?number))>
<#--Accession is too long, need to truncate it-->
${signature.ac?substring(0,maxAcLength - 3)}...
<#else>
${signature.ac}
</#if>
</a>

<#-- Now display the signature name (if not identical to the accession) -->
<#if signature.name?? && signature.ac != signature.name>
    <#if ((nameLength?number) > (maxNameLength?number))>
    <#--Name is too long, need to truncate it-->
        <span>(${signature.name?substring(0, maxNameLength - 3)}...)</span>
    <#else>
        <span>(${signature.name})</span>
    </#if>
</#if>

</div>
</#macro>