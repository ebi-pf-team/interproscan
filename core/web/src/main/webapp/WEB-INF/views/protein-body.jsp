<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--Returns main body of protein page for inclusion in DBML--%>

<div class="tab">
    <div class="Protein_tab">Protein</div>
</div>

<div class="main-box">

<%--Cannot use the following:--%>
<%--<h1>${protein.crossReferences[0].identifier}</h1>--%>
<%--Following is ugly:--%>
<c:forEach var="xref" items="${protein.crossReferences}" end="0">

    <h1>TODO: Add description to xref (TODO: Add accession to xref class)</h1>

    <div class="shortname">
        <small> Short name:</small> ${xref.name}
    </div>

    <div class="intro_prot">
        <table class="invisible_plus">
            <tbody>
                <tr>
                    <td width="70"><strong>Accession</strong></td>
                    <td>
                        <a class="ext" href="http://www.uniprot.org/uniprot/P38398">TODO: Add accession to xref class</a>
                        (<img alt="Reviewed protein (UniProtKB/Swiss-Prot)" src="http://wwwdev.ebi.ac.uk/interpro/images/ico_protein_S.png"
                              title="Reviewed protein (UniProtKB/Swiss-Prot)"> UniProtKB/Swiss-Prot)
                    </td>
                </tr>
                <tr>
                    <td><strong>Species</strong></td>
                    <td>TODO: get species</td>
                </tr>
                <tr>
                    <td><strong>Length</strong></td>
                    <td>${fn:length(protein.sequence)} amino acids (complete)</td>
                </tr>
            </tbody>
        </table>
    </div>
</c:forEach>

    <%--TODO: Include protein-features.jsp rather than repeat here--%>

    <h2>Matches</h2>
    <table>
        <tr>
            <th>Signature</th>
            <th>Start</th>
            <th>End</th>
        </tr>
        <c:forEach var="match" items="${protein.matches}">
            <%--TODO: Sort on location.start, either in model code by implementing Comparable [1], or by returning a holder--%>
            <%--TODO: from the Controller with the separate objects we need for each section? --%>
            <%--[1] http://forums.devshed.com/java-help-9/sorting-with-jstl-340581.html--%>
            <c:forEach var="location" items="${match.locations}">
                <tr>
                    <td>${match.signature.name} (${match.signature.accession})</td>
                    <td>${location.start}</td>
                    <td>${location.end}</td>
                </tr>
            </c:forEach>
        </c:forEach>
    </table>

</div>    