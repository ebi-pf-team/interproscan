#include "cstdlib"
#include "SubstitutionMatrix.h"

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
 
SubstitutionMatrix::SubstitutionMatrix(int ma) 
  : matrix(ma)
{
  // create 2D array of ints
  SM = new int * [ALPHABETSIZE];
  if (SM) {
    for (int i=0; i<ALPHABETSIZE; i++)
      SM[i] = new int [ALPHABETSIZE];
  } else { cerr << "Memory Allocation Error in function SubsitutionMatrix"; }
  
  // Clear all matrix positions and fill with a value that will 
  // normally not occur in the matrix  
  for (int i=0; i<ALPHABETSIZE; i++) {
    for (int j=0; j<ALPHABETSIZE; j++) { 
      insertSubs(i,j,-99);
    }
  }
  populate_matrix();
}

SubstitutionMatrix::~SubstitutionMatrix() {
  // destroy 2d array
  for (int i=0; i < ALPHABETSIZE; i++)
    delete [] SM[i];
  delete [] SM;
}

// #  Matrix made by matblas from blosum62.iij
// #  * column uses minimum score
// #  BLOSUM Clustered Scoring Matrix in 1/2 Bit Units
// #  Blocks Database = /data/blocks_5.0/blocks.dat
// #  Cluster Percentage: >= 62
// #  Entropy =   0.6979, Expected =  -0.5209
//  A  R  N  D  C  Q  E  G  H  I  L  K  M  F  P  S  T  W  Y  V  B  Z  X  *
//  4 -1 -2 -2  0 -1 -1  0 -2 -1 -1 -1 -1 -2 -1  1  0 -3 -2  0 -2 -1  0 -4 
// -1  5  0 -2 -3  1  0 -2  0 -3 -2  2 -1 -3 -2 -1 -1 -3 -2 -3 -1  0 -1 -4 
// -2  0  6  1 -3  0  0  0  1 -3 -3  0 -2 -3 -2  1  0 -4 -2 -3  3  0 -1 -4 
// -2 -2  1  6 -3  0  2 -1 -1 -3 -4 -1 -3 -3 -1  0 -1 -4 -3 -3  4  1 -1 -4 
//  0 -3 -3 -3  9 -3 -4 -3 -3 -1 -1 -3 -1 -2 -3 -1 -1 -2 -2 -1 -3 -3 -2 -4 
// -1  1  0  0 -3  5  2 -2  0 -3 -2  1  0 -3 -1  0 -1 -2 -1 -2  0  3 -1 -4 
// -1  0  0  2 -4  2  5 -2  0 -3 -3  1 -2 -3 -1  0 -1 -3 -2 -2  1  4 -1 -4 
//  0 -2  0 -1 -3 -2 -2  6 -2 -4 -4 -2 -3 -3 -2  0 -2 -2 -3 -3 -1 -2 -1 -4 
// -2  0  1 -1 -3  0  0 -2  8 -3 -3 -1 -2 -1 -2 -1 -2 -2  2 -3  0  0 -1 -4 
// -1 -3 -3 -3 -1 -3 -3 -4 -3  4  2 -3  1  0 -3 -2 -1 -3 -1  3 -3 -3 -1 -4 
// -1 -2 -3 -4 -1 -2 -3 -4 -3  2  4 -2  2  0 -3 -2 -1 -2 -1  1 -4 -3 -1 -4 
// -1  2  0 -1 -3  1  1 -2 -1 -3 -2  5 -1 -3 -1  0 -1 -3 -2 -2  0  1 -1 -4 
// -1 -1 -2 -3 -1  0 -2 -3 -2  1  2 -1  5  0 -2 -1 -1 -1 -1  1 -3 -1 -1 -4 
// -2 -3 -3 -3 -2 -3 -3 -3 -1  0  0 -3  0  6 -4 -2 -2  1  3 -1 -3 -3 -1 -4 
// -1 -2 -2 -1 -3 -1 -1 -2 -2 -3 -3 -1 -2 -4  7 -1 -1 -4 -3 -2 -2 -1 -2 -4 
//  1 -1  1  0 -1  0  0  0 -1 -2 -2  0 -1 -2 -1  4  1 -3 -2 -2  0  0  0 -4 
//  0 -1  0 -1 -1 -1 -1 -2 -2 -1 -1 -1 -1 -2 -1  1  5 -2 -2  0 -1 -1  0 -4 
// -3 -3 -4 -4 -2 -2 -3 -2 -2 -3 -2 -3 -1  1 -4 -3 -2 11  2 -3 -4 -3 -2 -4 
// -2 -2 -2 -3 -2 -1 -2 -3  2 -1 -1 -2 -1  3 -3 -2 -2  2  7 -1 -3 -2 -1 -4 
//  0 -3 -3 -3 -1 -2 -2 -3 -3  3  1 -2  1 -1 -2 -2  0 -3 -1  4 -3 -2 -1 -4 
// -2 -1  3  4 -3  0  1 -1  0 -3 -4  0 -3 -3 -2  0 -1 -4 -3 -3  4  1 -1 -4 
// -1  0  0  1 -3  3  4 -2  0 -3 -3  1 -1 -3 -1  0 -1 -3 -2 -2  1  4 -1 -4 
//  0 -1 -1 -1 -2 -1 -1 -1 -1 -1 -1 -1 -1 -1 -2  0  0 -2 -1 -1 -1 -1 -1 -4 

