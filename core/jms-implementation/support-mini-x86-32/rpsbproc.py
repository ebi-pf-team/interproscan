import sys
import os.path
import shutil

def get_properties(interproscan_dir):
  d = {}
  with open(interproscan_dir + '/interproscan.properties') as f:
      for line in f:
        tokens = line.split('=')
        d[tokens[0]] = '='.join(tokens[1:])
  return d

#rpsbproc_ini_file = 'rpsbproc.ini'
interproscan_dir = os.path.dirname(os.path.abspath(__file__))
if sys.argv[1]:
   interproscan_dir = sys.argv[1]

d = get_properties(interproscan_dir)
rpsbproc_ini_file = d['cdd.rpsbproc.data.config.path'].strip()
cdd_data_path = d['cdd.data.path']
cdd_data_path = cdd_data_path.strip() + '/'

#print "interproscan install dir", interproscan_dir
absolute_cdd_data_path = interproscan_dir + "/" + cdd_data_path

#print "absolute_cdd_data_path:", absolute_cdd_data_path

if os.path.isfile(rpsbproc_ini_file):
  # with open(rpsbproc_ini_file, 'r') as inf:
  #   for line in inf:
  #     print line
  shutil.move(rpsbproc_ini_file, rpsbproc_ini_file.strip() + ".default")
  # print "moved ", rpsbproc_ini_file,

print rpsbproc_ini_file,
print cdd_data_path
#sys.exit(0)

with open(rpsbproc_ini_file, 'a') as config_file:
  config_file.write('[datapath]\n')
  config_file.write('cdd = ' + absolute_cdd_data_path + 'cddid.tbl\n')
  config_file.write('cdt = ' + absolute_cdd_data_path + 'cdtrack.txt\n')
  config_file.write('clst = ' + absolute_cdd_data_path + 'family_superfamily_links\n')
  config_file.write('feats = ' + absolute_cdd_data_path + 'cddannot.dat\n')
  config_file.write('genfeats = ' + absolute_cdd_data_path + 'cddannot_generic.dat\n')
  config_file.write('spthr = ' + absolute_cdd_data_path + 'bitscore_specific_3.14.txt\n')
  # config_file.write('----')

# print "done "
