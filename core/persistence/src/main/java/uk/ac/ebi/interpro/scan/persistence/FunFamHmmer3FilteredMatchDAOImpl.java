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
            Map<UUID, FunFamHmmer3Match> splitGroups = new HashMap<>();

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
                FunFamHmmer3Match.FunFamHmmer3Location funFamHmmer3Location = new FunFamHmmer3Match.FunFamHmmer3Location(
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
                        m.getAlignment(),
                        m.getResolvedLocationStart(),
                        m.getResolvedLocationEnd()
                );
                Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment hmmer3LocationFragment = new Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment(
                        m.getResolvedLocationStart(),
                        m.getResolvedLocationEnd(),
                        DCStatus.parseSymbol(m.getLocFragmentDCStatus())
                );

                FunFamHmmer3Match match;

                if (groupId != null) {
                    // This is a discontinuous location

                    match = splitGroups.get(groupId);
                    if (match != null) {
                        Set<FunFamHmmer3Match.FunFamHmmer3Location> locations = match.getLocations();
                        FunFamHmmer3Match.FunFamHmmer3Location location = locations.iterator().next();
                        for (Object objFragment: location.getLocationFragments()){
                            LocationFragment cmprLocationFragment = (LocationFragment) objFragment;
                            hmmer3LocationFragment.updateDCStatus(cmprLocationFragment);
                            cmprLocationFragment.updateDCStatus(hmmer3LocationFragment);
                        }
                        location.addLocationFragment(hmmer3LocationFragment);

                    } else {
                        match = new FunFamHmmer3Match(signature, m.getModelId(), m.getScore(), m.getEvalue(), null);
                        match.addLocation(funFamHmmer3Location);
                        splitGroups.put(groupId, match);
                    }
                } else {
                    // This is a normal continuous location
                    groupId = UUID.randomUUID();
                    match = new FunFamHmmer3Match(signature, m.getModelId(), m.getScore(), m.getEvalue(), null);
                    match.addLocation(funFamHmmer3Location);
                    splitGroups.put(groupId, match);
                }

                this.updateMatch(match);
            }

            if (splitGroups.size() > 0) {
                final String dbKey = Long.toString(protein.getId()) + signatureLibrary.getName();
                Set<FunFamHmmer3Match> matches = new HashSet<>(splitGroups.values());
                this.matchDAO.persist(dbKey, matches);
            }
        }
    }
}