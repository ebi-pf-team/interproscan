<#-- The protein-header page is used by both the protein and protein-structure freemarker pages when runnning I5
in standalone mode. Also the protein-header is used as a dependency in InterPro web 6.
 -->
<#if protein??>
<div class="prot_gal">

    <div class="prot_gal_bloc">
        <div class="prot_gal_col"><b>Accession</b></div>
        <div class="prot_gal_desc">
            <#if standalone>
                ${protein.ac}
            <#else>
                <a href="http://www.uniprot.org/uniprot/${protein.ac}" class="ext"
                                          title="View this protein in UniProtKB">${protein.ac}</a> (${protein.id})
            </#if>
        </div>
    </div>

    <#if protein.taxFullName?has_content && protein.taxFullName != "Unknown">
        <#--Taxonomy is known on the InterPro web 6 protein page, but not in InterProScan standalone mode, nor on the
        protein-structure page -->
        <div class="prot_gal_bloc">
            <div class="prot_gal_col"><b>Species</b></div>
            <div class="prot_gal_desc">${protein.taxFullName}</div>
        </div>
    </#if>

    <#--Work out if protein sequence is a fragment or not-->
    <#assign isProteinFragment=protein.proteinFragment />
    <div class="prot_gal_bloc">
        <div class="prot_gal_col"><b>Length</b></div>
        <div class="prot_gal_desc">${protein.length} amino acids
            <#if isProteinFragment>(fragment)<#else>(complete)</#if>
        </div>
    </div>

    <div class="prot_gal_source">Source: <span>UniProtKB</span></div>
    <br/>
    <hr/>
</div>
</#if>
