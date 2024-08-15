import os
from subprocess import DEVNULL, PIPE, Popen


def _run(bindir: str, sequence: str, mode: str):
    cmd = [
        os.path.join(bindir, "iupred_string"),
        sequence,
        mode
    ]
    proc = Popen(cmd, stdout=PIPE, stderr=DEVNULL, env={"IUPred_PATH": bindir},
                 encoding="utf-8")
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
