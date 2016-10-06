<#--Returns main body of protein page for inclusion in the InterProScan 5 HTML output, or elsewhere -->
<#import "../macros/condensedView.ftl" as condensedViewMacro/>
<#import "../macros/residueLocation.ftl" as residueLocationMacro/>
<#import "../macros/signatureText.ftl" as signatureTextMacro>

<#if protein??>

    <#assign proteinLength = protein.length>

        <#if (protein.familyEntries?has_content)>
          <h3>Protein family membership</h3>
          <div class="Family rel_tree">${protein.familyHierarchy}</div>
        <#else>
        <h3>Protein family membership</h3>
        <p>None predicted.</p>
        </#if>


    <#import "../macros/signature.ftl" as signatureMacro>
    <#if ! standalone>
        <#include "web_menu_javascript.ftl"/>
    </#if>

    <h3>Domains and repeats</h3>

    <@condensedViewMacro.condensedView condensedView=condensedView scale=scale entryColours=entryColours/>

    <#if protein.entries?has_content || protein.unintegratedSignatures?has_content>
    <h3>Detailed signature matches</h3>


        <#if protein.entries?has_content>
        <div class="prot_entries">
            <ol class="entries">
                <#list protein.entries as entry>
                    <#-- Prepare required variables for this entry -->
                <#--Use unintegrated signature icon if unknown (probably needs own icon)-->
                    <#assign icon>
                        <#if entry.type?lower_case?starts_with("family") || entry.type?lower_case?starts_with("domain") || entry.type?lower_case?starts_with("region") || entry.type?lower_case?starts_with("repeat")>
                        ${entry.type?lower_case}
                        <#elseif entry.type?lower_case?starts_with("unknown")>uni
                        <#else>site
                        </#if>
                    </#assign>
                    <#assign icon=icon?trim>
                    <#assign title=entry.type?replace("_"," ")>
                    <#assign colourClass>
                    <#--For cases where the entry colour file is out of sync, we need to check that entryColours[entry.ac] exists -->
                        <#if entryColours[entry.ac]?? && (entry.type?lower_case?starts_with("domain") || entry.type?lower_case?starts_with("repeat"))>
                            c${entryColours[entry.ac]} ${entry.type}
                        <#else>
                            ${entry.type}
                        </#if>
                    </#assign>
                    <#assign colourClass=colourClass?trim>

                    <#assign containerId=entry.ac+"-signatures">

                    <#-- Now display the entry on the page using these variables -->
                    <li class="entry ${entry.type}-row">

                        <div class="top-row">
                            <div class="top-row-id"><img src="${img_resource_path}/images/ico_type_${icon}_small.png"
                                                         alt="${title}" title="${title}"/> <a
                                    href="http://www.ebi.ac.uk/interpro/entry/${entry.ac}">${entry.ac}</a></div>
                            <div class="top-row-name"><a href="http://www.ebi.ac.uk/interpro/entry/${entry.ac}"
                                                         class="neutral">${entry.name}</a></div>
                        </div>

                        <div class="bot-row">
                            <div class="bot-row-line-top"></div>
                            <ol class="signatures" style="border:0px solid pink;">

                                <#list entry.signatures as signature>

                                    <li id="${containerId}" class="signature entry-signatures">
                                        <@signatureMacro.signature proteinLength=proteinLength signature=signature entryTypeTitle=title scale=scale entryAc=entry.ac colourClass=colourClass />
                                    </li>
                                </#list>
                            </ol>
                            <div class="bot-row-line-bot"></div>
                        </div>
                    </li>
                </#list>
            </ol>
        </div>
        </#if>

        <#if protein.unintegratedSignatures?has_content>

            <div class="prot_entries" id="uni_sign">
                <div class="top-row">
                    <div class="top-row-id"><img src="${img_resource_path}/images/ico_type_uni_small.png"
                                                 alt="Unintegrated signatures" title="Unintegrated signatures"/> no IPR
                    </div>
                    <div class="top-row-name">Unintegrated signatures</div>
                </div>
                <div class="bot-row">
                    <div class="bot-row-line-top"></div>
                    <ol class="signatures">
                        <#list protein.unintegratedSignatures as signature>
                            <li class="signature">
                                <@signatureMacro.signature proteinLength=proteinLength signature=signature entryTypeTitle="Unintegrated" scale=scale entryAc="null" colourClass="uni" />
                            </li>
                        </#list>
                    </ol>
                    <div class="bot-row-line-bot"></div>
                </div>
            </div>
        </#if>

        <#--Per residue features-->
        <#if protein.sites?has_content>
            <#global residueId=0>
            <h3>Per residue annotation</h3>
            <#--<div class="prot_sum">-->
            <div class="prot_sum">
                <div class="bot-row">
                    <div class="bot-row-line-top"></div>
                    <ol class="signatures">
                            <#list protein.sites as site>
                            <li class="signature">
                                <@signatureTextMacro.signatureText signature=site.signature/>

                                <div class="bot-row-line">
                            <#--<div class="matches">-->
                            <div class="matches">
                                <#list site.siteLocations as residueMatch>
                                    <#global residueId=residueId + 1>
                                        <@residueLocationMacro.residueLocation residueId=residueId proteinLength=proteinLength residue=residueMatch site=site colourClass="uni" />
                                </#list>

                            <#--Draw in scale markers for this line-->
                                <#list scale?split(",") as scaleMarker>
                                    <span class="grade" style="left:${(scaleMarker?number?int / proteinLength) * 100}%;" title="${scaleMarker}"></span>
                                </#list>

                            </div>
                            </div>
                            </li>
                            </#list>
                    </ol>
                    <div class="bot-row-line-bot"></div>
                </div>

            </div>

        </#if>


    <#else>
        <#-- No matches so the detailed matches section is omitted. -->
    </#if>

