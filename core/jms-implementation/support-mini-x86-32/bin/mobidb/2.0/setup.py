"""
MobiDB-Lite setup. Only needed to state requirements.
No requirement is needed for output formats other than 2.
"""
import mobidb_lite

VERSION = mobidb_lite.__version__

try:
    from setuptools import setup
except ImportError:
    setup = None
    print("---------------- WARNING -------------------"
          "This setup runs on the setuptools library.\n"
          "Run `pip install setuptools` to install it.\n\n"
          "You will only need to install setuptools\n"
          "if you want to have the output format 2.\n"
          "See the README for further information.\n"
          "--------------------------------------------\n")
    raise ImportError


INSTALL_REQUIRES = ['numpy >= 1.9.0']

setup(
    name='mobidb_lite',
    version=VERSION,
    url='',
    license='',
    author='Marco Necci, Damiano Piovesan',
    author_email='rvrmarco@gmail.com',
    description='MobiDB-lite is a disorder metapredictor based on 8 fast disorder predictors',
    install_requires=INSTALL_REQUIRES)
