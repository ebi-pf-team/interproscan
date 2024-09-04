# Licensed under the GPL: https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
# For details: https://github.com/matthiasblum/idrpred/blob/main/LICENSE
# Copyright (c) Matthias Blum <mblum@ebi.ac.uk>

import os
from subprocess import DEVNULL, PIPE, Popen


def run(bindir: str, file: str) -> list[int] | None:
    cmd = [
        os.path.join(bindir, "seg"),
        file,
        "-x"
    ]
    proc = Popen(cmd, stdout=PIPE, stderr=DEVNULL, encoding="utf-8")
    out, err = proc.communicate()

    if proc.returncode == 0:
        scores = []
        for line in out.splitlines():
            if line and line[0] != ">":
                for aa in line:
                    scores.append(1 if aa == "x" else 0)

        return scores

    return None