// #  Matrix made by matblas from blosum80.iij
// #  * column uses minimum score
// #  BLOSUM Clustered Scoring Matrix in 1/2 Bit Units
// #  Blocks Database = /data/blocks_5.0/blocks.dat
// #  Cluster Percentage: >= 80
// #  Entropy =   0.9868, Expected =  -0.7442
//  A  R  N  D  C  Q  E  G  H  I  L  K  M  F  P  S  T  W  Y  V  B  Z  X  *
//  5 -2 -2 -2 -1 -1 -1  0 -2 -2 -2 -1 -1 -3 -1  1  0 -3 -2  0 -2 -1 -1 -6 
// -2  6 -1 -2 -4  1 -1 -3  0 -3 -3  2 -2 -4 -2 -1 -1 -4 -3 -3 -2  0 -1 -6 
// -2 -1  6  1 -3  0 -1 -1  0 -4 -4  0 -3 -4 -3  0  0 -4 -3 -4  4  0 -1 -6 
// -2 -2  1  6 -4 -1  1 -2 -2 -4 -5 -1 -4 -4 -2 -1 -1 -6 -4 -4  4  1 -2 -6 
// -1 -4 -3 -4  9 -4 -5 -4 -4 -2 -2 -4 -2 -3 -4 -2 -1 -3 -3 -1 -4 -4 -3 -6 
// -1  1  0 -1 -4  6  2 -2  1 -3 -3  1  0 -4 -2  0 -1 -3 -2 -3  0  3 -1 -6 
// -1 -1 -1  1 -5  2  6 -3  0 -4 -4  1 -2 -4 -2  0 -1 -4 -3 -3  1  4 -1 -6 
//  0 -3 -1 -2 -4 -2 -3  6 -3 -5 -4 -2 -4 -4 -3 -1 -2 -4 -4 -4 -1 -3 -2 -6 
// -2  0  0 -2 -4  1  0 -3  8 -4 -3 -1 -2 -2 -3 -1 -2 -3  2 -4 -1  0 -2 -6 
// -2 -3 -4 -4 -2 -3 -4 -5 -4  5  1 -3  1 -1 -4 -3 -1 -3 -2  3 -4 -4 -2 -6 
// -2 -3 -4 -5 -2 -3 -4 -4 -3  1  4 -3  2  0 -3 -3 -2 -2 -2  1 -4 -3 -2 -6 
// -1  2  0 -1 -4  1  1 -2 -1 -3 -3  5 -2 -4 -1 -1 -1 -4 -3 -3 -1  1 -1 -6 
// -1 -2 -3 -4 -2  0 -2 -4 -2  1  2 -2  6  0 -3 -2 -1 -2 -2  1 -3 -2 -1 -6 
// -3 -4 -4 -4 -3 -4 -4 -4 -2 -1  0 -4  0  6 -4 -3 -2  0  3 -1 -4 -4 -2 -6 
// -1 -2 -3 -2 -4 -2 -2 -3 -3 -4 -3 -1 -3 -4  8 -1 -2 -5 -4 -3 -2 -2 -2 -6 
//  1 -1  0 -1 -2  0  0 -1 -1 -3 -3 -1 -2 -3 -1  5  1 -4 -2 -2  0  0 -1 -6 
//  0 -1  0 -1 -1 -1 -1 -2 -2 -1 -2 -1 -1 -2 -2  1  5 -4 -2  0 -1 -1 -1 -6 
// -3 -4 -4 -6 -3 -3 -4 -4 -3 -3 -2 -4 -2  0 -5 -4 -4 11  2 -3 -5 -4 -3 -6 
// -2 -3 -3 -4 -3 -2 -3 -4  2 -2 -2 -3 -2  3 -4 -2 -2  2  7 -2 -3 -3 -2 -6 
//  0 -3 -4 -4 -1 -3 -3 -4 -4  3  1 -3  1 -1 -3 -2  0 -3 -2  4 -4 -3 -1 -6 
// -2 -2  4  4 -4  0  1 -1 -1 -4 -4 -1 -3 -4 -2  0 -1 -5 -3 -4  4  0 -2 -6 
// -1  0  0  1 -4  3  4 -3  0 -4 -3  1 -2 -4 -2  0 -1 -4 -3 -3  0  4 -1 -6 
// -1 -1 -1 -2 -3 -1 -1 -2 -2 -2 -2 -1 -1 -2 -2 -1 -1 -3 -2 -1 -2 -1 -1 -6 
// -6 -6 -6 -6 -6 -6 -6 -6 -6 -6 -6 -6 -6 -6 -6 -6 -6 -6 -6 -6 -6 -6 -6  1 

