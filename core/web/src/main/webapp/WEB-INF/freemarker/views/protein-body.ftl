<#--Returns main body of protein page for inclusion in DBML-->

<#if protein?? && protein.entries?has_content>

    <#include "protein-header.ftl"/>

<div class="prot_tree">
    <#if (protein.familyEntries?has_content)>
        <div class="prot_tree_desc">
            <h1>Protein family membership:</h1>
        ${protein.familyHierarchy}
        </div>
        <#else>
            <div style="float: left;"><h1>Protein family membership:</h1></div>
            <span style="margin: 6px 0 3px 6px; color:#838383;float:left; font-size:120%;">none</span>
    </#if>
</div>

    <#include "protein-features.ftl"/>
<div class="prot_go">
    <h1>GO Term prediction</h1>

    <div class="go_terms">

        <div class="go_terms_box">
            <h2>Biological Process</h2>
            <#assign hasGo=false/>
            <#list protein.processGoTerms as goTerm>
                <a href="http://www.ebi.ac.uk/QuickGO/GTerm?id=${goTerm.accession}"
                   class="ext">${goTerm.accession}</a> ${goTerm.termName}
                <#assign hasGo=true/>
                <br/>
            </#list>
            <#if !hasGo>
                No biological process GO terms.
            </#if>
        </div>

        <div class="go_terms_box">
            <h2>Molecular Function</h2>
            <#assign hasGo=false/>
            <#list protein.functionGoTerms as goTerm>
                <a href="http://www.ebi.ac.uk/QuickGO/GTerm?id=${goTerm.accession}"
                   class="ext">${goTerm.accession}</a> ${goTerm.termName}
                <#assign hasGo=true/>
                <br/>
            </#list>
            <#if !hasGo>
                No molecular function GO terms.
            </#if>
        </div>
        <div class="go_terms_box">
            <h2>Cellular Component</h2>
            <#assign hasGo=false/>
            <#list protein.componentGoTerms as goTerm>
                <a href="http://www.ebi.ac.uk/QuickGO/GTerm?id=${goTerm.accession}"
                   class="ext">${goTerm.accession}</a> ${goTerm.termName}
                <#assign hasGo=true/>
                <br/>
            </#list>
            <#if !hasGo>
                No cellular component GO terms.
            </#if>
        </div>
    </div>
</div>

    <#else>
    <div>
        Sorry, no match data found for this protein.
    </div>
</#if>
