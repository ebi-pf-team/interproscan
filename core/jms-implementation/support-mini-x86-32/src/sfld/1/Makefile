# Example makefile - note that you will have to install the easel library to compile this
# Replace path in EASEL_DIR below with the 'easel' subdir of the hmmer distribution
# See README for more information.
#
#EASEL_DIR=/path/to/hmmer-3.1b2/easel
EASEL_DIR=

CFLAGS = -I${EASEL_DIR} -O2
LDFLAGS = -L${EASEL_DIR}
LDLIBS = -leasel -lm

all: sfld_preprocess sfld_postprocess

clean:
	rm -f sfld_preprocess sfld_postprocess
