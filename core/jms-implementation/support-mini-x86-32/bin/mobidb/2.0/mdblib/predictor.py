import os
import json
import shlex
import logging
import subprocess

from mdblib.prediction import Prediction


class Predictor(object):
    tag = None
    types = None
    groups = None
    intype = None
    shared_name = None
    suppress_stderr = False

    def __init__(self, _input_file, _bin_directory, _architecture):
        self.input_file = _input_file
        self.bin_directory = _bin_directory
        self.architecture = _architecture

        self.cwd = None
        self.command = None

    def run(self):
        try:
            devnull = open(os.devnull, 'w') if self.suppress_stderr is True else None
            output = subprocess.check_output(
                shlex.split(self.command), stderr=devnull, cwd=self.cwd)

            if output:
                return self.parse(output)
            else:
                logging.warning('%s | No output', self.shared_name)

        except subprocess.CalledProcessError as e:
            acc = '|'.join(self.input_file.split('-')[:-1]).split('/')[-1]

            logging.error('%s | %s crashed with error <%s>',
                         acc,
                         self.shared_name,
                         '; '.join(e.output.decode('utf-8').split('\n')))

        except OSError as e:
            acc = '|'.join(self.input_file.split('-')[:-1]).split('/')[-1]

            logging.error('%s | %s crashed with error <%s>',
                          acc,
                          self.shared_name,
                          '; '.join(e.strerror.split('\n')))

        except Exception as e:
            if isinstance(e, bytes):
                e = e.decode('utf-8')

            logging.error('Unhandled exception encountered: <%s>', e)

        return None

    def parse(self, *args):
        pass


class IUPredL(Predictor):
    tag = 'iupl'
    types = ['disorder', 'mobidblite']
    groups = ['main', 'mobidb3', 'allid']
    intype = 'flat'
    shared_name = 'iupred'

    def __init__(self, _input_file, _bin_directory, _architecture, _threshold):
        super(IUPredL, self).__init__(_input_file, _bin_directory, _architecture)
        self.threshold = _threshold[self.tag]
        self.input_seq = self.get_seq()

        self.command = '{}/bin{}/iupred_string {} long'.format(
            self.bin_directory, self.architecture, self.input_seq)

    def get_seq(self):
        seq = ""
        with open(self.input_file) as f:
            for line in f:
                seq += line.strip()

        return seq

    def parse(self, output):
        probs = list()
        for line in output.decode('utf-8').strip().split("\n"):
            if line[0] != "#":
                line = line.strip().split()
                probs.append(round(float(line[2]), 4))

        if probs:
            return [Prediction(self.tag, probs, self.threshold, self.types)]


class IUPredS(Predictor):
    tag = 'iups'
    types = ['disorder', 'mobidblite']
    groups = ['main', 'mobidb3', 'allid']
    intype = 'flat'
    shared_name = 'iupred'

    def __init__(self, _input_file, _bin_directory, _architecture, _threshold):
        super(IUPredS, self).__init__(_input_file, _bin_directory, _architecture)
        self.threshold = _threshold[self.tag]
        self.input_seq = self.get_seq()

        self.command = '{}/bin{}/iupred_string {} short'.format(
            self.bin_directory, self.architecture, self.input_seq)

    def get_seq(self):
        seq = ""
        with open(self.input_file) as f:
            for line in f:
                seq += line.strip()

        return seq

    def parse(self, output):
        probs = list()
        for r in output.decode('utf-8').strip().split("\n"):
            if r[0] != "#":
                r = r.strip().split()
                probs.append(round(float(r[2]), 4))

        if probs:
            return [Prediction(self.tag, probs, self.threshold, self.types)]


