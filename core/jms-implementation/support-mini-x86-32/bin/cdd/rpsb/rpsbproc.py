import sys

path = sys.argv[1]
print path
with open('rspb.ini', 'a') as outfile:
  outfile.write('[datapath]\n')
  outfile.write('cdd = ' + path + 'cddid.tbl\n')
  outfile.write('cdt = ' + path + 'cdtrack.txt\n')
  outfile.write('clst = ' + path + 'family_superfamily_links\n')
  outfile.write('feats = ' + path + 'cddannot.dat\n')
  outfile.write('genfeats = ' + path + 'cddannot_generic.dat\n')
  outfile.write('spthr = ' + path + 'bitscore_specific_3.14.txt\n')

