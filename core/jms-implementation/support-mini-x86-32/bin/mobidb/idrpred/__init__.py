# Licensed under the GPL: https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
# For details: https://github.com/matthiasblum/idrpred/blob/main/LICENSE
# Copyright (c) Matthias Blum <mblum@ebi.ac.uk>

import math
import os
import re
import sys
from tempfile import mkstemp
from typing import Union

from . import disembl
from . import espritz
from . import globplot
from . import iupred
from . import seg


_THRESHOLDS = {
    "mobidblite": 0.625,
    "disembl-hl": 0.086,
    "disembl-rem465": 0.5,
    "espritz-d": 0.5072,
    "espritz-n": 0.3089,
    "espritz-x": 0.1434,
    "globplot": 0,
    "iupred-l": 0.5,
    "iupred-s": 0.5,
    "seg": 0.5,
}
_POSITIVE_FLAG = "1"
_NEGATIVE_FLAG = "0"
_POSITIVE_RESIDUES = {"H", "R", "K"}
_NEGATIVE_RESIDUES = {"D", "E"}
_FEATURES = [
    "Polyampholyte",
    "Positive Polyelectrolyte",
    "Negative Polyelectrolyte",
    "Cystein-rich",
    "Proline-rich",
    "Glycine-rich",
    "Low complexity",
    "Polar"
]


def predict(sequence_id: str, sequence: str, bindir: str, **kwargs):
    force_consensus = kwargs.get("force", False)
    round_score = kwargs.get("round", False)
    find_features = kwargs.get("find_features", True)
    merge_features = kwargs.get("merge_features", True)
    keep_non_idr_features = kwargs.get("keep_non_idr_features", False)
    tempdir = kwargs.get("tempdir")
    threshold = kwargs.get("threshold", _THRESHOLDS["mobidblite"])

    seq_length = len(sequence)
    scores = run_predictors(sequence, bindir,
                            find_features=find_features,
                            tempdir=tempdir)

    # SEG: not considered for consensus
    seg_scores = scores.pop("seg", [])

    agreement = [0] * seq_length
    num_indicators = 0

    for pred_name, pred_scores in scores.items():
        if pred_scores is None or len(pred_scores) != seq_length:
            print(f"{sequence_id}: {pred_name} failed", file=sys.stderr)
            continue

        num_indicators += 1
        pred_threshold = _THRESHOLDS[pred_name]
        for i, score in enumerate(pred_scores):
            if round_score:
                score = round(score, 3)

            if score >= pred_threshold:
                agreement[i] += 1

    if num_indicators == 0:
        return None
    elif num_indicators < len(scores) and not force_consensus:
        return None

    states = ""
    for s in agreement:
        if round_score:
            score = round(s / num_indicators, 3)
        else:
            score = s / num_indicators

        if score >= threshold:
            states += _POSITIVE_FLAG
        else:
            states += _NEGATIVE_FLAG

    states = dilate(states, max_length=3)
    states = erode(states, max_length=3)
    states = merge_long_disordered_regions(states)
    regions = get_regions(states, min_length=20)
    results = []
    if regions:
        if seg_scores is not None and len(sequence) == len(seg_scores):
            features = get_region_features(sequence, seg_scores, merge_features,
                                           keep_non_idr_features)
        else:
            features = None

        for start, end, _ in sorted(regions):
            results.append((start + 1, end + 1, "-"))

            if features:
                region = features[start:end + 1]

                for i, j, state in get_regions(region, min_length=10):
                    # state = _FEATURES[int(x)-1]
                    results.append((start + 1 + i, start + 1 + j, state))

    return results


def run_predictors(sequence: str, bindir: str, **kwargs) -> dict:
    tempdir = kwargs.get("tempdir")
    find_features = kwargs.get("find_features", True)

    fd, disbin = mkstemp(dir=tempdir)
    with open(fd, "wt") as fh:
        fh.write(f"1\n{len(sequence)}\n{sequence}")

    hot_loop, remark_465 = disembl.run(os.path.join(bindir, "DisEMBL"),
                                       os.path.join(bindir, "TISEAN"),
                                       sequence)

    results = {
        "disembl-hl": hot_loop,
        "disembl-rem465": remark_465,
        "espritz-d": espritz.run_espritz_d(os.path.join(bindir, "ESpritz"),
                                           disbin),
        "espritz-n": espritz.run_espritz_n(os.path.join(bindir, "ESpritz"),
                                           disbin),
        "espritz-x": espritz.run_espritz_x(os.path.join(bindir, "ESpritz"),
                                           disbin),
        "globplot": globplot.run(os.path.join(bindir, "TISEAN"), sequence),
        "iupred-l": iupred.run_long(os.path.join(bindir, "IUPred"), sequence),
        "iupred-s": iupred.run_short(os.path.join(bindir, "IUPred"), sequence),
    }

    os.unlink(disbin)

    if find_features:
        fd, fasta = mkstemp(dir=tempdir)
        with open(fd, "wt") as fh:
            fh.write(f">1\n{sequence}\n")

        results["seg"] = seg.run(os.path.join(bindir, "SEG"), fasta)
        os.unlink(fasta)

    return results


