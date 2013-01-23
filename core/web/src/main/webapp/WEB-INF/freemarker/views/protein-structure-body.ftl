<#--Returns main body of protein structure page for inclusion in the InterProScan 5 HTML output, or elsewhere -->
<#import "../macros/structuralLocation.ftl" as structuralLocationMacro/>

<#if protein??>

    <#if condensedView?? && (condensedView.numSuperMatchBlobs > 0)>
        <div class="prot_sum">
            <div class="top-row">
                <div class="top-row-id">
                    <h1>Domains and repeats:</h1>
                </div>
                <div class="top-row-opt"><a href="#" title="Open domains and repeats view in a new window"><span
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
        </div>
        <#else>
        <div class="prot_sum" style="overflow: auto;">
            <div style="float: left;"><h1>Domains and repeats:</h1></div>
            <span style="margin: 6px 0 3px 6px; color:#838383;float:left; font-size:120%;">None predicted.</span>
        </div>
        </#if>

    <#if (protein.structuralFeatures?has_content || protein.structuralPredictions?has_content)>

        <#global locationId=0>


        <#if ((protein.structuralFeatures?size>0) || (protein.structuralPredictions?size>0))>
            <div class="prot_entries">
                <h1>Structural features and predictions</h1>
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
                                                <@structuralLocationMacro.structuralLocation smid=locationId protein=protein location=key structuralMatchData=feature.getSimpleStructuralMatchData(key) databaseMetadata=feature.dataSource/>
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
            <!-- No structural matches so the detailed matches section is omitted. -->
    </#if>
    <#else>
        <b>No structural match data found for this protein.</b>
</#if>