class ESpritzN(Predictor):
    tag = 'espN'
    types = ['disorder', 'mobidblite']
    groups = ['main', 'mobidb3', 'allid']
    intype = 'disbin'
    shared_name = 'espritz'

    def __init__(self, _input_file, _bin_directory, _architecture, _threshold):
        super(ESpritzN, self).__init__(_input_file, _bin_directory, _architecture)
        self.threshold = _threshold[self.tag]

        self.cwd = os.path.join(self.bin_directory, 'bin')
        self.command = '{0}/bin/bin{2}/disbinN {0}/bin/model_definition/ensembleN {1} /dev/null'\
            .format(self.bin_directory, self.input_file, self.architecture)

    def parse(self, output):
        probs = list()
        for residue in output.decode('utf-8').split("\n"):
            if not residue:
                continue
            if residue[0] in ["O", "D"]:
                probs.append(round(float(residue.split()[1]), 4))

        if probs:
            return [Prediction(self.tag, probs, self.threshold, self.types)]


class ESpritzD(Predictor):
    tag = 'espD'
    types = ['disorder', 'mobidblite']
    groups = ['main', 'mobidb3', 'allid']
    intype = 'disbin'
    shared_name = 'espritz'

    def __init__(self, _input_file, _bin_directory, _architecture, _threshold):
        super(ESpritzD, self).__init__(_input_file, _bin_directory, _architecture)
        self.threshold = _threshold[self.tag]

        self.cwd = os.path.join(self.bin_directory, 'bin')
        self.command = '{0}/bin/bin{2}/disbinD {0}/bin/model_definition/ensembleD {1} /dev/null'\
            .format(self.bin_directory, self.input_file, self.architecture)

    def parse(self, output):
        probs = list()
        for residue in output.decode('utf-8').split("\n"):
            if not residue:
                continue
            if residue[0] in ["O", "D"]:
                probs.append(round(float(residue.split()[1]), 4))

        if probs:
            return [Prediction(self.tag, probs, self.threshold, self.types)]


class ESpritzX(Predictor):
    tag = 'espX'
    types = ['disorder', 'mobidblite']
    groups = ['main', 'mobidb3', 'allid']
    intype = 'disbin'
    shared_name = 'espritz'

    def __init__(self, _input_file, _bin_directory, _architecture, _threshold):
        super(ESpritzX, self).__init__(_input_file, _bin_directory, _architecture)
        self.threshold = _threshold[self.tag]

        self.cwd = os.path.join(self.bin_directory, 'bin')
        self.command = '{0}/bin/bin{2}/disbinX {0}/bin/model_definition/ensembleX {1} /dev/null'\
            .format(self.bin_directory, self.input_file, self.architecture)

    def parse(self, output):
        probs = list()
        for residue in output.decode('utf-8').split("\n"):
            if len(residue) == 0:
                continue
            if residue[0] in ["O", "D"]:
                probs.append(round(float(residue.split()[1]), 4))

        if probs:
            return [Prediction(self.tag, probs, self.threshold, self.types)]


class GlobPlot(Predictor):
    tag = 'glo'
    types = ['disorder', 'mobidblite']
    groups = ['main', 'mobidb3', 'allid']
    intype = 'flat'
    shared_name = 'globplot'
    suppress_stderr = True

    def __init__(self, _input_file, _bin_directory, _architecture, _threshold):
        super(GlobPlot, self).__init__(_input_file, _bin_directory, _architecture)
        self.threshold = _threshold[self.tag]

        self.command = "python2 {0}/GlobPipe.py 10 15 74 4 5 {1} {0}/bin{2}".format(
            self.bin_directory, self.input_file, self.architecture
        )

    def parse(self, output):
        probs = json.loads(output.decode('utf-8').replace("\'", "\""))[0]['p']

        if probs:
            return [Prediction(self.tag, probs, self.threshold, self.types)]


