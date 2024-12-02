import os
import sys


if __name__ == "__main__":
    dirpath = os.path.abspath(os.path.dirname(__file__))
    sys.path.insert(0, os.path.join(dirpath, "src"))

    from idrpred.idrpred import main
    main()
