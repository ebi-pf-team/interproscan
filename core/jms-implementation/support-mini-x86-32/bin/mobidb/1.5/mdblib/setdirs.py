import os
import logging


def predictors(cd, cf_parser, outfmt):
    src_dirs = dict()

    packages_in_binx = os.listdir('{}/binx/'.format(cd))
    preds, dirs = zip(*cf_parser.items('bin_directories'))

    missing_option_keys = set(packages_in_binx) - set(preds)

    if missing_option_keys:
        logging.error(
            'One or more keys necessary for output format <%s> are missing. Check your config.ini '
            'and make sure that all necessary keys are present. | MISSING KEY(S): %s',
            outfmt if outfmt is not False else 'DEFAULT', '; '.join(list(missing_option_keys)))
        raise ValueError

    for pred, directory in zip(preds, dirs):
        if not directory:
            default_bin = "{}/binx/{}".format(cd, pred)

            logging.debug(
                '{}: bin directory not set in config.ini; using default path: {}'.format(
                    pred.upper(), default_bin))
            src_dirs[pred] = default_bin
        else:
            src_dirs[pred] = os.path.abspath(directory)

    return src_dirs