class DisEMBL(Predictor):
    tag = ['dis465', 'disHL']
    types = ['disorder', 'mobidblite']
    groups = ['main', 'mobidb3', 'allid']
    intype = 'flat'
    shared_name = 'disembl'

    def __init__(self, _input_file, _bin_directory, _architecture, _thresholds):
        super(DisEMBL, self).__init__(_input_file, _bin_directory, _architecture)
        self.thresholds = {subtag: _thresholds[subtag] for subtag in self.tag}

        self.command = 'python2 {0}/DisEMBL.mobidb.py 8 8 4 1.2 1.4 1.2 {0}/bin{2} {1}'.format(
            self.bin_directory, self.input_file, self.architecture)

    def parse(self, output):
        probs = {ele['pred']: ele['p'] for ele in json.loads(
            output.decode('utf-8').rstrip("\n").replace("\'", "\""))}

        preds = list()
        for subtag in self.tag:
            if probs[subtag]:
                preds.append(Prediction(subtag, probs[subtag], self.thresholds[subtag], self.types))

        return preds


class VSL2b(Predictor):
    tag = 'vsl'
    types = ['disorder']
    groups = ['mobidb3', 'allid']
    intype = 'flat'
    shared_name = 'vsl2'
    suppress_stderr = True

    def __init__(self, _input_file, _bin_directory, _architecture, _threshold):
        super(VSL2b, self).__init__(_input_file, _bin_directory, _architecture)
        self.threshold = _threshold[self.tag]

        self.command = "java -XX:+UseSerialGC -jar {}/VSL2.jar -s:{}".format(
            self.bin_directory, self.input_file)

    def parse(self, output):
        """Parse VSL2b output and extract probabilities

        :param output: VSL2b output
        :types output: str
        :return: ID prediction probabilities::

            [0.9994, 0.9994, 0.9995, 0.9995, ...]

        :rtype: list
        """
        processlines = False
        probs = list()
        for row in output.decode('utf-8').split("\n"):
            if row == "----------------------------------------":
                processlines = True
            elif row == "========================================":
                processlines = False
            elif processlines and "-" not in row:
                probs.append(round(float(row.split()[2].replace(',', '.')), 4))

        if probs:  # may not get results if sequence has non-standard residues (X,etc)outfile
            return [Prediction(self.tag, probs, self.threshold, self.types)]


class JRonn(Predictor):
    tag = 'jronn'
    types = ['disorder']
    groups = ['mobidb3', 'allid']
    intype = 'fasta'
    shared_name = 'jronn'

    def __init__(self, _input_file, _bin_directory, _architecture, _threshold):
        super(JRonn, self).__init__(_input_file, _bin_directory, _architecture)
        self.threshold = _threshold[self.tag]

        self.command = "java -XX:+UseSerialGC -jar {}/jronn.jar -i={}".format(
            self.bin_directory, self.input_file)

    def parse(self, output):
        probs = list()
        for row in output.decode('utf-8').split("\n"):
            if len(row) > 0 and row[0] != ">":
                probs.append(round(float(row.split()[1]), 4))

        if probs:
            return [Prediction(self.tag, probs, self.threshold, self.types)]


class Seg(Predictor):
    tag = 'seg'
    types = ['lowcomp']
    groups = ['mobidb3']
    intype = 'fasta'
    shared_name = 'seg'

    def __init__(self, _input_file, _bin_directory, _architecture, _threshold):
        super(Seg, self).__init__(_input_file, _bin_directory, _architecture)
        self.threshold = _threshold[self.tag]

        self.command = '{}/bin{}/seg {} -x'.format(
            self.bin_directory, self.architecture, self.input_file)

    def parse(self, output):
        probs = list()
        for line in output.decode('utf-8').split("\n"):
            if len(line) > 0 and line[0] != '>':
                for pred in line:
                    if pred == 'x':
                        probs.append(1.0)
                    else:
                        probs.append(0.0)

        if probs:
            return [Prediction(self.tag, probs, self.threshold, self.types)]


