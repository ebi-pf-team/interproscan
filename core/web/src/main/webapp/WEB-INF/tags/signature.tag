<%@ taglib prefix="h"  tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%--<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="protein"   required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleProtein" %>
<%@ attribute name="signature" required="true" type="uk.ac.ebi.interpro.scan.web.model.SimpleSignature" %>

<div>
    <p>
        <%--Taken from user manual appendices http://www.ebi.ac.uk/interpro/user_manual.html:--%>
        <c:set var="db" value="${signature.database}"/>
        <c:set var="desc">
            <c:choose>
                <c:when test="${db == 'HAMAP'}">
                    Members of HAMAP families are identified using PROSITE profile collections. HAMAP profiles are manually created by expert curators and they identify proteins that are part of well-conserved bacterial, archaeal and plastid-encoded proteins families or subfamilies. The aim of HAMAP is to propagate manually generated annotation to all members of a given protein family in an automated and controlled way using very strict criteria.
                </c:when>
                <c:when test="${db == 'Pfam' or db == 'PfamB'}">
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
        <a href="ISignature?ac=${signature.ac}">${signature.name}</a> (<abbr title="${desc}">${db}</abbr>: ${signature.ac})
    </p>
    <div class="match">
        <c:forEach var="location" items="${signature.locations}">
            <%--TODO: Get background-color for match--%>
            <c:set var="dbClass" value="${fn:replace(fn:toLowerCase(db), ' ', '-')}"/>
            <%--fn:toLowerCase(--%>
            <h:location protein="${protein}"
                        location="${location}"
                        colourClass="c-signature-${dbClass} c-signature"/>
        </c:forEach>
    </div>
    <%--Not sure why we need this break, but next entry gets messed up without it --%>
    <br/>
</div>
