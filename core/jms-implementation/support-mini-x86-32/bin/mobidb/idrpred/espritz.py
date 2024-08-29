# Licensed under the GPL: https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
# For details: https://github.com/matthiasblum/idrpred/blob/main/LICENSE
# Copyright (c) Matthias Blum <mblum@ebi.ac.uk>

import os
from subprocess import DEVNULL, PIPE, Popen


def parse(output):
    c = {"D", "O"}
    scores = []
    for line in output.splitlines():
        if line and line[0] in c:
            _, score = line.split("\t")
            scores.append(float(score))

    return scores


def run_espritz_d(bindir: str, file: str):
    cmd = [
        os.path.join(bindir, "disbinD"),
        os.path.join(bindir, "model_definition", "ensembleD"),
        file,
        "/dev/null"
    ]
    proc = Popen(cmd, stdout=PIPE, stderr=DEVNULL, cwd=bindir, encoding="utf-8")
    out, err = proc.communicate()
    return parse(out) if proc.returncode == 0 else None


def run_espritz_n(bindir: str, file: str):
    cmd = [
        os.path.join(bindir, "disbinN"),
        os.path.join(bindir, "model_definition", "ensembleN"),
        file,
        "/dev/null"
    ]
    proc = Popen(cmd, stdout=PIPE, stderr=DEVNULL, cwd=bindir, encoding="utf-8")
    out, err = proc.communicate()
    return parse(out) if proc.returncode == 0 else None


def run_espritz_x(bindir: str, file: str):
    cmd = [
        os.path.join(bindir, "disbinX"),
        os.path.join(bindir, "model_definition", "ensembleX"),
        file,
        "/dev/null"
    ]
    proc = Popen(cmd, stdout=PIPE, stderr=DEVNULL, cwd=bindir, encoding="utf-8")
    out, err = proc.communicate()
    return parse(out) if proc.returncode == 0 else None
