<#--Returns main body of protein page for inclusion in DBML-->

<#if protein?? && protein.structuralDatabases?has_content>

<#--Returns protein structure features for inclusion in DBML-->
    <#import "../macros/structuralLocation.ftl" as structuralLocationMacro/>

    <#if (protein.structuralFeatures?has_content || protein.structuralPredictions?has_content)>
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
                        <!-- to build an exception for 0 -->
                        <#list scale?split(",") as scaleMarker>
                            <span class="scale_bar" style="left:${(scaleMarker?number?int / protein.length) * 100}%;"
                                  title="${scaleMarker}"></span>
                        <span class="scale_numb"
                              style="left:${(scaleMarker?number?int / protein.length) * 100}%;">${scaleMarker}</span>
                        </#list>
                    </div>
                </div>

            </div>
        </div>
    </div>

        <#global locationId=0>

        <#if (protein.structuralFeatures?size>0) >

            <div class="prot_entries">
                <h1>Sequence features</h1>
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
                                            -- databaseName (String)
                                            -- structuralMatches (Map<SimpleLocation, SimpleStructuralMatchData>)

                                            location (SimpleLocation)
                                            -- start (int)
                                            -- end (int)

                                            locationData (SimpleStructuralMatchData)
                                            -- locationDataMap (Map<String, List<String>>)
                                        -->
                                        <div class="bot-row-signame">${feature.dataSource.sourceName}</div>
                                        <div class="bot-row-line">
                                            <div class="matches">

                                                <#assign structuralMatchesMap = feature.structuralMatches>
                                <#list structuralMatchesMap?keys as key>
                                                <#assign location=key>
                                                <#assign locationId=locationId?number?int + 1>
                                            <@structuralLocationMacro.structuralLocation smid=locationId protein=protein location=location structuralMatchData=feature.getSimpleStructuralMatchData(key) databaseMetadata=feature.dataSource />
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
            </div>
        </#if>

        <#if (protein.structuralPredictions?size>0)>
            <div class="prot_entries" id="uni">
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
                                            -- databaseName (String)
                                            -- structuralMatches (Map<SimpleLocation, SimpleStructuralMatchData>)

                                            location (SimpleLocation)
                                            -- start (int)
                                            -- end (int)

                                            locationData (SimpleStructuralMatchData)
                                            -- locationDataMap (Map<String, List<String>>)
                                        -->

                                        <div class="bot-row-signame">${feature.dataSource.sourceName}</div>
                                        <div class="bot-row-line">
                                            <div class="matches">

                                                <#assign structuralMatchesMap = feature.structuralMatches>
                            <#list structuralMatchesMap?keys as key>
                                                <#assign location=key>
                                                <#assign locationId=locationId?number?int + 1>
                                            <@structuralLocationMacro.structuralLocation smid=locationId protein=protein location=key structuralMatchData=feature.getSimpleStructuralMatchData(key) databaseMetadata=feature.dataSource/>
                                            </#list>


                                    </li>
                                </#list>
                            </ol>
                            <div class="bot-row-line-bot"></div>
                    </li>
                </ol>
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
            <b>No structural data found for this protein.</b>
    </#if>
    <#else>
        <b>No structural match data found for this protein.</b>
</#if>
