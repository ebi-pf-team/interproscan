<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:forEach var="match" items="${matches}"><c:forEach var="loc" items="${match.locations}"><c:out
        value="${match.proteinMD5}"/>,<c:out value="${match.signatureLibraryName}"/>,<c:out
        value="${match.signatureLibraryRelease}"/>,<c:out value="${match.signatureAccession}"/>,<c:out
        value="${match.sequenceScore}"/>,<c:out value="${match.sequenceEValue}"/>,<c:out
        value="${match.graphScan}"/>,<c:out value="${loc.start}"/>,<c:out value="${loc.end}"/>,<c:out
        value="${loc.score}"/>,<c:out value="${loc.eValue}"/>,<c:out value="${loc.pValue}"/>,<c:out
        value="${loc.motifNumber}"/>,<c:out value="${loc.hmmStart}"/>,<c:out value="${loc.hmmEnd}"/>,<c:out
        value="${loc.hmmBounds}"/>,<c:out value="${loc.envelopeStart}"/>,<c:out value="${loc.envelopeEnd}"/>,<c:out
        value="${loc.level}"/>,<c:out value="${loc.cigarAlignment}"/>
</c:forEach></c:forEach>

<c:out value="${xmlmatches}"/>
