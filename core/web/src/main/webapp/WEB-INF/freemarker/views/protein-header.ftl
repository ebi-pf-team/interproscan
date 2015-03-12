<#-- The protein-header page is used by both the protein and protein-structure freemarker pages when runnning I5
in standalone mode. Also the protein-header is used as a dependency in InterPro web 6.
Note that the majority of this data is unavailable in standalone mode since InterProScan 5 does not have access to the
UniProt data or the data warehouse used by InterPro web 6!
 -->
<#if protein??>
<div class="prot_gal">

    <#if !standalone>
        <div class="prot_gal_bloc">
            <div class="prot_gal_col"><b>Accession</b></div>
            <div class="prot_gal_desc">
                <a href="http://www.uniprot.org/uniprot/${protein.ac}" class="ext"
                   title="View this protein in UniProtKB">${protein.ac}</a>
                <#if protein.id?has_content> (${protein.id})</#if>
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
    </#if>

<#--Work out if protein sequence is a fragment or not-->
    <div class="prot_gal_bloc">
        <div class="prot_gal_col"><b>Length</b></div>
        <div class="prot_gal_desc">${protein.length} amino acids
            <#if !standalone>
                <#assign isProteinFragment=protein.proteinFragment />
                <#if isProteinFragment>(fragment)<#else>(complete)</#if>
            </#if>
        </div>
    </div>

    <#if !standalone>
        <div class="prot_gal_source">Source: <span>UniProtKB</span></div>
    </#if>
    <br/>
    <hr/>
</div>
</#if>