<h3>GO term prediction</h3>

<div class="prot_go">

    <div class="go_terms">
        <h4>Biological Process</h4>
        <div class="go_terms_box">

            <#assign hasGo=false/>
            <p><#list protein.processGoTerms as goTerm>
            <a href="http://www.ebi.ac.uk/QuickGO/GTerm?id=${goTerm.accession}"
                   class="ext">${goTerm.accession}</a> ${goTerm.termName}
                <#assign hasGo=true/>
                <br/>
            </#list></p>
            <#if !hasGo>
                <p>None predicted.</p>
            </#if>
        </div>

        <h4>Molecular Function</h4>
        <div class="go_terms_box">

            <#assign hasGo=false/>
        <p><#list protein.functionGoTerms as goTerm>
              <a href="http://www.ebi.ac.uk/QuickGO/GTerm?id=${goTerm.accession}"
                   class="ext">${goTerm.accession}</a> ${goTerm.termName}
                <#assign hasGo=true/>
                <br/>
            </#list></p>
            <#if !hasGo>
                <p>None predicted.</p>
            </#if>
        </div>

        <h4>Cellular Component</h4>
        <div class="go_terms_box">

            <#assign hasGo=false/>
        <p><#list protein.componentGoTerms as goTerm>
                <a href="http://www.ebi.ac.uk/QuickGO/GTerm?id=${goTerm.accession}"
                   class="ext">${goTerm.accession}</a> ${goTerm.termName}
                <#assign hasGo=true/>
                <br/>
            </#list></p>
            <#if !hasGo>
                <p>None predicted.</p>
            </#if>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(document).ready(function () {
        <#if standalone>
            // Use hidden DIVs to display popups
            $('span[id*="span-"]').each(
                    function(i) {
                        <#if condensedView??>
                            preparePopup(this.id, ${condensedView.numSuperMatchBlobs});
                        <#else>
                            // No supermatches for this protein, but there are structural matches (e.g. B7ZMM2 as of InterPro release 37.0)
                            preparePopup(this.id, 0);
                        </#if>
                    }
            );
        <#else>
            // Use AJAX call to display popups
            $('a[id*="location-"]').each(
                    function(i) {
                        preparePopup(this.id);
                    }
            );
        </#if>
    });
</script>

<#else>
<b>No match data found for this protein.</b>
</#if>
