#ifndef SUBSTITUTIONMATRIX_H
#define SUBSTITUTIONMATRIX_H

#include <iostream>
#include <cstdio>
#include <string>
#include <map>
using namespace std;

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

/***************************************************************************/
// The Substitution Matrix, literally holds the data points for the n*n
// matrix of amino acid substitution scores.

class SubstitutionMatrix {
  
private:
  int * * SM;
  static const int ALPHABETSIZE = 26; // alphabet +1
  const int matrix;
  int populate_matrix();
  void insertSubs(int r, int c, int value);
  void insertSubs(char row, char col, int value);
  inline int  getPos (char c) { return ( int(toupper(c)) - int('A') ); }
  inline char getChar (int x) { return ( char( x + int('A') ) ); } 
public:
  SubstitutionMatrix(int ma);
  ~SubstitutionMatrix();

  int  matrixLookup(int row, int col);
  int  matrixLookup(int row, char col);
  int  matrixLookup(char row, int col);
  int  matrixLookup(char row, char col);
  void printSM();
};

#endif    
