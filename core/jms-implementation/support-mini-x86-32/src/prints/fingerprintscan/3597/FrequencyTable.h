#ifndef FREQUENCYTABLE_H
#define FREQUENCYTABLE_H

#include <cmath>
#include <algorithm>
#include <vector>
using namespace std;


#ifndef SUBSTITUTIONMATRIX_H
#include "SubstitutionMatrix.h"
#endif

#ifndef MYSTRING_H 
#include "mystring.h"
#endif

#ifndef SCORE_H
#include "Score.h"
#endif

/*
FingerPRINTScan II: A PRINTS fingerprint identification algorithm.
Copyright (C) 1998,1999  Phil Scordis

Phil Scordis can be contacted at scordis@bioinf.man.ac.uk, and
Manchester Bioinformatics, rm 2.19, School of Biological Sciences,
Stopford Building, University of Manchester, Oxford Road,
Manchester, M13 9PT, UK.

This work was supported by a grant from Astra Pharmaceuticals and
developed at University College London and Manchester University.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
USA, or alternatively look at http://www.gnu.org/copyleft/gpl.html.
*/

class FrequencyTable {
  friend class FingerPrint;
  friend class Motif;

private:
  const int threshold;
  const int table_length;
  int * * ft_table;// the 2D array of frequency information
  int * * profile; // 2d array of profile info,
   // rows == aa's (0-20), cols == positions in motif

  int Score(const char aa, const int position, int * idscore);
  int Scoremystring(const char * seq, int * idscore);
  float Weight(const char aa, const int position);
  float Weight(const int index, const int position);
  float LogWeight(const char aa, const int position);
  const int num_of_codes;
  static const int ALPHABETSIZE = 26;
  SubstitutionMatrix * sm; // pointer to SM
  static const int MatrixMultiplier = 10;
  mystring consensus;
public:
  FrequencyTable(const int tl, const int nc, SubstitutionMatrix * subs, int thresh);
  // constructor creates frequency table
  ~FrequencyTable();
  void PrintTable(); // for test purposes
  void SetK(double k) { K = k;}
  void Setl(double l) { lambda = l;}
  void InsertListItem(const char aa, const int position);
  void DigestSequenceGraph(mystring seq,  MotifScore * motifscore);
  void DigestSequenceUnweighted(mystring seq,  MotifScore * motifscore);
  void DigestSequence(mystring seq,  MotifScore * motifscore);
  void ComputeProfile(int COMPACT);
  void insertProfileElement(int col, int pos, int val);
  void insertFreqElement(int col, int pos, int val);
  mystring Getcons() { Consensus(); return consensus; } 
  double K;
  double lambda;
private:
  int Consensus();
  int karlin(int low, int high, double* pr, double* lambda,double* K, double* H);
  int gcd(int a, int b); 
  double p_value (int S, double K, int N, double lambda);
};


#endif
