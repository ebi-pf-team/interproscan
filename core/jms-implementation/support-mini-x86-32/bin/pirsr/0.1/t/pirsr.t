use strict;
use warnings;
use Test::More;

use FindBin '$Bin';
use lib "$Bin/../";

use Smart::Comments;

use_ok('PIRSR');


my $data1_path = "$Bin/data1/";

my $data1_pirsr = PIRSR->new(data_folder => $data1_path);

ok($data1_pirsr, 'data1 PIRSR initiated');

ok($data1_pirsr->process_templates, 'data1 PIRSR template folder processing successful');
ok($data1_pirsr->process_hmms, 'data1 PIRSR hmm folder processing successful');
ok($data1_pirsr->process_rules, 'data1 PIRSR rule folder processing successful');

ok($data1_pirsr->process_data, 'data1 PIRSR data processed successful');

my $data1_expected = {
  'NNN-SEQ' => {},
  Q52369 => {
              'PIRSR000005-1' => {
                                   RuleSites => [
                                              {
                                                condition => 'C',
                                                desc => 'Heme 1 (covalent).',
                                                end => 34,
                                                group => '1',
                                                hmmEnd => 43,
                                                hmmStart => 43,
                                                label => 'BINDING',
                                                start => 34
                                              },
                                              {
                                                condition => 'C',
                                                desc => 'Heme 1 (covalent).',
                                                end => 37,
                                                group => '1',
                                                hmmEnd => 46,
                                                hmmStart => 46,
                                                label => 'BINDING',
                                                start => 37
                                              },
                                              {
                                                condition => 'C',
                                                desc => 'Heme 2 (covalent).',
                                                end => 139,
                                                group => '1',
                                                hmmEnd => 144,
                                                hmmStart => 144,
                                                label => 'BINDING',
                                                start => 139
                                              },
                                              {
                                                condition => 'C',
                                                desc => 'Heme 2 (covalent).',
                                                end => 142,
                                                group => '1',
                                                hmmEnd => 147,
                                                hmmStart => 147,
                                                label => 'BINDING',
                                                start => 142
                                              }
                                            ],
                                   Scope => [
                                              'Bacteria'
                                            ],
                                   aliAcc => '0.92',
                                   domEvalue => '2.7e-66',
                                   envFrom => '1',
                                   envTo => '210',
                                   evalue => '2.7e-66',
                                   hmmFrom => '1',
                                   hmmTo => '213',
                                   hmmalign => {
                                                 hmm => 'mkrvallvlllaalallaaaagaaekgkalaaagtaakavkaCaaCHgadGnsaaaayPrLAgqpaeYlvkqlkdfksge.ek.....srknavmaglakeLsdadieelaaYfasqkapkakeaekkasaeegkklyegGdaergipaCaaCHgpsgaGvepa.fprLaGqraaYiaaqLkafrngrrennsva.lmrevakklteeeikalaaYlaslr',
                                                 match => 'm++v++++ll++ ++++a+aag+ae+g+ +         v++C aCHg+dGns+a+++P+LAgq ++Yl kql+d+k+g+        +rk   m+g++++Lsd+d+e++aaYf+sq + + + a+ +++++ g+kl++gG++++g+paC+ CH+p+g G++ a fp+L+Gq+aaY+a+qL++fr+g+r+n+    +mr va kl++++i+al++Y+++l+',
                                                 pp => '677888899999999999999999999977.........556*************************************8444899999****************************.88889999999888.99*************************99643999*******************887776666**********************986',
                                                 seq => 'MNKVLVSLLLTLGITGMAHAAGDAEAGQGK---------VAVCGACHGVDGNSPAPNFPKLAGQGERYLLKQLQDIKAGStPGapegvGRKVLEMTGMLDPLSDQDLEDIAAYFSSQ-KGSVGYADPALAKQ-GEKLFRGGKLDQGMPACTGCHAPNGVGNDLAgFPKLGGQHAAYTAKQLTDFREGNRTNDGDTmIMRGVAAKLSNKDIEALSSYIQGLH'
                                               },
                                   seqFrom => '1',
                                   seqTo => '210'
                                 }
            }
};


my $data1_output = $data1_pirsr->run_query("$Bin/data1/query1.fasta");

is_deeply($data1_output, $data1_expected, 'data1 PIRSR query result correct');


done_testing();

