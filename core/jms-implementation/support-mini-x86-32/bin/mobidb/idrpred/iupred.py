# Licensed under the GPL: https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
# For details: https://github.com/matthiasblum/idrpred/blob/main/LICENSE
# Copyright (c) Matthias Blum <mblum@ebi.ac.uk>

import os
from subprocess import DEVNULL, PIPE, Popen


def _run(bindir: str, sequence: str, mode: str):
    cmd = [
        os.path.join(bindir, "iupred_string"),
        sequence,
        mode
    ]
    try:
        proc = Popen(cmd, stdout=PIPE, stderr=DEVNULL,
                     env={"IUPred_PATH": bindir},
                    encoding="utf-8")
    except OSError:
        # Following error can occur for sequences that are too long:
        # OSError: [Errno 7] Argument list too long:
        return None

    out, err = proc.communicate()

    if proc.returncode == 0:
        scores = []
        for line in out.splitlines():
            if line and line[0] != "#":
                pos, aa, score = line.split()
                scores.append(float(score))

        return scores

    return None


def run_long(bindir: str, sequence: str):
    return _run(bindir, sequence, "long")


def run_short(bindir: str, sequence: str):
    return _run(bindir, sequence, "short")