def dilate(states: str, max_length: int) -> str:
    states = "{0}{1}{0}".format(_POSITIVE_FLAG * max_length, states)

    for level in range(1, max_length + 1):
        old = "{0}{1}{0}".format(_POSITIVE_FLAG * level,
                                 _NEGATIVE_FLAG * level)
        new = "{0}{0}{0}".format(_POSITIVE_FLAG * level)

        for _ in range(level + 1):
            states = states.replace(old, new)

    return states[max_length:-max_length]


def erode(states: str, max_length: int) -> str:
    states = "{0}{1}{0}".format(_NEGATIVE_FLAG * max_length, states)

    for level in range(1, max_length + 1):
        old = "{0}{1}{0}".format(_NEGATIVE_FLAG * level,
                                 _POSITIVE_FLAG * level)
        new = "{0}{0}{0}".format(_NEGATIVE_FLAG * level)

        for _ in range(level + 1):
            states = states.replace(old, new)

    return states[max_length:-max_length]


def merge_long_disordered_regions(states: str) -> str:
    while True:
        new_states = re.sub(
            pattern=r"{p}{{21,}}{n}{{1,10}}{p}{{21,}}".format(
                p=_POSITIVE_FLAG,
                n=_NEGATIVE_FLAG
            ),
            repl=_repl_struct_by_disord,
            string=states)

        if new_states == states:
            return new_states

        states = new_states


def _repl_struct_by_disord(match: re.Match):
    return _POSITIVE_FLAG * len(match.group(0))


def get_regions(states: Union[list, str], min_length: int) -> list:
    # Return 0-indexed regions
    regions = []
    start = None
    current_flag = None
    for i, flag in enumerate(states):
        if flag != current_flag:
            if start is not None and current_flag != _NEGATIVE_FLAG:
                end = i - 1
                length = end - start + 1
                if length >= min_length:
                    regions.append((start, end, current_flag))

            start = i
            current_flag = flag

    if start is not None and current_flag != _NEGATIVE_FLAG:
        end = len(states) - 1
        length = end - start + 1
        if length >= min_length:
            regions.append((start, end, current_flag))

    return regions


def get_region_features(sequence: str, seg_scores: list,
                        merge_features: bool = True,
                        keep_non_idr_features: bool = False) -> list:
    threshold = _THRESHOLDS["seg"]
    seg_states = [s >= threshold for s in seg_scores]

    all_features = {}
    for state in _FEATURES:
        all_features[state] = [_NEGATIVE_FLAG] * len(sequence)

    features = [_NEGATIVE_FLAG] * len(sequence)

    for i, seq in _bin(sequence, size=7):
        """
        Classify sequence in one of the disorder states

        See Fig 7 from Das & Pappu (2013)
        https://www.pnas.org/doi/full/10.1073/pnas.1304749110
        """
        f_plus = f_minus = 0

        for aa in seq:
            if aa in _POSITIVE_RESIDUES:
                f_plus += 1
            elif aa in _NEGATIVE_RESIDUES:
                f_minus += 1

        f_plus /= len(seq)
        f_minus /= len(seq)
        # fraction of charged residues
        fcr = f_plus + f_minus
        # net charge per residue
        ncpr = abs(f_plus - f_minus)

        state = None
        if fcr > 0.35:
            if ncpr <= 0.35:
                # Strong polyampholyte
                state = "Polyampholyte"
            elif f_plus > 0.35:
                # Strong positive polyelectrolyte
                state = "Positive Polyelectrolyte"
            elif f_minus > 0.35:
                # Strong negative polyelectrolyte
                state = "Negative Polyelectrolyte"
            else:
                raise ValueError(f"{seq}: {f_plus}, {f_minus}")
        else:
            # Weak polyampholyte/polyelectrolyte
            cycstein = proline = glycine = polar = 0
            for aa in seq:
                if aa == "C":
                    cycstein += 1
                elif aa == "P":
                    proline += 1
                elif aa == "G":
                    glycine += 1
                elif aa in {"N", "Q", "S", "T"}:
                    polar += 1

            if cycstein / len(seq) >= 0.32:
                state = "Cystein-rich"
            elif proline / len(seq) >= 0.32:
                state = "Proline-rich"
            elif glycine / len(seq) >= 0.32:
                state = "Glycine-rich"
            elif seg_states[i]:
                state = "Low complexity"
            elif polar / len(seq) >= 0.32:
                state = "Polar"

        if state:
            all_features[state][i] = _POSITIVE_FLAG

    # Merge features, hierarchically (from Polar to Polyampholyte)
    for state in reversed(_FEATURES):
        states = "".join(all_features[state])
        states = dilate(states, max_length=5)
        states = erode(states, max_length=5)

        if merge_features:
            for i, flag in enumerate(states):
                if flag == _POSITIVE_FLAG:
                    features[i] = state
        else:
            # todo
            raise NotImplementedError

    return features


def _bin(sequence: str, size: int):
    n = math.floor((size - 1) / 2)
    seq_length = len(sequence)
    for i in range(seq_length):
        if i < n:
            yield i, sequence[n::-1] + sequence[1:n + 1]
        elif i + n < seq_length:
            yield i, sequence[i - n:i + n + 1]
        else:
            x = seq_length - (i + n) + 1
            yield i, sequence[i - n:] + sequence[::-1][1:x + 1]


def is_enriched(sequence: str, residues: set, threshold: float = 0.32):
    return len([aa for aa in sequence
                if aa in residues]) / len(sequence) >= threshold
