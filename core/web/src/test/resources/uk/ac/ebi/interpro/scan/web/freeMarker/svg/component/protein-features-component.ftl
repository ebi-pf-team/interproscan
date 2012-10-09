<#--Protein features component-->
<#import "../macros/signature.ftl" as signatureMacro>
<#import "../macros/entryInfo.ftl" as entryInfoMacro>
<svg id="sequenceFeatures" x="40" y="${yPosition}">
    <text y="13px"
          style="fill:#393939;font-family:Verdana,Helvetica,sans-serif;font-size:13px;font-weight:bold">
        Sequence features
    </text>
<#assign entryComponentYPosition=30/>
<#assign entryInfoHeight=18/>
<#list protein.entries as entry>
<#--Icon and title definition-->
    <#include "protein-features-component-icon-def.ftl"/>
    <#assign greyBoxYDimension=19/>
    <#assign entryComponentHeight=entryInfoHeight+greyBoxYDimension+entry.getEntryComponentHeightForSVG(17,20)/>
    <svg y="${entryComponentYPosition}" width="1210" height="${entryComponentHeight}"
         viewBox="0 0 1210 ${entryComponentHeight}">
    <#--<svg id="entryComponent" y="${entryComponentYDimension}px">-->
    <@entryInfoMacro.entryInfo entryType=entry.type resource="resources/icons/ico_type_${icon}_small.png"
    title=title entryAccession=entry.ac entryName=entry.name filling="#0072FE" textDecoration="underline" height=entryInfoHeight/>
        <#--<#assign greyBoxYDimension=19/>-->
        <#assign signatureLinkYDimension=20/>
        <#list entry.signatures as signature>
    <@signatureMacro.signature signature=signature entryType=entry.type entryAccession=entry.ac greyBoxYDimension=greyBoxYDimension signatureLinkYDimension=signatureLinkYDimension/>
        <#assign greyBoxYDimension=greyBoxYDimension+17/>
        <#assign signatureLinkYDimension=signatureLinkYDimension+17/>
    </#list>
    </svg>
    <#assign entryComponentYPosition=entryComponentYPosition+entryComponentHeight/>
</#list>
<#assign entryComponentHeight=entryInfoHeight+greyBoxYDimension+protein.getUnintegratedSignaturesComponentHeightForSVG(17,20)/>
<#if protein?? && protein.unintegratedSignatures?has_content>
    <svg id="unintegratedSignatures" y="${entryComponentYPosition}" width="1210" height="${entryComponentHeight}"
         viewBox="0 0 1210 ${entryComponentHeight}">
    <#--<svg id="unintegratedSignatures" y="${entryComponentYDimension}px">-->
    <@entryInfoMacro.entryInfo entryType="Unknown" resource="resources/icons/ico_type_uni_small.png"
    title="Unintegrated signatures" entryAccession="no IPR" entryName="Unintegrated signatures" filling="#525252"
    textDecoration="none" height=entryInfoHeight/>
        <#assign greyBoxYDimension=19/>
        <#assign signatureLinkYDimension=20/>
        <#list protein.unintegratedSignatures as signature>
    <@signatureMacro.signature signature=signature entryType="Unknown" entryAccession="" greyBoxYDimension=greyBoxYDimension signatureLinkYDimension=signatureLinkYDimension/>
        <#assign greyBoxYDimension=greyBoxYDimension+17/>
        <#assign signatureLinkYDimension=signatureLinkYDimension+17/>
    </#list>
    </svg>
</#if>
<#assign proteinFeaturesComponentHeight=entryComponentYPosition+entryComponentHeight/>
</svg>