// #  Matrix made by matblas from blosum45.iij
// #  * column uses minimum score
// #  BLOSUM Clustered Scoring Matrix in 1/3 Bit Units
// #  Blocks Database = /data/blocks_5.0/blocks.dat
// #  Cluster Percentage: >= 45
// #  Entropy =   0.3795, Expected =  -0.2789
//  A  R  N  D  C  Q  E  G  H  I  L  K  M  F  P  S  T  W  Y  V  B  Z  X  *
//  5 -2 -1 -2 -1 -1 -1  0 -2 -1 -1 -1 -1 -2 -1  1  0 -2 -2  0 -1 -1  0 -5 
// -2  7  0 -1 -3  1  0 -2  0 -3 -2  3 -1 -2 -2 -1 -1 -2 -1 -2 -1  0 -1 -5 
// -1  0  6  2 -2  0  0  0  1 -2 -3  0 -2 -2 -2  1  0 -4 -2 -3  4  0 -1 -5 
// -2 -1  2  7 -3  0  2 -1  0 -4 -3  0 -3 -4 -1  0 -1 -4 -2 -3  5  1 -1 -5 
// -1 -3 -2 -3 12 -3 -3 -3 -3 -3 -2 -3 -2 -2 -4 -1 -1 -5 -3 -1 -2 -3 -2 -5 
// -1  1  0  0 -3  6  2 -2  1 -2 -2  1  0 -4 -1  0 -1 -2 -1 -3  0  4 -1 -5 
// -1  0  0  2 -3  2  6 -2  0 -3 -2  1 -2 -3  0  0 -1 -3 -2 -3  1  4 -1 -5 
//  0 -2  0 -1 -3 -2 -2  7 -2 -4 -3 -2 -2 -3 -2  0 -2 -2 -3 -3 -1 -2 -1 -5 
// -2  0  1  0 -3  1  0 -2 10 -3 -2 -1  0 -2 -2 -1 -2 -3  2 -3  0  0 -1 -5 
// -1 -3 -2 -4 -3 -2 -3 -4 -3  5  2 -3  2  0 -2 -2 -1 -2  0  3 -3 -3 -1 -5 
// -1 -2 -3 -3 -2 -2 -2 -3 -2  2  5 -3  2  1 -3 -3 -1 -2  0  1 -3 -2 -1 -5 
// -1  3  0  0 -3  1  1 -2 -1 -3 -3  5 -1 -3 -1 -1 -1 -2 -1 -2  0  1 -1 -5 
// -1 -1 -2 -3 -2  0 -2 -2  0  2  2 -1  6  0 -2 -2 -1 -2  0  1 -2 -1 -1 -5 
// -2 -2 -2 -4 -2 -4 -3 -3 -2  0  1 -3  0  8 -3 -2 -1  1  3  0 -3 -3 -1 -5 
// -1 -2 -2 -1 -4 -1  0 -2 -2 -2 -3 -1 -2 -3  9 -1 -1 -3 -3 -3 -2 -1 -1 -5 
//  1 -1  1  0 -1  0  0  0 -1 -2 -3 -1 -2 -2 -1  4  2 -4 -2 -1  0  0  0 -5 
//  0 -1  0 -1 -1 -1 -1 -2 -2 -1 -1 -1 -1 -1 -1  2  5 -3 -1  0  0 -1  0 -5 
// -2 -2 -4 -4 -5 -2 -3 -2 -3 -2 -2 -2 -2  1 -3 -4 -3 15  3 -3 -4 -2 -2 -5 
// -2 -1 -2 -2 -3 -1 -2 -3  2  0  0 -1  0  3 -3 -2 -1  3  8 -1 -2 -2 -1 -5 
//  0 -2 -3 -3 -1 -3 -3 -3 -3  3  1 -2  1  0 -3 -1  0 -3 -1  5 -3 -3 -1 -5 
// -1 -1  4  5 -2  0  1 -1  0 -3 -3  0 -2 -3 -2  0  0 -4 -2 -3  4  0 -1 -5 
// -1  0  0  1 -3  4  4 -2  0 -3 -2  1 -1 -3 -1  0 -1 -2 -2 -3  0  4 -1 -5 
//  0 -1 -1 -1 -2 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1  0  0 -2 -1 -1 -1 -1 -1 -5 
// -5 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5 -5  1 

