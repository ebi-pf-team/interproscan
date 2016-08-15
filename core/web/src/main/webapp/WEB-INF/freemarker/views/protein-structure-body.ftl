<#--Returns main body of protein page for inclusion in DBML-->
<#import "../macros/condensedView.ftl" as condensedViewMacro/>

<#if protein?? && protein.structuralDatabases?has_content>

<#--Returns protein structure features for inclusion in DBML-->
    <#import "../macros/structuralLocation.ftl" as structuralLocationMacro/>
    <#assign proteinLength = protein.length>

<h3>Domains and repeats</h3>

    <@condensedViewMacro.condensedView condensedView=condensedView scale=scale entryColours=entryColours/>

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

                                        <#--
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
                                                        <@structuralLocationMacro.structuralLocation structMatchId=locationId proteinAc=protein.ac proteinLength=proteinLength location=location structuralMatchData=feature.getSimpleStructuralMatchData(key) databaseMetadata=feature.dataSource />
                                                    </#list>

                                                <#--Draw in scale markers for this line-->
                                                    <#list scale?split(",") as scaleMarker>
                                                        <span class="grade" style="left:${((scaleMarker?number?int / proteinLength) * 100)?c}%;" title="${scaleMarker}"></span>
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
                                        <#--
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
                                                        <@structuralLocationMacro.structuralLocation structMatchId=locationId proteinAc=protein.ac proteinLength=proteinLength location=key structuralMatchData=feature.getSimpleStructuralMatchData(key) databaseMetadata=feature.dataSource/>
                                                    </#list>

                                                <#--Draw in scale markers for this line-->
                                                    <#list scale?split(",") as scaleMarker>
                                                        <span class="grade" style="left:${((scaleMarker?number?int / proteinLength) * 100)?c}%;" title="${scaleMarker}"></span>
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
    <#--Nothing to display!-->
    <#-- No structural matches so the detailed matches section is omitted. -->
    </#if>
<#else>
    <b>No structural match data found for this protein.</b>
</#if>
