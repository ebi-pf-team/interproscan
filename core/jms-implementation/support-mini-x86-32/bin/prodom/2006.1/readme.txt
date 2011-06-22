ProDom 2006.1 member database implementation in InterProScan 5
==============================================================

Note:

The location of the ProDomBlast3i.pl can be overriden if required using the configuration in the interproscan.properties
file. Please be aware that the ProDomBlast3i.pl perl script expects to find the following dependant files and
directories in the same location (this cannot be configured).

ParseBlastXmlReport
calcs/Report1.pm
calcs/XML_BLAST/HSP.pm
calcs/XML_BLAST/Iter.pm
calcs/XML_BLAST/Report.pm
calcs/XML_BLAST/Report1.pm

If this point is not taken into consideration when modifying the default location of ProDomBlast3i.pl then this could
lead to errors.