int SubstitutionMatrix::populate_matrix() {
  string aas = "ARNDCQEGHILKMFPSTWYVBZX";
  string A; string R; string N; string D; string C; string Q; string E; string G;
  string H; string I; string L; string K; string M; string F; string P; string S;
  string T; string W; string Y; string V; string B; string Z; string X;

  if (matrix == 80) {
    // BLOSUM80
    A   = " 5 -2 -2 -2 -1 -1 -1  0 -2 -2 -2 -1 -1 -3 -1  1  0 -3 -2  0 -2 -1 -1";
    R   = "-2  6 -1 -2 -4  1 -1 -3  0 -3 -3  2 -2 -4 -2 -1 -1 -4 -3 -3 -2  0 -1";
    N   = "-2 -1  6  1 -3  0 -1 -1  0 -4 -4  0 -3 -4 -3  0  0 -4 -3 -4  4  0 -1";
    D   = "-2 -2  1  6 -4 -1  1 -2 -2 -4 -5 -1 -4 -4 -2 -1 -1 -6 -4 -4  4  1 -2";
    C   = "-1 -4 -3 -4  9 -4 -5 -4 -4 -2 -2 -4 -2 -3 -4 -2 -1 -3 -3 -1 -4 -4 -3";
    Q   = "-1  1  0 -1 -4  6  2 -2  1 -3 -3  1  0 -4 -2  0 -1 -3 -2 -3  0  3 -1";
    E   = "-1 -1 -1  1 -5  2  6 -3  0 -4 -4  1 -2 -4 -2  0 -1 -4 -3 -3  1  4 -1";
    G   = " 0 -3 -1 -2 -4 -2 -3  6 -3 -5 -4 -2 -4 -4 -3 -1 -2 -4 -4 -4 -1 -3 -2";
    H   = "-2  0  0 -2 -4  1  0 -3  8 -4 -3 -1 -2 -2 -3 -1 -2 -3  2 -4 -1  0 -2";
    I   = "-2 -3 -4 -4 -2 -3 -4 -5 -4  5  1 -3  1 -1 -4 -3 -1 -3 -2  3 -4 -4 -2";
    L   = "-2 -3 -4 -5 -2 -3 -4 -4 -3  1  4 -3  2  0 -3 -3 -2 -2 -2  1 -4 -3 -2";
    K   = "-1  2  0 -1 -4  1  1 -2 -1 -3 -3  5 -2 -4 -1 -1 -1 -4 -3 -3 -1  1 -1";
    M   = "-1 -2 -3 -4 -2  0 -2 -4 -2  1  2 -2  6  0 -3 -2 -1 -2 -2  1 -3 -2 -1";
    F   = "-3 -4 -4 -4 -3 -4 -4 -4 -2 -1  0 -4  0  6 -4 -3 -2  0  3 -1 -4 -4 -2";
    P   = "-1 -2 -3 -2 -4 -2 -2 -3 -3 -4 -3 -1 -3 -4  8 -1 -2 -5 -4 -3 -2 -2 -2";
    S   = " 1 -1  0 -1 -2  0  0 -1 -1 -3 -3 -1 -2 -3 -1  5  1 -4 -2 -2  0  0 -1";
    T   = " 0 -1  0 -1 -1 -1 -1 -2 -2 -1 -2 -1 -1 -2 -2  1  5 -4 -2  0 -1 -1 -1";
    W   = "-3 -4 -4 -6 -3 -3 -4 -4 -3 -3 -2 -4 -2  0 -5 -4 -4 11  2 -3 -5 -4 -3";
    Y   = "-2 -3 -3 -4 -3 -2 -3 -4  2 -2 -2 -3 -2  3 -4 -2 -2  2  7 -2 -3 -3 -2";
    V   = " 0 -3 -4 -4 -1 -3 -3 -4 -4  3  1 -3  1 -1 -3 -2  0 -3 -2  4 -4 -3 -1";
    B   = "-2 -2  4  4 -4  0  1 -1 -1 -4 -4 -1 -3 -4 -2  0 -1 -5 -3 -4  4  0 -2";
    Z   = "-1  0  0  1 -4  3  4 -3  0 -4 -3  1 -2 -4 -2  0 -1 -4 -3 -3  0  4 -1";
    X   = "-1 -1 -1 -2 -3 -1 -1 -2 -2 -2 -2 -1 -1 -2 -2 -1 -1 -3 -2 -1 -2 -1 -1";
  }
  else if (matrix == 45) {
    // BLOSUM45
    A   = " 5 -2 -1 -2 -1 -1 -1  0 -2 -1 -1 -1 -1 -2 -1  1  0 -2 -2  0 -1 -1  0";
    R   = "-2  7  0 -1 -3  1  0 -2  0 -3 -2  3 -1 -2 -2 -1 -1 -2 -1 -2 -1  0 -1";
    N   = "-1  0  6  2 -2  0  0  0  1 -2 -3  0 -2 -2 -2  1  0 -4 -2 -3  4  0 -1";
    D   = "-2 -1  2  7 -3  0  2 -1  0 -4 -3  0 -3 -4 -1  0 -1 -4 -2 -3  5  1 -1";
    C   = "-1 -3 -2 -3 12 -3 -3 -3 -3 -3 -2 -3 -2 -2 -4 -1 -1 -5 -3 -1 -2 -3 -2";
    Q   = "-1  1  0  0 -3  6  2 -2  1 -2 -2  1  0 -4 -1  0 -1 -2 -1 -3  0  4 -1";
    E   = "-1  0  0  2 -3  2  6 -2  0 -3 -2  1 -2 -3  0  0 -1 -3 -2 -3  1  4 -1";
    G   = " 0 -2  0 -1 -3 -2 -2  7 -2 -4 -3 -2 -2 -3 -2  0 -2 -2 -3 -3 -1 -2 -1";
    H   = "-2  0  1  0 -3  1  0 -2 10 -3 -2 -1  0 -2 -2 -1 -2 -3  2 -3  0  0 -1";
    I   = "-1 -3 -2 -4 -3 -2 -3 -4 -3  5  2 -3  2  0 -2 -2 -1 -2  0  3 -3 -3 -1";
    L   = "-1 -2 -3 -3 -2 -2 -2 -3 -2  2  5 -3  2  1 -3 -3 -1 -2  0  1 -3 -2 -1";
    K   = "-1  3  0  0 -3  1  1 -2 -1 -3 -3  5 -1 -3 -1 -1 -1 -2 -1 -2  0  1 -1";
    M   = "-1 -1 -2 -3 -2  0 -2 -2  0  2  2 -1  6  0 -2 -2 -1 -2  0  1 -2 -1 -1";
    F   = "-2 -2 -2 -4 -2 -4 -3 -3 -2  0  1 -3  0  8 -3 -2 -1  1  3  0 -3 -3 -1";
    P   = "-1 -2 -2 -1 -4 -1  0 -2 -2 -2 -3 -1 -2 -3  9 -1 -1 -3 -3 -3 -2 -1 -1";
    S   = " 1 -1  1  0 -1  0  0  0 -1 -2 -3 -1 -2 -2 -1  4  2 -4 -2 -1  0  0  0";
    T   = " 0 -1  0 -1 -1 -1 -1 -2 -2 -1 -1 -1 -1 -1 -1  2  5 -3 -1  0  0 -1  0";
    W   = "-2 -2 -4 -4 -5 -2 -3 -2 -3 -2 -2 -2 -2  1 -3 -4 -3 15  3 -3 -4 -2 -2";
    Y   = "-2 -1 -2 -2 -3 -1 -2 -3  2  0  0 -1  0  3 -3 -2 -1  3  8 -1 -2 -2 -1";
    V   = " 0 -2 -3 -3 -1 -3 -3 -3 -3  3  1 -2  1  0 -3 -1  0 -3 -1  5 -3 -3 -1";
    B   = "-1 -1  4  5 -2  0  1 -1  0 -3 -3  0 -2 -3 -2  0  0 -4 -2 -3  4  0 -1";
    Z   = "-1  0  0  1 -3  4  4 -2  0 -3 -2  1 -1 -3 -1  0 -1 -2 -2 -3  0  4 -1";
    X   = " 0 -1 -1 -1 -2 -1 -1 -1 -1 -1 -1 -1 -1 -1 -1  0  0 -2 -1 -1 -1 -1 -1"; 
  }
  else {
    // BLOSUM62 default
    A   = " 4 -1 -2 -2  0 -1 -1  0 -2 -1 -1 -1 -1 -2 -1  1  0 -3 -2  0 -2 -1  0";
    R   = "-1  5  0 -2 -3  1  0 -2  0 -3 -2  2 -1 -3 -2 -1 -1 -3 -2 -3 -1  0 -1"; 
    N   = "-2  0  6  1 -3  0  0  0  1 -3 -3  0 -2 -3 -2  1  0 -4 -2 -3  3  0 -1";
    D   = "-2 -2  1  6 -3  0  2 -1 -1 -3 -4 -1 -3 -3 -1  0 -1 -4 -3 -3  4  1 -1"; 
    C   = " 0 -3 -3 -3  9 -3 -4 -3 -3 -1 -1 -3 -1 -2 -3 -1 -1 -2 -2 -1 -3 -3 -2"; 
    Q   = "-1  1  0  0 -3  5  2 -2  0 -3 -2  1  0 -3 -1  0 -1 -2 -1 -2  0  3 -1"; 
    E   = "-1  0  0  2 -4  2  5 -2  0 -3 -3  1 -2 -3 -1  0 -1 -3 -2 -2  1  4 -1"; 
    G   = " 0 -2  0 -1 -3 -2 -2  6 -2 -4 -4 -2 -3 -3 -2  0 -2 -2 -3 -3 -1 -2 -1"; 
    H   = "-2  0  1 -1 -3  0  0 -2  8 -3 -3 -1 -2 -1 -2 -1 -2 -2  2 -3  0  0 -1"; 
    I   = "-1 -3 -3 -3 -1 -3 -3 -4 -3  4  2 -3  1  0 -3 -2 -1 -3 -1  3 -3 -3 -1"; 
    L   = "-1 -2 -3 -4 -1 -2 -3 -4 -3  2  4 -2  2  0 -3 -2 -1 -2 -1  1 -4 -3 -1"; 
    K   = "-1  2  0 -1 -3  1  1 -2 -1 -3 -2  5 -1 -3 -1  0 -1 -3 -2 -2  0  1 -1"; 
    M   = "-1 -1 -2 -3 -1  0 -2 -3 -2  1  2 -1  5  0 -2 -1 -1 -1 -1  1 -3 -1 -1"; 
    F   = "-2 -3 -3 -3 -2 -3 -3 -3 -1  0  0 -3  0  6 -4 -2 -2  1  3 -1 -3 -3 -1"; 
    P   = "-1 -2 -2 -1 -3 -1 -1 -2 -2 -3 -3 -1 -2 -4  7 -1 -1 -4 -3 -2 -2 -1 -2"; 
    S   = " 1 -1  1  0 -1  0  0  0 -1 -2 -2  0 -1 -2 -1  4  1 -3 -2 -2  0  0  0"; 
    T   = " 0 -1  0 -1 -1 -1 -1 -2 -2 -1 -1 -1 -1 -2 -1  1  5 -2 -2  0 -1 -1  0"; 
    W   = "-3 -3 -4 -4 -2 -2 -3 -2 -2 -3 -2 -3 -1  1 -4 -3 -2 11  2 -3 -4 -3 -2"; 
    Y   = "-2 -2 -2 -3 -2 -1 -2 -3  2 -1 -1 -2 -1  3 -3 -2 -2  2  7 -1 -3 -2 -1"; 
    V   = " 0 -3 -3 -3 -1 -2 -2 -3 -3  3  1 -2  1 -1 -2 -2  0 -3 -1  4 -3 -2 -1"; 
    B   = "-2 -1  3  4 -3  0  1 -1  0 -3 -4  0 -3 -3 -2  0 -1 -4 -3 -3  4  1 -1"; 
    Z   = "-1  0  0  1 -3  3  4 -2  0 -3 -3  1 -1 -3 -1  0 -1 -3 -2 -2  1  4 -1"; 
    X   = " 0 -1 -1 -1 -2 -1 -1 -1 -1 -1 -1 -1 -1 -1 -2  0  0 -2 -1 -1 -1 -1 -1";
  }
 
  map<char,string,less<char> > prof;

  prof['A'] = A; prof['R'] = R; prof['N'] = N; prof['D'] = D;
  prof['C'] = C; prof['Q'] = Q; prof['E'] = E; prof['G'] = G;
  prof['H'] = H; prof['I'] = I; prof['L'] = L; prof['K'] = K;
  prof['M'] = M; prof['F'] = F; prof['P'] = P; prof['S'] = S;
  prof['T'] = T; prof['W'] = W; prof['Y'] = Y; prof['V'] = V;
  prof['B'] = B; prof['Z'] = Z; prof['X'] = X;

  string temp;
  
  for (string::iterator i = aas.begin(); i < aas.end(); ++i) {
    if (*i != ' ') {
      int c = 0;   
      for ( string::iterator j = prof[*i].begin(); j < prof[*i].end(); ++j) {
	if (*j != ' ') {
	  temp += *j;
	} else {
	  if (temp == "") {
	    temp.erase(temp.begin(),temp.end());     
	  } else {
	    insertSubs( *i, aas[c], atoi(temp.c_str()));
	    temp.erase(temp.begin(),temp.end());
	    c++;
	  }
	}
      } 
      insertSubs( *i, aas[c],  atoi(temp.c_str()));
      temp.erase(temp.begin(),temp.end());
    }
  }
}


