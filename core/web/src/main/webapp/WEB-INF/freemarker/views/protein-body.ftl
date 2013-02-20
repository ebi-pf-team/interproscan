<#--Returns main body of protein page for inclusion in the InterProScan 5 HTML output, or elsewhere -->

<#if protein??>

<div class="prot_tree">
    <div class="prot_tree_desc">
        <#if (protein.familyEntries?has_content)>
            <h1>Protein family membership:</h1>
        ${protein.familyHierarchy}
        <#else>
        <div style="float: left;"><h1>Protein family membership:</h1></div>
        <span style="margin: 6px 0 3px 6px; color:#838383;float:left; font-size:120%;">None predicted.</span>
        </#if>
    </div>
</div>

    <#import "../macros/signature.ftl" as signatureMacro>
    <#if ! standalone>
        <#include "web_menu_javascript.ftl"/>
    </#if>

    <#if condensedView?? && (condensedView.numSuperMatchBlobs > 0)>
    <div class="prot_sum">
    <#else>
    <div class="prot_sum" style="background:none;">
    </#if>
<div class="top-row">
    <div class="top-row-id">
        <h1>Domains and repeats:</h1>
    </div>
    <div class="top-row-opt"><a href="#" title="Open domains and repeats view in a new window"><span
            class="opt1"></span></a></div>
</div>

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
                            <span class="scale_bar" style="left:${(scaleMarker?number / protein.length) * 100}%;"
                                  title="1"></span>
                                <span class="scale_numb"
                                      style="left:${(scaleMarker?number / protein.length) * 100}%;">1</span>
                        <#else>
                            <span class="scale_bar" style="left:${(scaleMarker?number / protein.length) * 100}%;"
                                  title="${scaleMarker}"></span>
                                <span class="scale_numb"
                                      style="left:${(scaleMarker?number / protein.length) * 100}%;">${scaleMarker}</span>
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
    <div class="prot_entries" style="overflow: auto;">
        <h1>Detailed signature matches</h1>
        <#if protein.entries?has_content>
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
                        <#else>               `
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
                                    href="http://www.ebi.ac.uk/interpro/IEntry?ac=${entry.ac}">${entry.ac}</a></div>
                            <div class="top-row-name"><a href="http://www.ebi.ac.uk/interpro/IEntry?ac=${entry.ac}"
                                                         class="neutral">${entry.name}</a></div>
                        </div>

                        <div class="bot-row">
                            <div class="bot-row-line-top"></div>
                            <ol class="signatures" style="border:0px solid pink;">

                                <#list entry.signatures as signature>

                                    <li id="${containerId}" class="signature entry-signatures">
                                        <@signatureMacro.signature protein=protein signature=signature entryTypeTitle=title colourClass=colourClass />
                                    </li>
                                </#list>
                            </ol>
                            <div class="bot-row-line-bot"></div>
                        </div>
                    </li>
                </#list>
            </ol>
        </#if>

        <#if protein.unintegratedSignatures?has_content>

            <div class="prot_entries" id="uni">
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
                                <@signatureMacro.signature protein=protein signature=signature entryTypeTitle="Unintegrated" colourClass="uni" />
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
                None predicted.
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
                None predicted.
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
                None predicted.
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