class Pfilt(Predictor):
    tag = 'pfilt'
    types = ['lowcomp']
    groups = ['mobidb3']
    intype = 'fasta'
    shared_name = 'pfilt'

    def __init__(self, _input_file, _bin_directory, _architecture, _threshold):
        super(Pfilt, self).__init__(_input_file, _bin_directory, _architecture)
        self.threshold = _threshold[self.tag]

        self.command = '{}/bin{}/pfilt {}'.format(
            self.bin_directory, self.architecture, self.input_file)

    def parse(self, output):
        probs = list()
        for line in output.decode('utf-8').split("\n"):
            if len(line) > 0 and line[0] != '>':
                for pred in line:
                    if pred == 'X':
                        probs.append(1.0)
                    else:
                        probs.append(0.0)
        if probs:
            return [Prediction(self.tag, probs, self.threshold, self.types)]


class FESS(Predictor):
    tag = ['fess_helix', 'fess_sheet', 'fess_coil']
    types = ['sspops']
    groups = ['mobidb3']
    intype = 'fasta'
    shared_name = 'fess'

    def __init__(self, _input_file, _bin_directory, _architecture, _thresholds):
        super(FESS, self).__init__(_input_file, _bin_directory, _architecture)
        self.thresholds = {subtag: _thresholds[subtag] for subtag in self.tag}

        self.cwd = os.path.join(self.bin_directory, 'bin{}'.format(self.architecture))
        self.command = '{}/bin{}/fess {}'.format(
            self.bin_directory, self.architecture, self.input_file)

    def parse(self, output):
        probs = {t: list() for t in self.tag}

        for output_line in output.decode('utf-8').split("\n"):
            if not output_line.startswith("#"):
                output_line = output_line.split()

                if len(output_line) > 0:
                    # LINE: aminoacid, secstruct, helix_score, sheet_score, coil_score
                    for subtag, score in zip(self.tag, output_line[2:]):
                        probs[subtag].append(float(score))

        preds = list()
        for subtag in self.tag:
            if probs[subtag]:
                preds.append(Prediction(subtag, probs[subtag], self.thresholds[subtag], self.types))

        return preds


class DynaMine(Predictor):
    tag = 'dynamine_coil'
    types = ['sspops']
    groups = ['mobidb3']
    intype = 'fasta'
    shared_name = 'dynamine'

    def __init__(self, _input_file, _bin_directory, _architecture, _threshold):
        super(DynaMine, self).__init__(_input_file, _bin_directory, _architecture)
        self.threshold = _threshold[self.tag]

        self.command = 'python2 {}/runFasta.py {}'.format(self.bin_directory, self.input_file)

    def parse(self, output):
        probs = [float(line.split()[-1]) for line in output.decode('utf-8').split("\n")[3:] if line]

        if probs:
            return [Prediction(self.tag, probs, self.threshold, self.types)]


class Anchor(Predictor):
    tag = 'anchor'
    types = ['bindsite']
    groups = ['mobidb3']
    intype = 'fasta'
    shared_name = 'anchor'

    def __init__(self, _input_file, _bin_directory, _architecture, _threshold):
        super(Anchor, self).__init__(_input_file, _bin_directory, _architecture)
        self.threshold = _threshold[self.tag]

        self.cwd = self.bin_directory
        self.command = '{}/anchor {}'.format(self.bin_directory, self.input_file)

    def parse(self, output):
        """
        Parse anchor output and extract binding regions

        Return and save (in a .json file) a dictionary with binding sites,
        linear motifs and protein length for each protein present in anchor
        output

        :param output: Original text output from Anchor
        :types output: str

        :return: xray_fasta regions per protein
        :rtype: dict
        """

        probs = list()

        for line in output.decode('utf-8').split("\n"):
            # check if the first character is a residue position
            if not line.startswith("#"):
                line = line.strip().split()

                if len(line) > 0:
                    probs.append(float(line[2]))

        if probs:
            return [Prediction(self.tag, probs, self.threshold, self.types)]
