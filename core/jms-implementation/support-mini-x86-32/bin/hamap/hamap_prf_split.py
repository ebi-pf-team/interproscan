import os, sys
import re

data_dir = sys.argv[1]
out_dir = data_dir + '/profiles/'

if not os.path.exists(out_dir):
  os.makedirs(out_dir)

profile_delimiter = "//"
accession_pattern = re.compile('AC\s+(MF_\S+)\;')
id_pattern = re.compile('^ID\s+(\S+)')


count  = 0
accession_file = None
with open(data_dir + '/hamap.prf', 'r') as profiles:
  for line in profiles:
    if (id_pattern.match(line)):
      id_line = line
      continue
    if(accession_pattern.match(line)):
      accession = accession_pattern.match(line).group(1)
      accession_file = out_dir + accession + ".prf"
      with open(accession_file,'a') as profile:
        profile.write(id_line)
        profile.write(line)
        id_line = ''
      count += 1
    else:
      if accession_file:
        with open(accession_file,'a') as profile:
          profile.write(line)

print('Total number of profiles: ', count)
