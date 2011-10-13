<%@ taglib prefix="h"  tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--Returns protein features for inclusion in DBML--%>

<%--TODO: Easier if SimpleProtein returns separate collections for each section, even if based on two underlying collections?--%>

<a name="domains-sites"></a>
<h3>Domains and sites</h3>
<div id="section-domains-sites">
<c:forEach var="entry" items="${protein.entries}">
    <c:if test="${not empty entry.type}">
    <div>
        <p><a href="IEntry?ac=${entry.ac}">${entry.name}</a> (${entry.ac})</p>
        <div class="match">
            <c:forEach var="location" items="${entry.locations}">
                <%--TODO: Get background-color for match--%>
                <h:match proteinLength="${protein.length}"
                         start="${location.start}"
                         end="${location.end}"
                         colour="#ff9999"/>
            </c:forEach>
        </div>
        <div id="${entry.ac}-signatures" class="entry-signatures">
        <c:forEach var="signature" items="${entry.signatures}">
            <div>
                <p>
                    <%--Taken from user manual appendices http://www.ebi.ac.uk/interpro/user_manual.html:--%>
                    <c:set var="db" value="${signature.database}"/>
                    <c:set var="desc">
                        <c:choose>
                            <c:when test="${db == 'HAMAP'}">
                                Members of HAMAP families are identified using PROSITE profile collections. HAMAP profiles are manually created by expert curators and they identify proteins that are part of well-conserved bacterial, archaeal and plastid-encoded proteins families or subfamilies. The aim of HAMAP is to propagate manually generated annotation to all members of a given protein family in an automated and controlled way using very strict criteria.
                            </c:when>
                            <c:when test="${db == 'Pfam'}">
                                Pfam is a collection of protein family alignments which were constructed semi-automatically using hidden Markov models (HMMs). Sequences that were not covered by Pfam were clustered and aligned automatically, and are released as Pfam-B. Pfam families have permanent accession numbers and contain functional annotation and cross-references to other databases, while Pfam-B families are re-generated at each release and are unannotated.
                            </c:when>
                            <c:when test="${db == 'PRINTS'}">
                                PRINTS is a compendium of protein fingerprints. A fingerprint is a group of conserved motifs used to characterise a protein family; its diagnostic power is refined by iterative scanning of OWL. Usually the motifs do not overlap, but are separated along a sequence, though they may be contiguous in 3D-space. Fingerprints can encode protein folds and functionalities more flexibly and powerfully than can single motifs: the database thus provides a useful adjunct to PROSITE.
                            </c:when>
                            <c:when test="${db == 'PROSITE patterns' or db == 'PROSITE profiles'}">
                                PROSITE consists of documentation entries describing protein domains, families and functional sites, as well as associated patterns and profiles to identify them. Profiles and patterns are constructed from manually edited seed alignments. PROSITE is complemented by ProRule, a collection of rules based on profiles and patterns, which increases the discriminatory power of profiles and patterns by providing additional information about functionally and/or structurally critical amino acids.   
                            </c:when>
                            <c:otherwise>
                                <%--TODO: write DB descriptions --%>
                            </c:otherwise>
                        </c:choose>
                    </c:set>
                    <abbr title="${desc}">${db}</abbr>:
                    <a href="ISignature?ac=${signature.ac}">${signature.name}</a> (${signature.ac})
                </p>
                <div class="match">
                    <c:forEach var="location" items="${signature.locations}">
                        <%--TODO: Get background-color for match--%>
                        <h:match proteinLength="${protein.length}"
                                 start="${location.start}"
                                 end="${location.end}"
                                 colour="#aa9999"/>
                    </c:forEach>
                </div>
                <%--Not sure why we need this break, but next entry gets messed up without it --%>
                <br/>
            </div>
        </c:forEach>
        </div>
        <%--Not sure why we need this break, but next entry gets messed up without it --%>
        <br/>
    </div>
    </c:if>
</c:forEach>
</div>

