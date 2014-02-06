<#--Returns main body of protein page for inclusion in the InterProScan 5 HTML output, or elsewhere -->

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
    <#if condensedView?? && (condensedView.numSuperMatchBlobs > 0)>
    <div class="prot_sum">
    <#else>
    <div class="prot_sum" style="background:none;">
    </#if>



    <#if condensedView?? && (condensedView.numSuperMatchBlobs > 0)>
    <div class="bot-row">
        <div class="bot-row-line-top"></div>
        <ol class="signatures">

            <#include "condensed-view.ftl"/>

        </ol>
        <div class="bot-row-line-bot"></div>
    </div>

    <div class="prot_scale">
        <div class="bot-row">

            <div class="bot-row-line">
                <div style="position:relative;">
                    <!-- Position marker lines -->
                    <#list scale?split(",") as scaleMarker>
                        <!-- to build an exception for 0 -->
                        <#if scaleMarker?number == 0>
                            <span class="scale_bar" style="left:${(scaleMarker?number / proteinLength) * 100}%;"
                                  title="1"></span>
                                <span class="scale_numb"
                                      style="left:${(scaleMarker?number / proteinLength) * 100+1}%;">1</span>
                        <#else>
                            <span class="scale_bar" style="left:${(scaleMarker?number / proteinLength) * 100}%;"
                                  title="${scaleMarker}"></span>
                                <span class="scale_numb"
                                      style="left:${(scaleMarker?number / proteinLength) * 100}%;">${scaleMarker}</span>
                        </#if>
                    </#list>
                </div>
            </div>

        </div>
    </div>

    <#else>
    <div class="bot-row">
        None predicted.
    </div>
    </#if>
</div> <!-- Closing the prot_sum DIV -->

    <#if protein.entries?has_content || protein.unintegratedSignatures?has_content>
    <h3>Detailed signature matches</h3>


        <#if protein.entries?has_content>
        <div class="prot_entries">
            <ol class="entries">
                <#list protein.entries as entry>
                    <!-- Prepare required variables for this entry -->
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
                    <#--TODO: Check domain and repeat in the same if clause-->
                        <#if entry.type?lower_case?starts_with("domain")>
                            c${entryColours[entry.ac]} ${entry.type}
                        <#elseif entry.type?lower_case?starts_with("repeat")>
                            c${entryColours[entry.ac]} ${entry.type}
                        <#else>
                        ${entry.type}
                        </#if>
                    </#assign>
                    <#assign colourClass=colourClass?trim>

                    <#assign containerId=entry.ac+"-signatures">

                    <!-- Now display the entry on the page using these variables -->
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
                                        <@signatureMacro.signature proteinLength=proteinLength signature=signature entryTypeTitle=title colourClass=colourClass />
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
                                <@signatureMacro.signature proteinLength=proteinLength signature=signature entryTypeTitle="Unintegrated" colourClass="uni" />
                            </li>
                        </#list>
                    </ol>
                    <div class="bot-row-line-bot"></div>
                </div>
            </div>
        </#if>

    <#else>
        <!-- No matches so the detailed matches section is omitted. -->
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
        $('span[id*="location-"]').each(
                function (i) {
                    preparePopup(this.id, ${condensedView.numSuperMatchBlobs});
                }
        );
    });
</script>

<#else>
<b>No match data found for this protein.</b>
</#if>
