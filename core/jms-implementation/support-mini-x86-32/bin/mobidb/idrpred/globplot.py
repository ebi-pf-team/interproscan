# Licensed under the GPL: https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
# For details: https://github.com/matthiasblum/idrpred/blob/main/LICENSE
# Copyright (c) Matthias Blum <mblum@ebi.ac.uk>

from .tisean import smooth

RUSSEL_LINDING = {
    'A': -0.261538461538462,
    'C': -0.0151515151515152,
    'D': 0.227629796839729,
    'E': -0.204684629516228,
    'F': -0.225572305974316,
    'G': 0.433225711769886,
    'H': -0.00121743364986608,
    'I': -0.422234699606962,
    'K': -0.100092289621613,
    'L': -0.337933495925287,
    'M': -0.225903614457831,
    'N': 0.229885057471264,
    'P': 0.552316012226663,
    'Q': -0.187676577424997,
    'R': -0.176592654077609,
    'S': 0.142883029808825,
    'T': 0.00887797506611258,
    'V': -0.386174834235195,
    'W': -0.243375458622095,
    'Y': -0.20750516775322
}


def run(bindir: str, sequence: str, smooth_frame: int = 10):
    scores = []
    s = 0
    for aa in sequence:
        s += RUSSEL_LINDING.get(aa, 0)
        scores.append(s)

    sg_scores = smooth(bindir, derivative=1, smooth_frame=smooth_frame,
                       scores=scores)
    if sg_scores:
        head = scores[:smooth_frame]
        tail = scores[-smooth_frame:]
        _head = []
        _tail = []

        for i, (h, t) in enumerate(zip(head, tail)):
            try:
                x = (head[i + 1] - h) / 2
            except IndexError:
                x = (h - head[i - 1]) / 2
            finally:
                _head.append(x)

            try:
                x = (tail[i + 1] - t) / 2
            except IndexError:
                x = (t - tail[i - 1]) / 2
            finally:
                _tail.append(x)

        sg_scores[:smooth_frame] = _head
        sg_scores[-smooth_frame:] = _tail
    
    return sg_scores