<a name="unintegrated-signatures"></a>
<h3>Unintegrated signatures</h3>
<c:forEach var="entry" items="${protein.entries}">
    <c:if test="${empty entry.type}">
    <div>
        <c:forEach var="signature" items="${entry.signatures}">
        <div>
            <p><a href="ISignature?ac=${signature.ac}">${signature.name}</a> (${signature.ac})</p>
            <div class="match">
                <c:forEach var="location" items="${signature.locations}">
                    <%--TODO: Get background-color for match--%>
                    <h:match proteinLength="${protein.length}"
                             start="${location.start}"
                             end="${location.end}"
                             colour="#ff9999"/>
                </c:forEach>
            </div>
            <%--Not sure why we need this break, but next entry gets messed up without it --%>
            <br/>
        <div>
        </c:forEach>
    </div>
    </c:if>
</c:forEach>

<a name="structural-features"></a>
<h3>Structural features</h3>
<c:forEach var="match" items="${protein.structuralMatches}">
    <c:if test="${match.databaseName == 'CATH' or match.databaseName == 'SCOP' or match.databaseName == 'PDB'}">
        <div>
            <p>
                <c:choose>
                    <c:when test="${match.databaseName == 'CATH'}">
                        <a href="http://www.cathdb.info/cathnode/${match.classId}">${match.classId}</a>
                    </c:when>
                    <c:when test="${match.databaseName == 'SCOP'}">
                        <a href="http://scop.mrc-lmb.cam.ac.uk/scop/search.cgi?key=${match.classId}">${match.classId}</a>
                    </c:when>
                    <c:otherwise>
                        <a href="http://www.ebi.ac.uk/pdbe-srv/view/entry/${match.domainId}/summary">${match.domainId}</a>
                    </c:otherwise>
                </c:choose>
                (${match.databaseName})
            </p>                
            <div class="match">
                <c:forEach var="location" items="${match.locations}">
                    <%--TODO: Get background-color for match--%>
                    <h:match proteinLength="${protein.length}"
                             start="${location.start}"
                             end="${location.end}"
                             colour="#ff9999"/>
                </c:forEach>
            </div>
            <%--Not sure why we need this break, but next entry gets messed up without it --%>
            <br/>
        </div>
    </c:if>
</c:forEach>

<a name="structural-predictions"></a>
<h3>Structural predictions</h3>
<c:forEach var="match" items="${protein.structuralMatches}">
    <c:if test="${match.databaseName == 'MODBASE' or match.databaseName == 'SWISS-MODEL'}">
        <div>
            <p>
                <c:choose>
                    <c:when test="${match.databaseName == 'MODBASE'}">
                        <a href="http://modbase.compbio.ucsf.edu/modbase-cgi-new/model_search.cgi?searchvalue=${protein.ac}&searchproperties=database_id&displaymode=moddetail&searchmode=default">${match.classId}</a>
                    </c:when>
                    <c:otherwise>
                        <a href="http://swissmodel.expasy.org/repository/?pid=smr03&query_1_input=${protein.ac}">${match.domainId}</a>
                    </c:otherwise>
                </c:choose>
                (${match.databaseName})
            </p>
            <div class="match">
                <c:forEach var="location" items="${match.locations}">
                    <%--TODO: Get background-color for match--%>
                    <h:match proteinLength="${protein.length}"
                             start="${location.start}"
                             end="${location.end}"
                             colour="#ff9999"/>
                </c:forEach>
            </div>
            <%--Not sure why we need this break, but next entry gets messed up without it --%>
            <br/>
        </div>
    </c:if>
</c:forEach>

<%--Not sure why we need this break, but table gets right-aligned without it...--%>
<div><br/></div>

<%--TODO: Could use HTML5 Canvas to highlight matches in graphic and table when hover over--%>
<table class="match">
    <tr>
        <th>Entry</th>
        <th>Signature</th>
        <th>Start</th>
        <th>End</th>
    </tr>
    <c:forEach var="entry" items="${protein.entries}">
        <c:forEach var="location" items="${entry.locations}">
            <tr class="entry">
                <td><a href="IEntry?ac=${entry.ac}">${entry.name}</a> (${entry.ac})</td>
                <td></td>
                <td align="right">${location.start}</td>
                <td align="right">${location.end}</td>
            </tr>
            <c:forEach var="signature" items="${entry.signatures}">
                <c:forEach var="location" items="${signature.locations}">
                    <tr>
                        <td></td>
                        <td><a href="ISignature?ac=${signature.ac}">${signature.name}</a> (${signature.ac})</td>
                        <td align="right">${location.start}</td>
                        <td align="right">${location.end}</td>
                    </tr>
                </c:forEach>
            </c:forEach>
        </c:forEach>
    </c:forEach>
</table>