"""
Contain States class which abstracts series of statuses. It is used to operate over states,
applying mathematical morphology and translation to other representations (regions).
"""

import re
import sys
import copy
import math
import logging
from itertools import groupby
from operator import itemgetter


class States(object):
    """
    States class
    """
    def __init__(self, _states):
        self.states = _states

    def translate_states(self, translation):
        """translate states from a set of characters to another one

        :param translation: translation dictionary, map a character to the one it must replace
        :return: dict
        """
        self.states = [translation[status] for status in self.states]

    def math_morphology(self, rmax=3, tags=('D', 'S')):
        """Erosion/Dilation implementation of Order/Disorder states

        Mathematical Morphology (MM) Erosion & Dilation implementation of
        Order/Disorder states. Erosion and Dilation are the most basic
        morphological operations. Morphological Operations are part of Morphology,
        a set of image processing operations.

        *Example of Dilation & Erosion*


        .. figure:: imgs/dil-ero.png
            :scale: 50%
            :align: center

        Here a simple implementation of Erosion and Dilation processes are used	to
        eliminate spurious order inside disorder and vice versa. In particular,
        binary representation of order or disorder states along a protein
        sequence are treated as pixels in a mono-dimensional image. This "image"
        undergoes ``rmax`` iterations of ID dilation and ``rmax`` iterations of ID
        erosion. During each iteration the considered-context distance is around
        a region increases by one (on each side). When a region is surrounded by
        two regions with opposite state, it is changed to that state.

        *MobiDB-Lite Dilation & Erosion Implementation*

        .. figure:: imgs/dil-ero_mbdbl.png
            :scale: 26%
            :align: center


        :param rmax: Max dimension of a region to be changed. Regions bigger than
            ``rmax``, won't switch state even if the context condition is
            satisfied.
        :type rmax: int
        :param tags: The two identifiers of the states of the prediction
        :type tags: tuple
        """
        dis_tag, str_tag = tags

        if not isinstance(rmax, int):
            logging.error("Mathematical Morphology length parameter must be an integer, "
                            "instead its: %s", rmax)
            raise ValueError

        else:
            logging.debug('startseq: %s', ''.join(self.states))

            if isinstance(self.states, list):
                states = ''.join(map(str, self.states))
            else:
                states = self.states

            # Disorder expansion
            states = rmax * dis_tag + states + rmax * dis_tag

            for rlevel in range(1, rmax + 1):
                pattern = dis_tag * rlevel + str_tag * rlevel + dis_tag * rlevel
                new_pattern = dis_tag * rlevel + dis_tag * rlevel + dis_tag * rlevel

                for _ in range(0, rlevel + 1):
                    states = states.replace(pattern, new_pattern)

            # Disorder contraction
            states = rmax * str_tag + states[rmax: -rmax] + rmax * str_tag

            for rlevel in range(1, rmax + 1):
                pattern = str_tag * rlevel + dis_tag * rlevel + str_tag * rlevel
                new_pattern = str_tag * rlevel + str_tag * rlevel + str_tag * rlevel

                for _ in range(0, rlevel + 1):
                    states = states.replace(pattern, new_pattern)

            self.states = states[rmax: -rmax]
            logging.debug('matmorph: %s', self.states)

    def merge_close_longidrs(self, tags=('D', 'S')):
        """Merge two long IDRs separated by a short structured region

        Convert stretches of structured residues flanked by long IDRs into ID
        residues, if the stretches are at most 10 residues long. To identify
        these stretches the following regex is used :regexp:`D{21,}S{1,10}D{21,}`


        :return: Post-processed consensus with extended long IDRs
        :rtype: str
        """

        there_are_matches = True
        dis_tag, str_tag = tags
        pattern = re.compile("{0}{{21,}}{1}{{1,10}}{0}{{21,}}".format(dis_tag, str_tag))
        merged_states = self.states

        while there_are_matches:
            match_obj = pattern.search(merged_states)
            if match_obj:
                match_length = match_obj.end(0) - match_obj.start(0)

                merged_states = merged_states[:match_obj.start(0)] \
                    + dis_tag * match_length + \
                    merged_states[match_obj.end(0):]
            else:
                there_are_matches = False

        if merged_states is not None:
            self.states = merged_states

        logging.debug('mergeidr: %s', self.states)

    def to_regions(self, keep_none=False, translate_states=None,
                   start_index=0, positivetag=None, len_thr=1):
        """Transform multiclass list or string into a list of disorder regions (start/end)

        :param keep_none: modifier, if true return regions of missing annotation
        :type keep_none: bool
        :param translate_states: translation dictionary to replace states classes to
            with custom classes
        :type translate_states: dict
        :param start_index: left-to-right shifts for the start residue, usual values are 0 or 1
            but any integer value is accepted.
        :type start_index: int
        :param positivetag: identifier of positive matches of the predictor. If None, all matches
            are returned
        :type positivetag: str|int|None
        :param len_thr: minimum length of a region to be returned (default: 1)
        :return: list of tuples (start, end, class tag)
        :rtype: list
        """

        regions = list()

        for status, groups in groupby(enumerate(self.states), key=itemgetter(1)):
            groups = list(groups)
            if keep_none is True or (keep_none is False and status is not None):
                start = groups[0][0] + start_index
                end = groups[-1][0] + start_index

                if end - start + 1 >= len_thr:
                    status = translate_states[status] if translate_states else status

                    if not positivetag or  status == positivetag or \
                            (translate_states and status == translate_states[positivetag]):
                        regions.append([start, end, status])

        return regions

    @staticmethod
    def get_disorder_class(seq):
        """Classify protein sequences into ID types.

        Classify submitted sequence into one of the four flavors among:

            * Weakly charged
            * Strong polyampholytes
            * Highly positive polyelectrolytes
            * Highly negative polyelectrolytes

        Flavor definition is derived from (Das and Pappu, 2013) diagram of
        states for IDPs. However since class 1 and 2 of that classification is
        hardly distinguishable, we join them in an single class called
        *Weakly Charged*.

        .. figure:: imgs/daspappudiag.gif
            :align: left
            :width: 350
            :figwidth: 100%

            Das and Pappu diagram of states for IDPs

        :param seq: amino acid sequence (or portion of sequence) to classify
        :type seq: str

        :return: identifier of the ID class
        :rtype: str
        """
        id_class = ''
        seqlen = float(len(seq))

        # make translation table for amino acids
        intab = 'RKDEACFGHILMNPQSTVWY'
        outab = 'PPNN____P___________'
        trantab = str.maketrans(intab, outab)

        # translate sequence
        seq = seq.translate(trantab)

        # compute sequence scores
        f_plus = seq.count("P") / seqlen
        f_minus = seq.count("N") / seqlen
        fcr = f_plus + f_minus
        ncpr = abs(f_plus - f_minus)

        # classify sequence based on scores
        # strong charge
        if fcr > 0.35:
            # polyampholytes
            if ncpr <= 0.35 or (f_minus > 0.35 and f_plus > 0.35):
                id_class = 'PA'
            # polyelectrolytes
            else:
                # positive charge
                if f_plus > 0.35:
                    id_class = 'PPE'
                # negative charge
                if f_minus > 0.35:
                    id_class = 'NPE'
        # weak charge
        else:
            id_class = 'WC'

        if not id_class:
            logging.warning('Couldn\'t define ID flavor for seq: %s', seq)

        return id_class

    def is_enriched(self, subset, threshold=0.32):
        """
        Return True if a subset of elements in an iterable occurs more frequently than a threshold

        :param subset: elements to count the occurrences of
        :type subset: list
        :param threshold: minimum frequency with which the subset is considered enriched
        :type threshold: float
        :return: True if enriched else False
        :rtype: bool
        """
        is_enriched = False
        s = sum(self.states.count(a) for a in subset) / len(self.states)

        if s >= threshold:
            is_enriched = True

        return is_enriched

    def tokenize(self, n=7):
        """
        Tokenize an iterable in a series of adjacent windows

        Yield a series of blobs of size n * 2 + 1 with the
        actual residue in the middle.

        For ending residues mirrored windows are added.

        :param n: n * 2 + 1 is the size of the window
        :type n: int
        :return: an iterator of blobs
        :rtype: iterator
        """

        new_seq = copy.copy(self.states[1:n + 1][::-1])  # Reversed start
        new_seq += copy.copy(self.states)
        new_seq += copy.copy(self.states[-n - 1:-1][::-1])  # Reversed end
        seq = new_seq

        for pos in range(n, len(seq) - n):
            yield seq[pos - n:pos + n + 1]

    def make_binary(self, active, inactive='0'):
        """
        Transform multi-class states in binary states (setter method).

        Transform multi-class states in binary states by setting all states
        different from a specified active (1) value to the value of inactive (0)

        :param active: the value to be considered active
        :type active: str
        :param inactive: the value to insert when the active value does not occur
        :type inactive: str
        """

        self.states = ''.join(x if x == active else inactive for x in self.states)