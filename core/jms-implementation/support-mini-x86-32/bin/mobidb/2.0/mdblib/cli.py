import os
import argparse


def arg_parser(cd):
    """
    Command line arguments parser. For more information on Arguments see README
    or launch ``mobidb_lite_old.py --help``
    """

    parser = argparse.ArgumentParser(
        prog='mobidb_lite.py', description="MobiDB-lite: long disorder consensus predictor",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter)

    parser.add_argument(
        'fastaFile',
        help="Fasta input file. Multi-fasta is allowed. Use '-' to take input from stdin")
    parser.add_argument(
        '-o', '--outFile', default='',
        help='Output file name')
    parser.add_argument(
        '-a', '--architecture', default='64', choices=['64', '32'],
        help='System architecture. Some predictors will not be affected by this choice')
    parser.add_argument(
        '-t', '--threads', default=0, type=int,
        help="Number of parallel threads. Use 0 for true single threading")

    parser.add_argument('-l', '--log', type=str, default=None, help='log file')
    parser.add_argument("-ll", "--logLevel", default="ERROR",
                        choices=["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"],
                        help='log level filter. All levels <= choice will be displayed')

    parser.add_argument('-f', '--outputFormat', type=int, choices=range(4), default=0,
                        help='output format, see README.md for further details')

    parser.add_argument('-c', '--conf', type=str, default=os.path.join(cd, 'config.ini'),
                        help="path to an alternative configuration file.")

    parser.add_argument('-mk', '--multiplyOutputBy', type=str, default='acc',
                        help='clone output by the specified key if it is found in Fasta '
                             'header. Can only be used with -f {1,2,3}')

    parser.add_argument('-ms', '--multiplySeparator', type=str, default=',',
                        help='set the separator for the output cloning values')

    parser.add_argument('-pa', '--parseAccession', action='store_true', default=False,
                        help='use regular expressions to extract a UniProt accession '
                             'from a composed accession (e.g. xx|uniprot|yyyyy)')

    parser.add_argument('-fc', '--forceConsensus', action='store_true', default=False,
                        help='generate the output even if single predictors fail')

    parser.add_argument('-sf', '--skipFeatures', action='store_true', default=False,
                        help='does not calculate sequence features within long IDRs')

    args = parser.parse_args()

    if args.multiplyOutputBy is not None:
        if not args.multiplyOutputBy.endswith('='):
            args.multiplyOutputBy = args.multiplyOutputBy + '='

    return args
