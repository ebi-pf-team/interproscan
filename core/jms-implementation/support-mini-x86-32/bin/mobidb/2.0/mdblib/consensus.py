"""Contains classes used to build different type of consensus."""

import copy
import logging

# relative imports
from mdblib.prediction import Prediction
from mdblib.states import States

pappu_codes = {'PA': '1',
               'PPE': '2',
               'NPE': '3'}

feature_codes = {'C': '4',
                 'P': '5',
                 'STNQ': '6'}

feature_desc = {'1': 'Polyampholyte',
                '2': 'Positive Polyelectrolyte',
                '3': 'Negative Polyelectrolyte',
                '4': 'Cystein-rich',
                '5': 'Proline-rich',
                '6': 'Polar'}

class Consensus(object):
    """
    General consensus class. Can calculate the raw agreement of predictors.
    """
    tag = 'mdb'

    def __init__(self, _prediction_stack):
        self.predictions_stack = _prediction_stack
        self.prediction = None

    def calc_agreement(self, seq, threshold, ptype=None, force_consensus=False):
        """
        Compute agreement from a stack of predictions

        :param seq: amino acid sequence
        :type seq: str
        :param threshold: agreement threshold
        :type threshold: float
        :param ptype: prediction type to consider for agreement
        :type ptype: str
        :param force_consensus: if True consensus computation is computed despite
            single predictors errors
        :type force_consensus: bool
        """
        agreement = [0.0] * len(seq)
        included_predictors = 0

        for prediction in self.predictions_stack:

            if ptype is not None and ptype in prediction.types:
                logging.debug('%s | agreement: included', prediction.method)

                if prediction.has_correct_length(seq, force_consensus):
                    logging.debug('%s | length: OK (%i)', prediction.method, len(seq))

                    included_predictors += 1
                    agreement = map(sum, zip(agreement, prediction.states))
            else:
                logging.debug('%s | agreement: excluded', prediction.method)

        agreement = [summed_states / included_predictors for summed_states in agreement]

        self.prediction = Prediction(self.tag, agreement, threshold)


class SimpleConsensus(Consensus):
    """
    Define a simple consensus based on an agreement threshold (default 0.5).
    """
    def __init__(self, _prediction_stack, _seq, threshold=0.5, force=False):
        logging.debug('Generating Simple consensus')
        super(SimpleConsensus, self).__init__(_prediction_stack)
        self.calc_agreement(_seq, threshold, ptype='disorder', force_consensus=force)
        self.prediction.translate_states({1: 'D', 0: 'S'})
        self.prediction.regions = self.prediction.to_regions(start_index=1, positivetag='D')


class MobidbLiteConsensus(Consensus):
    """
    Define consensus featured by MobiDB-Lite as its prediction.
    """
    def __init__(self, _prediction_stack, _seq,
                 threshold=0.625, _lencutoff=20, pappu=False, force=False):
        logging.debug('Generating MobiDB Lite consensus')
        super(MobidbLiteConsensus, self).__init__(_prediction_stack)
        self.lencutoff = _lencutoff
        self.seq = _seq
        self.calc_agreement(_seq, threshold, ptype='mobidblite', force_consensus=force)
        self.prediction.translate_states({1: 'D', 0: 'S'})
        self.prediction.math_morphology()
        self.prediction.merge_close_longidrs()
        self.prediction.regions = self.prediction.to_regions(start_index=1, positivetag='D',
                                                             len_thr=self.lencutoff)
        if self.prediction.regions:
            self.enriched_regions = self.get_region_features()
            if pappu is True:
                self.set_pappu_classes_per_region()

    def set_pappu_classes_per_region(self, reg_startindex=1):
        """
        Transform the status of regions appending the Pappu class. (setter)

        :param reg_startindex: start index of regions
        """
        for region in self.prediction.regions:
            start, end, status = region
            region_sequence = self.seq[start - reg_startindex: end - reg_startindex + 1]
            pappu_class = self.prediction.get_disorder_class(region_sequence)
            region[-1] = '{}_{}'.format(status, pappu_class)

    def get_region_features(self):
        """
        Look for sequence features within prediction.regions

        :return: prediction.regions extended with feature regions
        """
        features_raw = ['0'] * len(self.seq)
        features_final = ['0'] * len(self.seq)
        enriched_regions = []

        seq = States(self.seq)
        for i, token in enumerate(seq.tokenize(n=7)):
            token = States(token)

            pappu_class = token.get_disorder_class(token.states)

            # assign features hierarchically
            if pappu_class != 'WC':
                features_raw[i] = pappu_codes[pappu_class]

            elif token.is_enriched(['C']) is True:
                features_raw[i] = '4'

            elif token.is_enriched(['P']) is True:
                features_raw[i] = '5'

            elif token.is_enriched(['S', 'T', 'N', 'Q']) is True:
                features_raw[i] = '6'

        # merge features hierarchically
        for feature_code in range(len(feature_desc), 0, -1):
            feature_code = str(feature_code)

            # apply math morph to single features
            f = States(features_raw)
            f.make_binary(active=feature_code)
            f.math_morphology(rmax=5, tags=(feature_code, '0'))

            # apply feature to seq positions
            for i, e in enumerate(f.states):
                if e == feature_code:
                    features_final[i] = feature_code

        for region in self.prediction.regions:
            start, end, _ = region
            enriched_regions.append(region)

            for feat_reg in States(features_final[start - 1: end]).to_regions(len_thr=15):
                if feat_reg[-1] != '0':
                    reg = [feat_reg[0] + start, feat_reg[1] + start, feature_desc[feat_reg[2]]]
                    enriched_regions.append(reg)

        return enriched_regions
