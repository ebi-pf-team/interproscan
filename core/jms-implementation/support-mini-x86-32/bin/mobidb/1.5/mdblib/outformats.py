import re
import json
import copy
from functools import reduce


class Formatter(object):
    def __init__(self, _acc, _multi_accessions=None):
        self.acc = _acc.split()[0]
        self.multi_accessions = _multi_accessions
        self.output = self._get_output_obj()

    def multiply_by_accession(self, key):
        print(self.multi_accessions)
        for i, acc in enumerate(self.multi_accessions):
            if i == 0:
                self.output[0][key] = acc
            else:
                copy_out = copy.copy(self.output[0])
                copy_out[key] = acc
                self.output.append(copy_out)

    def _get_output_obj(self):
        # handle
        pass


class InterProFormat(Formatter):
    def __init__(self, _acc, _mdbl_consensus, _features=False):
        self.features = _features
        self.mdbl_consensus = _mdbl_consensus
        super(InterProFormat, self).__init__(_acc)

    def _get_output_obj(self):
        if self.mdbl_consensus.prediction.regions:
            if self.features is False:
                out_obj = [[self.acc] + r for r in self.mdbl_consensus.enriched_regions]
            else:
                out_obj = [[self.acc] + r for r in self.mdbl_consensus.prediction.regions]

            return [out_obj]

    def __repr__(self):
        if self.output:
            if self.features is False:
                output = "\n".join(["{}\t{}\t{}{}".format(
                    ele[0], ele[1], ele[2], "\t{}".format(
                        ele[3]) if ele[3] != "D" else '') for ele in self.output[0]])
            else:
                output = "\n".join(["{}\t{}\t{}".format(
                        ele[0], ele[1], ele[2]) for ele in self.output[0]])
            return output
        else:
            return ""


class ExtendedFormat(Formatter):
    def __init__(self, _acc, _mdbl_consensus, **kwargs):
        self.mdbl_consensus = _mdbl_consensus
        super(ExtendedFormat, self).__init__(_acc, **kwargs)

        if self.multi_accessions:
            self.multiply_by_accession("accession")

    def _get_output_obj(self):
        if self.mdbl_consensus.prediction.regions:
            out_obj = {
                    "accession": self.acc,
                    "pred": "mobidblite",
                    "consensus": self.mdbl_consensus.prediction.states,
                    "regions": self.mdbl_consensus.prediction.regions}

            return [out_obj]

    def __repr__(self):
        if self.output:
            return '\n'.join(json.dumps(oobj) for oobj in self.output)
        else:
            return ""


class Mobidb3Format(Formatter):
    def __init__(self, _acc, _seqlen, _mdbl_consensus,
                 _simple_consensus, _single_predictions, **kwargs):
        self.seqlen = _seqlen
        self.mdbl_consensus = _mdbl_consensus
        self.simple_consensus = _simple_consensus
        self.single_predictions = _single_predictions
        super(Mobidb3Format, self).__init__(_acc, **kwargs)

        if self.multi_accessions:
            self.multiply_by_accession("accession")

    def _get_output_obj(self):
        out_obj = dict()

        # MobiDB-lite consensus
        if self.mdbl_consensus.prediction.regions:
            out_obj \
                .setdefault('mobidb_consensus', dict()) \
                .setdefault('disorder', dict()) \
                .setdefault('predictors', list()) \
                .append(
                {'method': 'mobidb_lite',
                 'regions': self.mdbl_consensus.prediction.regions,
                 'scores': self.mdbl_consensus.prediction.scores,
                 'dc': reduce(
                     lambda x, t:
                     x + (t[1] - t[0] + 1),
                     self.mdbl_consensus.prediction.regions, 0.0) / self.seqlen})

        # Simple consensus
        if self.simple_consensus.prediction.regions:
            out_obj.setdefault('mobidb_consensus', dict()) \
                .setdefault('disorder', dict()) \
                .setdefault('predictors', list()) \
                .append(
                {'method': 'simple',
                 'regions': self.simple_consensus.prediction.regions})

        # Single predictions
        for prediction in self.single_predictions:

            if ('disorder' or 'lowcomp') in prediction.types:
                prediction.translate_states({1: 'D', 0: 'S'})

                out_obj \
                    .setdefault('mobidb_data', dict()) \
                    .setdefault('disorder', dict()) \
                    .setdefault('predictors', list()) \
                    .append(
                    {'method': prediction.method,
                     'regions': prediction.to_regions(start_index=1, positivetag='D')})

            if 'sspops' in prediction.types:
                method, ptype = prediction.method.split('_')

                out_obj \
                    .setdefault('mobidb_data', dict()) \
                    .setdefault('ss_populations', dict()) \
                    .setdefault('predictors', list()) \
                    .append(
                    {'method': method,
                     'type': ptype,
                     'regions': prediction.scores})

            if 'bindsite' in prediction.types:
                prediction.translate_states({1: 'D', 0: 'S'})

                out_obj \
                    .setdefault('mobidb_data', dict()) \
                    .setdefault('lips', dict()) \
                    .setdefault('predictors', list()) \
                    .append(
                    {'method': prediction.method,
                     'regions': prediction.to_regions(start_index=1, positivetag='D')})

        if out_obj:
            out_obj["length"] = self.seqlen

            if re.search("^UPI[A-F0-9]{10}$", self.acc):
                out_obj['uniparc'] = self.acc

            else:
                out_obj['accession'] = self.acc

            return [out_obj]

    def __repr__(self):
        if self.output:
            return '\n'.join(json.dumps(oobj) for oobj in self.output)
        else:
            return ""


class FullIdPredsFormat(Formatter):
    def __init__(self, _acc, _mdbl_consensus, _single_predictions, **kwargs):
        self.mdbl_consensus = _mdbl_consensus
        self.single_predictions = _single_predictions
        super(FullIdPredsFormat, self).__init__(_acc, **kwargs)

        if self.multi_accessions:
            self.multiply_by_accession("accession")

    def _get_output_obj(self):
        out_obj = dict()

        if self.mdbl_consensus.prediction.regions:
            out_obj \
                .setdefault('predictions', list()) \
                .append(
                {'method': 'mobidb_lite',
                 'regions': self.mdbl_consensus.prediction.regions,
                 'scores': self.mdbl_consensus.prediction.scores})

        for prediction in self.single_predictions:
            if 'disorder' in prediction.types:
                prediction.translate_states({1: 'D', 0: 'S'})

                out_obj \
                    .setdefault('predictions', list()) \
                    .append(
                    {'method': prediction.method,
                     'regions': prediction.to_regions(start_index=1, positivetag='D'),
                     'scores': prediction.scores})

        if out_obj:
            out_obj['accession'] = self.acc
            return [out_obj]

    def __repr__(self):
        if self.output:
            return '\n'.join(json.dumps(oobj) for oobj in self.output)
        else:
            return ""
