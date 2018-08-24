import os
import re
import base64
import codecs
import hashlib
import logging
import contextlib
from multiprocessing import Pool
from tempfile import NamedTemporaryFile

# relative imports
from mdblib import predictor

uniprot_acc_pattern = re.compile(
    "[OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2}")


class Protein(object):
    """
    Defines a protein entity and manage operations relative to proteins.

    :param _accession: identifier of the protein object
    :type _accession: str
    :param _sequence: amino acid sequence of the protein object
    :type _sequence: str
    """
    def __init__(self, _accession, _sequence):
        # passed attributes
        self.accession = _accession
        self.sequence = _sequence
        # computed attributes
        search = uniprot_acc_pattern.search(self.accession)
        self.uniprot_acc = search.group(0) if search else None
        self.secure_acc = self.accession.replace('|', '-').split()[0]
        # handle attributes
        self.seguid = None
        self.reprs = None
        self.predictions = None

    def compute_seguid(self):
        """Generate protein sequence hash

        :return: hashed protein sequence
        :rtype: str
        """
        self.sequence = codecs.latin_1_encode(self.sequence.upper())[0]

        m = hashlib.sha1()
        m.update(self.sequence)

        return base64.b64encode(m.digest()).rstrip("=")

    def generate_repr(self):
        """Generate temporary files representing a protein used as input of the predictors.

        Different predictors want different input formats. This function provides
        the 3 input format required from the protein sequence and accession. The 3
        formats required are:

        * disbin format::

            1
            sequence length
            sequence

        * flat format::

             sequence

        * fasta format::

            >accession
            sequence

        :return: filenames of the disbin, flat and fasta temporary files
        :rtype: str
        """

        f_disbin = NamedTemporaryFile(
            delete=False, prefix="{}-disbin".format(self.secure_acc))
        f_flat = NamedTemporaryFile(
            delete=False, prefix="{}-flat".format(self.secure_acc))
        f_fasta = NamedTemporaryFile(
            delete=False, prefix="{}-fasta".format(self.secure_acc))

        f_disbin.write("1\n{}\n{}".format(len(self.sequence), self.sequence).encode('utf-8'))
        f_flat.write(self.sequence.encode('utf-8'))
        f_fasta.write(">{}\n{}\n".format(self.accession, self.sequence).encode('utf-8'))

        # set temporary files name
        logging.debug('Tempfiles generated')
        self.reprs = {
            'disbin': f_disbin.name,
            'flat': f_flat.name,
            'fasta': f_fasta.name
        }

    def delete_repr(self):
        """
        Delete temporary files.
        """
        with contextlib.suppress(FileNotFoundError):
            for fmt in self.reprs:
                os.remove(self.reprs[fmt])

    def run_predictors(self, outgroup, bin_dirs, thresholds, architecture, processes):
        """Parallel call to predictors

        :param outgroup: tag indicating which predictors will be executed, can be configured in
            .ini file
        :type outgroup: str
        :param bin_dirs: Directory of the predictor executables
        :type bin_dirs: dict
        :param thresholds: probability cutoff for discriminating ID from structure
            for each predictor
        :type thresholds: dict
        :param architecture: 32- or 64-bit OS architecture
        :type architecture: str
        :param processes: Number of worker processes of the process
            :py:class:`Pool` object
        :type processes: int
        """
        pool = Pool(processes) if processes > 0 else None

        preds = list()

        for subcl in predictor.Predictor.__subclasses__():
            if outgroup in subcl.groups:
                pred = subcl(
                    self.reprs[subcl.intype], bin_dirs[subcl.shared_name], architecture, thresholds)

                if pool is not None:
                    preds.append(pool.apply_async(pred.run))
                else:
                    logging.debug('Running predictor %s', pred.shared_name)
                    prediction = pred.run()
                    if prediction:
                        preds.extend(prediction)

        if pool is not None:
            pool.close()
            pool.join()

        if preds:
            if pool is not None:
                preds = self._unpack_pool_results(preds)

            self.predictions = preds

        else:
            log_acc = self.uniprot_acc if self.uniprot_acc else self.secure_acc
            logging.error("%s | No predictors output", log_acc)

    @staticmethod
    def _unpack_pool_results(pool_results):
        """Extract python data structures from pickled apply results

        :param pool_results: list of `multiprocessing.pool.ApplyResult`s
        :type pool_results: list

        :return: Unpacked apply results
        :rtype: dict
        """
        unpacked_results = list()
        for applyresult_obj in pool_results:

            try:
                if applyresult_obj.get():
                    for applyresult in applyresult_obj.get():
                        if applyresult:
                            unpacked_results.append(applyresult)
            except Exception as e:
                logging.warning(e)

        return unpacked_results
