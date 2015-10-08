#ifndef SEQUENCE_DATABASE_H
#define SEQUENCE_DATABASE_H

#include <iostream>
#include <string>
#include <fstream>
#include <cstdlib>
#include <cctype>
#include <iomanip>
#include <vector>
#include <algorithm>
using namespace std;


#include <time.h>// needed for time(NULL) 

#include "mystring.h"
#include "Score.h"

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

class Sequence {
  typedef Sequence * Ptr_Sequence;
  typedef ScoreList * Ptr_ScoreList;
  typedef FpScore * Ptr_FpScore;
  
public:
  Sequence(mystring inf, mystring nm, mystring res) 
    : info(inf), name(nm), residue_list(res), next(0) 
    {    
      fppoint = scorelist.Schead;   
      srand(time(NULL));
      for (int i= 0; i < MAXPAD; i++)  { padding += "#"; }
    }
  ~Sequence(){ }
  //    friend ostream & operator << (ostream & os, const Sequence & S);
  void PrintSequence(){ cout << name << endl << info << endl << residue_list << endl; }
  
  void Shuffle();

  const mystring info;
  
  mystring Getresidue_list() { 
    mystring destin;
    destin += padding; destin += residue_list; destin += padding;
    return destin;
  }
  
  const mystring name;
  
  Ptr_Sequence next;
  ScoreList scorelist;
  Ptr_FpScore fppoint;
  
private: 
  //for ease of access these are moved out of the private declarations
  
  //const mystring info;
  //const mystring name;
  static const int MAXPAD = 35;
  mystring padding;
  mystring residue_list;
  struct random{ 
    int operator()(int m){ return rand()%m; }; 
  };
  random rnd;
};



class SequenceDatabase  {

typedef Sequence * Ptr_Sequence;

private:
  int GetStrippedData(mystring & buffer, mystring & name ,
     char a ,char b, int offset); 
  int GetStrippedData(mystring & buffer, mystring & name ,
     int offset); 
  //returns status of get
  int GetData(mystring & buffer, mystring & name ,char a ,
    char b, int offset); 
  int GetData(mystring & buffer, mystring & name ,char a ,
    int offset); 
  //returns status of get
  void ERROR(char * s= "", char * s1="" ) 
    { cerr << s << ' ' << s1 <<'\n';  exit(1); }
  
  void AddSequence(mystring in, mystring nm, mystring res);
  
public:
  SequenceDatabase():head(0){};
  ~SequenceDatabase();
  int ReadSequence(mystring file_name);
  int ReadFasta(mystring file_name);

  void PrintSequences();
  Ptr_Sequence head;
  
};

#endif
