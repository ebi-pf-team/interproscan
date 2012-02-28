<%@ taglib prefix="h"  tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--Returns protein structure features for inclusion in DBML--%>

<c:choose>
    <c:when test="${empty protein.structuralFeatures && empty protein.structuralPredictions}">
        <%--Nothing to display!--%>
        <p>No structural data found for this protein.</p>
    </c:when>
    <c:otherwise>

     <div class="prot_sum">
            <%--<h1>Sequence summary</h1>--%>

                <div class="top-row">
                    <div class="top-row-id"><h1>Summary view</h1></div>
                    <div class="top-row-opt"><a href="#" title="Open sequence summary view in a new window"><span class="opt1"></span></a></div>
                </div>

                <div class="bot-row">
                    <div class="bot-row-line-top"></div>
                    <ol class="signatures">

                    <c:import url="condensed-view.jsp"/>

                    </ol>
                    <div class="bot-row-line-bot"></div>
                </div>

        <div class="prot_scale">
          <div class="bot-row">

                  <div class="bot-row-line">
                        <div style="position:relative;">
                        <!-- Position marker lines -->
                        <c:forTokens items="${scale}" delims="," var="scaleMarker">
                       <!-- to build an exception for 0 -->
                       <span class="scale_bar" style="left:${(scaleMarker / protein.length) * 100}%;" title="${scaleMarker}"></span>
                       <span class="scale_numb" style="left:${(scaleMarker / protein.length) * 100}%;">${scaleMarker}</span>
                       </c:forTokens>
                       </div>
             </div>

          </div>
     </div>
    </div>

        <c:set var="locationId" value="0" scope="request"/>

        <c:if test="${not empty protein.structuralFeatures}">

    <div class="prot_entries">
         <h1>Sequence features</h1>
         <ol class="entries">
         <li class="entry">
            <div class="top-row">
                 <div class="top-row-id"> no IPR </div>
                 <div class="top-row-name">Structural features</div>
            </div>
            <div class="bot-row">
            <div class="bot-row-line-top"></div>
            <ol class="signatures">
                    <c:forEach var="database" items="${protein.structuralFeatures}">
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
                            <div class="bot-row-signame">${database.dataSource.name}</div>
                            <div class="bot-row-line">
                            <div class="matches">

                                <c:forEach var="structuralMatch" items="${database.structuralMatches}">
                                    <%-- location = ${structuralMatch.key} --%>
                                    <%-- structuralMatchData = ${structuralMatch.value} --%>
                                    <c:set var="locationId" value="${locationId + 1}" />

                                    <h:structuralLocation id="${locationId}"
                                                          protein="${protein}"
                                                          location="${structuralMatch.key}"
                                                          structuralMatchData="${structuralMatch.value}"
                                                          databaseMetadata="${database.dataSource}"/>
                                </c:forEach>


                            </div></div>
                        </li>
                    </c:forEach>
                </ol>
                <div class="bot-row-line-bot"></div>
                </div>
             </li></ol>
    </div>
        </c:if>

        <c:if test="${not empty protein.structuralPredictions}">
        <div class="prot_entries" id="uni">
         <ol class="entries">
         <li class="entry">
        <div class="top-row">
        <div class="top-row-id"> no IPR </div>
        <div class="top-row-name">Structural predictions</div>
        </div>
        <div class="bot-row">
        <div class="bot-row-line-top"></div>
        <ol class="signatures">

                <c:forEach var="database" items="${protein.structuralPredictions}">
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

                        <div class="bot-row-signame">${database.dataSource.name}</div>
                        <div class="bot-row-line">
                        <div class="matches">

                            <c:forEach var="structuralMatch" items="${database.structuralMatches}">
                                <%-- location = ${structuralMatch.key} --%>
                                <%-- structuralMatchData = ${structuralMatch.value} --%>
                                <c:set var="locationId" value="${locationId + 1}" />
                                <h:structuralLocation id="${locationId}"
                                                      protein="${protein}"
                                                      location="${structuralMatch.key}"
                                                      structuralMatchData="${structuralMatch.value}"
                                                      databaseMetadata="${database.dataSource}"/>
                            </c:forEach>


                    </li>
                </c:forEach>
            </ol>
            <div class="bot-row-line-bot"></div>
        </li></ol>
        </div>
        </c:if>

    </c:otherwise>
</c:choose>
