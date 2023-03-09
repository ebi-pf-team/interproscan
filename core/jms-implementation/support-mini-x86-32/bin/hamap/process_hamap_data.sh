#!/bin/bash

CWD=${PWD}


if [ "$#" -gt 1 ]; then
    echo "Usage: $0 <hamap_data_path_to_process>"
    exit 1;
fi


DATA_PATH=$1

if [ ! -d ${DATA_PATH} ]; then
    echo "Could not find data path provided: ${DATA_PATH}"
    echo "Usage: $0 <hamap_data_path_to_process>"
    exit 1;
fi

echo "Preparing to preprocess Hamap data"

cd $DATA_PATH

if [ ! -f hamap.prf.gz ] || [ ! -f hamap_model.xml ] || [ ! -f hamap_model.xml ] ; then
   echo "Could not find all required data files in $DATA_PATH folder."
   echo "Check the following files exist:"
   echo "   ${DATA_PATH}/hamap.prf.gz"
   echo "   ${DATA_PATH}/hamap_model.xml"
   echo "   ${DATA_PATH}/hamap_seed_alignments.tar.gz"
   exit 1;
fi

echo "Going to decompress ${DATA_PATH}/hamap_seed_alignments.tar.gz"
tar xzf hamap_seed_alignments.tar.gz

echo "Going to decompress ${DATA_PATH}/hamap.prf.gz"
gunzip -c hamap.prf.gz > hamap.prf

cd $CWD


echo "Generating ${DATA_PATH}/method.dat"
python3 abstract_xml_parser.py hamap ${DATA_PATH}/hamap_model.xml ${DATA_PATH}/method.dat

echo "Generating HMM profiles"
python3 build_hamap_hmms.py ${DATA_PATH}

echo "Generating ${DATA_PATH}/hamap.hmm.lib"
cat $DATA_PATH/hamap_hmms/* > $DATA_PATH/hamap.hmm.lib

echo "Generating individual profile files"
python3 hamap_prf_split.py ${DATA_PATH}


echo "Generating mini data files for interproscan"
cd $DATA_PATH
mkdir -p mini_data/profiles
cp profiles/MF_00457.prf mini_data/profiles
cp profiles/MF_01458.prf mini_data/profiles
cat profiles/MF_00457.prf profiles/MF_01458.prf > mini_data/hamap.prf
cat hamap_hmms/MF_00457.hmm hamap_hmms/MF_01458.hmm > mini_data/hamap.hmm.lib

echo "Cleaning up"
rm hamap.prf.gz
rm hamap_seed_alignments.tar.gz
rm hamap_model.xml
rm method.dat
rm -rf log
rm -rf hamap_hmms
rm -rf hamap_seed_alignments

echo "Done"
