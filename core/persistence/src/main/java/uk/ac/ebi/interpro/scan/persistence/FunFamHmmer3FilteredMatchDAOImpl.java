package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.helper.SignatureModelHolder;
import uk.ac.ebi.interpro.scan.model.raw.FunFamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.*;

public class FunFamHmmer3FilteredMatchDAOImpl extends Hmmer3FilteredMatchDAO<FunFamHmmer3RawMatch> {
    @Override
    public void persist(Collection<RawProtein<FunFamHmmer3RawMatch>> filteredProteins, Map<String, SignatureModelHolder> modelToSignature, Map<String, Protein> proteinIdToProteinMap) {
        for (RawProtein<FunFamHmmer3RawMatch> rawProtein : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }

            SignatureLibrary signatureLibrary = null;
            String signatureLibraryRelease = null;

            Collection<FunFamHmmer3RawMatch> rawMatches = rawProtein.getMatches();
            Map<UUID, Hmmer3Match> splitGroups = new HashMap<>();

            for (FunFamHmmer3RawMatch m : rawMatches) {
                if (! isLocationWithinRange(protein, m)){
                    throw new IllegalStateException("Attempting to persist a match location outside sequence range " +
                            m.toString() + "\n" + protein.toString());
                } else if (signatureLibrary == null) {
                    signatureLibrary = m.getSignatureLibrary();
                    signatureLibraryRelease = m.getSignatureLibraryRelease();
                } else if (!signatureLibrary.equals(m.getSignatureLibrary()) ||
                        !signatureLibraryRelease.equals(m.getSignatureLibraryRelease())) {
                    throw new IllegalArgumentException("Filtered matches are from different signature library versions " +
                            "(more than one library version found)");
                }

                SignatureModelHolder holder = modelToSignature.get(m.getModelId());
                Signature signature = holder.getSignature();
                Model model = holder.getModel();
                if (signature == null) {
                    throw new IllegalStateException("Cannot find FunFam model " + m.getModelId());
                }

                UUID groupId = m.getSplitGroup();
                Hmmer3Match.Hmmer3Location hmmer3Location = new Hmmer3Match.Hmmer3Location(
                        m.getLocationStart(),
                        m.getLocationEnd(),
                        m.getLocationScore(),
                        m.getDomainIeValue(),
                        m.getHmmStart(),
                        m.getHmmEnd(),
                        model.getLength(),
                        HmmBounds.parseSymbol(m.getHmmBounds()),
                        m.getEnvelopeStart(),
                        m.getEnvelopeEnd(),
                        true,
                        DCStatus.parseSymbol(m.getLocFragmentDCStatus()),
                        m.getAlignment()
                );
                Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment hmmer3LocationFragment = new Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment(
                        m.getLocationStart(),
                        m.getLocationEnd(),
                        DCStatus.parseSymbol(m.getLocFragmentDCStatus())
                );

                Hmmer3Match match;

                if (groupId != null) {
                    // This is a discontinuous location

                    match = splitGroups.get(groupId);
                    if (match != null) {
                        Set<Hmmer3Match.Hmmer3Location> locations = match.getLocations();
                        Hmmer3Match.Hmmer3Location location = locations.iterator().next();
                        for (Object objFragment: location.getLocationFragments()){
                            LocationFragment cmprLocationFragment = (LocationFragment) objFragment;
                            hmmer3LocationFragment.updateDCStatus(cmprLocationFragment);
                            cmprLocationFragment.updateDCStatus(hmmer3LocationFragment);
                        }
                        location.addLocationFragment(hmmer3LocationFragment);

                    } else {
                        match = new Hmmer3Match(signature, m.getModelId(), m.getScore(), m.getEvalue(), null);
                        match.addLocation(hmmer3Location);
                        splitGroups.put(groupId, match);
                    }
                } else {
                    // This is a normal continuous location
                    groupId = UUID.randomUUID();
                    match = new Hmmer3Match(signature, m.getModelId(), m.getScore(), m.getEvalue(), null);
                    match.addLocation(hmmer3Location);
                    splitGroups.put(groupId, match);
                }

                this.updateMatch(match);
            }

            if (splitGroups.size() > 0) {
                final String dbKey = Long.toString(protein.getId()) + signatureLibrary.getName();
                Set<Hmmer3Match> matches = new HashSet<>(splitGroups.values());
                this.matchDAO.persist(dbKey, matches);
            }
        }
    }
}