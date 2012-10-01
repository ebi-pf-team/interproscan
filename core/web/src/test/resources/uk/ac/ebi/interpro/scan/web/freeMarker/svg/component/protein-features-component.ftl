<#--Protein features component-->
<#import "../macros/signature.ftl" as signatureMacro>
<#import "../macros/entryInfo.ftl" as entryInfoMacro>
<svg id="sequenceFeatures" x="40px" y="500px">
    <text y="13px"
          style="fill:#393939;font-family:Verdana,Helvetica,sans-serif;font-size:13px;font-weight:bold">
        Sequence features
    </text>
<#assign entryComponentYDimension=30/>
<#list protein.entries as entry>
<#--Icon and title definition-->
    <#include "protein-features-component-icon-def.ftl"/>
    <#--<svg id="entryComponent" y="${entryComponentYDimension}px" width="1210" height="100" viewBox="0 0 1210 100">-->
        <svg id="entryComponent" y="${entryComponentYDimension}px">
    <@entryInfoMacro.entryInfo entryType=entry.type resource="resources/icons/ico_type_${icon}_small.png"
    title=title entryAccession=entry.ac entryName=entry.name filling="#0072FE" textDecoration="underline"/>
        <#assign greyBoxYDimension=19/>
        <#assign signatureLinkYDimension=20/>
        <#list entry.signatures as signature>
    <@signatureMacro.signature signature=signature entryType=entry.type entryAccession=entry.ac greyBoxYDimension=greyBoxYDimension signatureLinkYDimension=signatureLinkYDimension/>
        <#assign greyBoxYDimension=greyBoxYDimension+17/>
        <#assign signatureLinkYDimension=signatureLinkYDimension+17/>
    </#list>
    </svg>
    <#assign entryComponentYDimension=entryComponentYDimension+77/>
</#list>
<#if protein?? && protein.unintegratedSignatures?has_content>
    <#--<svg id="unintegratedSignatures" y="${entryComponentYDimension}px" width="1210" height="200" viewBox="0 0 1210 200">-->
        <svg id="unintegratedSignatures" y="${entryComponentYDimension}px">
    <@entryInfoMacro.entryInfo entryType="Unknown" resource="resources/icons/ico_type_uni_small.png"
    title="Unintegrated signatures" entryAccession="no IPR" entryName="Unintegrated signatures" filling="#525252"
    textDecoration="none"/>
        <#assign greyBoxYDimension=19/>
        <#assign signatureLinkYDimension=20/>
        <#list protein.unintegratedSignatures as signature>
    <@signatureMacro.signature signature=signature entryType="Unknown" entryAccession="" greyBoxYDimension=greyBoxYDimension signatureLinkYDimension=signatureLinkYDimension/>
        <#assign greyBoxYDimension=greyBoxYDimension+17/>
        <#assign signatureLinkYDimension=signatureLinkYDimension+17/>
    </#list>
    </svg>
</#if>
</svg>