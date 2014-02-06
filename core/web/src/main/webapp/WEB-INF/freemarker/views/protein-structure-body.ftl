<#--Returns main body of protein page for inclusion in DBML-->

<#if protein?? && protein.structuralDatabases?has_content>

<#--Returns protein structure features for inclusion in DBML-->
    <#import "../macros/structuralLocation.ftl" as structuralLocationMacro/>
    <#assign proteinLength = protein.length>

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
                                      style="left:${(scaleMarker?number / proteinLength) * 100}%;">1</span>
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
    <div class="bot-row">None predicted.</div>
    </#if>

</div> <!-- Closing the prot_sum DIV -->

    <#if (protein.structuralFeatures?has_content || protein.structuralPredictions?has_content)>
        <#global locationId=0>

        <#if ((protein.structuralFeatures?size>0) || (protein.structuralPredictions?size>0))>
            <h3>Structural features and predictions</h3>

            <div class="prot_entries">

                <#if (protein.structuralFeatures?size>0) >
                    <ol class="entries">
                        <li class="entry">
                            <div class="top-row">
                                <div class="top-row-id"> no IPR</div>
                                <div class="top-row-name">Structural features</div>
                            </div>
                            <div class="bot-row">
                                <div class="bot-row-line-top"></div>
                                <ol class="signatures">
                                    <#list protein.structuralFeatures as feature>
                                        <li>

                                            <!--
                                                Structural match data structure:

                                                database (SimpleStructuralMatchDatabase)
                                                    databaseName (String)
                                                    structuralMatches (Map of SimpleLocation to SimpleStructuralMatchData)

                                                location (SimpleLocation)
                                                    start (int)
                                                    end (int)

                                                locationData (SimpleStructuralMatchData)
                                                    locationDataMap (Map of String to  List of Strings)

                                            -->
                                            <div class="bot-row-signame">${feature.dataSource.sourceName}</div>
                                            <div class="bot-row-line">
                                                <div class="matches">

                                                    <#assign structuralMatchesMap = feature.structuralMatches>
                                <#list structuralMatchesMap?keys as key>
                                                    <#assign location=key>
                                                    <#assign locationId=locationId?number?int + 1>
                                                    <@structuralLocationMacro.structuralLocation smid=locationId proteinLength=proteinLength location=location structuralMatchData=feature.getSimpleStructuralMatchData(key) databaseMetadata=feature.dataSource />
                                                </#list>

                                                </div>
                                            </div>
                                        </li>
                                    </#list>
                                </ol>
                                <div class="bot-row-line-bot"></div>
                            </div>
                        </li>
                    </ol>
                </#if>

                <#if (protein.structuralPredictions?size>0)>
                    <ol class="entries">
                        <li class="entry">
                            <div class="top-row">
                                <div class="top-row-id"> no IPR</div>
                                <div class="top-row-name">Structural predictions</div>
                            </div>
                            <div class="bot-row">
                                <div class="bot-row-line-top"></div>
                                <ol class="signatures">

                                    <#list protein.structuralPredictions as feature>
                                        <li>
                                            <!--
                                                Structural match data structure:

                                                database (SimpleStructuralMatchDatabase)
                                                    databaseName (String)
                                                    structuralMatches (Map of SimpleLocation to SimpleStructuralMatchData)

                                                location (SimpleLocation)
                                                    start (int)
                                                    end (int)

                                                locationData (SimpleStructuralMatchData)
                                                    locationDataMap (Map of String to a List of Strings)

                                            -->

                                            <div class="bot-row-signame">${feature.dataSource.sourceName}</div>
                                            <div class="bot-row-line">
                                                <div class="matches">

                                                    <#assign structuralMatchesMap = feature.structuralMatches>
                            <#list structuralMatchesMap?keys as key>
                                                    <#assign location=key>
                                                    <#assign locationId=locationId?number?int + 1>
                                                    <@structuralLocationMacro.structuralLocation smid=locationId proteinLength=proteinLength location=key structuralMatchData=feature.getSimpleStructuralMatchData(key) databaseMetadata=feature.dataSource/>
                                                </#list>


                                        </li>
                                    </#list>
                                </ol>
                                <div class="bot-row-line-bot"></div>
                        </li>
                    </ol>
                </#if>
            </div>
        </#if>

        <script type="text/javascript">

            // Tie the extra popup DIV to it's match SPAN
            $(document).ready(function() {
                $('span[id*="location-"]').each(
                        function(i) {
                            <#if condensedView??>
                                preparePopup(this.id, ${condensedView.numSuperMatchBlobs});
                            <#else>
                                // No supermatches for this protein, but there are structural matches (e.g. B7ZMM2 as of InterPro release 37.0)
                                preparePopup(this.id, 0);
                            </#if>
                        }
                );
            });
        </script>
    <#else>
    <#--Nothing to display!-->
        <!-- No structural matches so the detailed matches section is omitted. -->
    </#if>
<#else>
    <b>No structural match data found for this protein.</b>
</#if>
