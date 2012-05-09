<div class="prot_gal">

    <div class="prot_gal_bloc">
        <div class="prot_gal_col"><h1>Accession</h1></div>
        <div class="prot_gal_desc">
            <#if standalone>
                ${protein.ac}
            <#else>
                <a href="http://www.uniprot.org/uniprot/${protein.ac}" class="ext"
                                          title="${protein.name} (${protein.ac})">${protein.ac}</a> (${protein.id})
            </#if>
        </div>
    </div>
<#if ! standalone && protein.taxFullName?has_content>
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
