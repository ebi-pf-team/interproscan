<#-- The protein-header page is used by both the protein and protein-structure freemarker pages when runnning I5
in standalone mode. Also the protein-header is used as a dependency in InterPro web 6.
 -->
<#if protein??>
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

    <#if protein.taxFullName?has_content>
        <#--Taxonomy is known on the protein page, but not on the protein-structure page-->
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
            <#if isProteinFragment>(fragment)<#else>(complete)</#if>
        </div>
    </div>

    <div class="prot_gal_source">Source: <span>UniProtKB/Swiss-Prot</span></div>
    <br/>
    <hr/>
</div>
</#if>
