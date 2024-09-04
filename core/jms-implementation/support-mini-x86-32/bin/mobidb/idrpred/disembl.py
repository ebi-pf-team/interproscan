# Licensed under the GPL: https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
# For details: https://github.com/matthiasblum/idrpred/blob/main/LICENSE
# Copyright (c) Matthias Blum <mblum@ebi.ac.uk>

import os
from subprocess import DEVNULL, PIPE, Popen

from .tisean import smooth


"""
Reference: http://dis.embl.de/html/paper.html
"""

ALPHABET = "ACDEFGHIKLMNPQRSTVWY"


def run(disembl_bindir: str, tisean_bindir: str, sequence: str, ):
    # Conversion of network output to probability scores
    cmd = [os.path.join(disembl_bindir, "disembl")]
    proc = Popen(cmd, stdin=PIPE, stdout=PIPE, stderr=DEVNULL, encoding="utf-8")
    out, _ = proc.communicate(input=sequence + "\n")

    if proc.returncode != 0:
        return None, None

    hot_loop = [0] * len(sequence)
    remark_465 = [0] * len(sequence)
    results = list(out.splitlines())

    j = 0
    for i, aa in enumerate(sequence):
        if aa in ALPHABET:
            values = results[j].split("\t")
            hot_loop[i] = float(values[1])
            remark_465[i] = float(values[2])
            j += 1

    return (
        _smooth(tisean_bindir, hot_loop),
        _smooth(tisean_bindir, remark_465)
    )


def _smooth(bindir, scores):
    smoothed_scores = smooth(bindir, 0, 8, scores)
    if smoothed_scores:
        return [max(v, 0) for v in smoothed_scores]

    return None
