<#import "../macros/signature.ftl" as signatureMacro>
<#--Returns protein features for inclusion in DBML-->
<#if ! standalone>
    <#include "web_menu_javascript.ftl"/>
</#if>
<#if protein?? && protein.entries?has_content>

    <#if condensedView?? && (condensedView.numSuperMatchBlobs > 0)>
    <div class="prot_sum">
        <#else>
        <div class="prot_sum" style="background:none;">
    </#if>

    <div class="top-row">
        <div class="top-row-id">
            <#if condensedView?? && (condensedView.numSuperMatchBlobs > 0)>
                <h1>Summary view</h1>
            </#if>
        </div>
        <div class="top-row-opt"><a href="#" title="Open sequence summary view in a new window"><span
                class="opt1"></span></a></div>
    </div>

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
                        <span class="scale_bar" style="left:${(scaleMarker?number / protein.length) * 100}%;"
                              title="${scaleMarker}"></span>
                        <span class="scale_numb"
                              style="left:${(scaleMarker?number / protein.length) * 100}%;">${scaleMarker}</span>
                    </#list>
                </div>
            </div>

        </div>
    </div>
</div>

    <div class="prot_entries">
        <h1>Sequence features</h1>
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
                    <#if entry.type?lower_case?starts_with("domain")>
                        c${entryColours[entry.ac]} ${entry.type}
                        <#elseif entry.type?lower_case?starts_with("REPEAT")>
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
                                href="http://wwwdev.ebi.ac.uk/interpro/IEntry?ac=${entry.ac}">${entry.ac}</a></div>
                        <div class="top-row-name"><a href="http://wwwdev.ebi.ac.uk/interpro/IEntry?ac=${entry.ac}"
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
    </div>
</#if>

<#if protein?? && protein.unintegratedSignatures?has_content>

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


    <script type="text/javascript">
        $(document).ready(function() {
            $('span[id*="location-"]').each(
                    function(i) {
                        preparePopup(this.id, ${condensedView.numSuperMatchBlobs});
                    }
            );
        });
    </script>
