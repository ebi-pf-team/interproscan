<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--Returns main body of protein page for inclusion in DBML--%>

<div class="tab">
    <div class="Protein_tab">Protein</div>
</div>

<div class="main-box">

    <h1>${protein.name} (${protein.ac})</h1>

    <div class="shortname">
        <small> Short name:</small>
        ${protein.id}
    </div>

    <div class="intro_prot">
        <table class="invisible_plus">
            <tbody>
            <tr>
                <td width="70"><strong>Accession</strong></td>
                <td>
                    <a class="ext" href="http://www.uniprot.org/uniprot/${protein.ac}">${protein.ac}</a>
                    (<img alt="Reviewed protein (UniProtKB/Swiss-Prot)"
                          src="http://wwwdev.ebi.ac.uk/interpro/images/ico_protein_S.png"
                          title="Reviewed protein (UniProtKB/Swiss-Prot)"> UniProtKB/Swiss-Prot)
                </td>
            </tr>
            <tr>
                <td><strong>Species</strong></td>
                <td>${protein.taxFullName}</td>
            </tr>
            <tr>
                <td><strong>Length</strong></td>
                <td>${protein.length} amino acids (complete)</td>
            </tr>
            </tbody>
        </table>
    </div>

    <c:import url="protein-features.jsp"/>

</div>