void SubstitutionMatrix::insertSubs( int r, int c, int value ) {
  // Insert a value at a matrix coordinate
  if (r < c) {  // the matrix is symmetrical so reduce access to one half of it
    int temp = r;   r = c;  c = temp;
  }
  // If both indices are within limits allow access to the coordinate
  if  (0 <= r && r < ALPHABETSIZE && 0 <= c && c < ALPHABETSIZE) 
    SM[r][c] = value;
}

void SubstitutionMatrix::insertSubs( char row, char col, int value ) {
  int r = getPos(row);
  int c = getPos(col);  
  // The matrix coordinates can be passed as chars in this case the getPos
  // function is used to provide an int index
  if (r < c) {  // the matrix is symmetrical so reduce access to one half of it
    int temp = r;   r = c;  c = temp;
    char tmp = row; row = col; col = tmp;
  }
  if  (0 <= r && r < ALPHABETSIZE && 0 <= c && c < ALPHABETSIZE) {
    SM[r][c] = value;
    //printf ("%c %2d %c %2d %2d\n",row,r,col,c,value);
  }
}


void SubstitutionMatrix::printSM() {  
  // A basic function to print out the matrix
  cout << "  ";
  for (int i=0;i<26;i++)
    printf ("%2c ",getChar(i));
  cout << endl;
  
  for (int i=0;i<26;i++) {
    printf("%-2c",getChar(i));
    for (int j=0;j<26;j++) 
      printf("%2d ",matrixLookup(i,j));
    cout << endl;
  }
}


