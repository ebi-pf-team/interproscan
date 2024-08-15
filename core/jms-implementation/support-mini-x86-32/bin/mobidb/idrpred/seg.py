import os
from subprocess import DEVNULL, PIPE, Popen


def run(bindir: str, file: str):
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
