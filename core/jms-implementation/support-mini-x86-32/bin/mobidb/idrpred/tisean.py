import os
from subprocess import DEVNULL, PIPE, Popen


def smooth(bindir: str, derivative: int, smooth_frame: float, scores: list):
    # Perform a Savitzky-Golay filter

    if len(scores) < 2 * smooth_frame:
        smooth_frame = len(scores) // 2
    elif smooth_frame == 0:
        smooth_frame = 1

    cmd = [
        os.path.join(bindir, "sav_gol"),
        "-V", "0",
        "-D", str(derivative),
        "-n", "{0},{0}".format(smooth_frame)
    ]
    proc = Popen(cmd, stdin=PIPE, stdout=PIPE, stderr=DEVNULL, encoding="utf-8")
    out, _ = proc.communicate(input="\n".join(map(str, scores)) + "\n")

    if proc.returncode == 0:
        return list(map(float, map(str.rstrip, out.splitlines())))

    return None
