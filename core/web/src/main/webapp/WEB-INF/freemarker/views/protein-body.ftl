<#--Returns main body of protein page for inclusion in DBML-->
<#-- remove the option for export
<div class="r_option">
    <a href="ISpy?ac={spy:proteinAc}&amp;mode=fasta" title="Download protein sequences in FASTA format" class="export"
       target="_blank">
        <div class="exp_fasta"></div>
        Export FASTA
    </a>

    <form name="bioMartForm" action="http://www.ebi.ac.uk/interpro/biomart/martservice" method="get"
          target="_blank">
        <!--
        Use a GET so the user can bookmark/link to the results page if they desire.
        If you change the query parameter text in the future then please ensure that the maximum
        URL length does not go beyond 2,083 characters (won't work in IE). We're fine at the moment!
        &ndash;&gt;
        <input type="hidden" id="query" name="query" value="{pp:get_supermatch_BioMartQuery}"/>
    </form>
    <a href="javascript: void(0);" onclick="document.bioMartForm.submit();return false;"
       title="Download protein match data as a Text file"
       class="export">
        <div class="exp_tsv"></div>
        Export list TSV</a>
</div>-->

<div class="tab">
    <div class="Protein_tab">Protein</div>
</div>

<div class="main-box">
    <h1>
    ${protein.name} <span>(${protein.ac})</span>
    </h1>

    <div class="prot_gal">

        <div class="prot_gal_bloc">
            <div class="prot_gal_col"><h1>Accession</h1></div>
            <div class="prot_gal_desc"><a href="http://www.uniprot.org/uniprot/${protein.ac}" class="ext"
                                          title="${protein.name} (${protein.ac})">${protein.ac}</a> (${protein.id})
            </div>
        </div>
    <#if ! standalone>
        <div class="prot_gal_bloc">
            <div class="prot_gal_col"><h1>Species</h1></div>
            <div class="prot_gal_desc">${protein.taxFullName}</div>
        </div>
    </#if>
    <#--Work out if protein sequence is a fragment or not-->
    <#assign isProteinFragment=protein.proteinFragment />
        <div class="prot_gal_bloc">
            <div class="prot_gal_col"><h1>Length</h1></div>
            <div class="prot_gal_desc">${protein.length} amino acids
            <#--Work out if protein sequence is a fragment or not-->
            <#if isProteinFragment>(fragment)</#if>
            </div>
        </div>
    <#if ! standalone>
        <div class="prot_gal_source">Source: <span>UniProtKB/Swiss-Prot</span></div>
    </#if>
        <hr/>
    </div>
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
</div>
