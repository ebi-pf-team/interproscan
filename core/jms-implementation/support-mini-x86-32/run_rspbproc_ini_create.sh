pushd `dirname $0` > /dev/null
SCRIPTPATH=`pwd`
popd > /dev/null
echo ${SCRIPTPATH}

python ${SCRIPTPATH}/rpsbproc.py ${SCRIPTPATH}