int SubstitutionMatrix::matrixLookup( int r, int c ) {
  // Four overloaded functions to allow access to be requested using
  // any combination of int or char coordinates
  if (r < c) {  // the matrix is symmetrical so reduce queries to one half of it
    int temp = r;   r = c;  c = temp;
  }

  if (0 <= r && r < ALPHABETSIZE && 0 <= c && c < ALPHABETSIZE) 
      return SM[r][c];
  else
    return 999;
}


int SubstitutionMatrix::matrixLookup( int r, char col ) {
  int c = getPos(col);

  if (r < c) {  // the matrix is symmetrical so reduce queries to one half of it
    int temp = r;   r = c;  c = temp;
  }

  if (0 <= r && r < ALPHABETSIZE && 0 <= c && c < ALPHABETSIZE) 
      return SM[r][c];
  else
    return 999;
}

int SubstitutionMatrix::matrixLookup( char row, int c ) {
  int r = getPos(row);

  if (r < c) {  // the matrix is symmetrical so reduce queries to one half of it
    int temp = r;   r = c;  c = temp;
  }

  if (0 <= r && r < ALPHABETSIZE && 0 <= c && c < ALPHABETSIZE) 
      return SM[r][c];
  else
    return 999;
}



int SubstitutionMatrix::matrixLookup( char row, char col ) {
  int r = getPos(row);
  int c = getPos(col);

  if (r < c) {  // the matrix is symmetrical so reduce queries to one half of it
    int temp = r;   r = c;  c = temp;
  }

  if ( 0 <= r && r < ALPHABETSIZE && 0 <= c && c < ALPHABETSIZE) 
      return SM[r][c];
  else
    return 999;
}


