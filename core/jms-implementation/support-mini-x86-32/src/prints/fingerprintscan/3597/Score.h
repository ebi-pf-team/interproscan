#ifndef SCORE_H
#define SCORE_H

#include <iostream>
#include <iomanip>
#include <cstdlib>
using namespace std;

#include "mystring.h"
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



/*****************************************************************************
Match Score Elements
*****************************************************************************/

class MatchScore {
  typedef MatchScore * ptr_MatchScore;
public:
  MatchScore()
    : score(0), idscore(0.0), position(0), pvalue(0), next(0), sequence(""){}
  MatchScore(int sc, int pos, mystring seq)
    :score(sc), idscore(0.0), position(pos), pvalue(0), next(0), sequence(seq) {}
  MatchScore(int sc, double pv, int pos, mystring seq)
    :score(sc), idscore(0.0), position(pos), pvalue(pv), next(0), sequence(seq) {}
  MatchScore(int sc, float idsc, double pv, int pos, mystring seq)
    :score(sc), idscore(idsc), position(pos), pvalue(pv), next(0), sequence(seq) {}
  MatchScore(const MatchScore& M)
    :score(M.score), position(M.position), pvalue(M.pvalue),next(0), sequence(M.sequence) {}

  ~MatchScore() {};
  
  MatchScore& operator= (const MatchScore& M) {
    score = M.score, position = M.position, 
      sequence = M.sequence, pvalue = M.pvalue;
    return *this;
  }  
  int operator== (const MatchScore& M) {
    return ( (score == M.score) &&  (position == M.position) && 
      (sequence == M.sequence) && (pvalue == M.pvalue));}
   
  // Overload of > and < ops for the sort algo 
  // position is the variable tested rather than score
  int operator> (const MatchScore& M) {
    return (position > M.position);}
  int operator< (const MatchScore& M) {
    return (position < M.position);}

  int Getscore () {return int(score);}//dont know why it is necessary?
  float Getidscore() {return idscore;}
  double Getpvalue() {return pvalue;}
  //double MatchScore::GetevalN()  {return pvalue;}
  //double MatchScore::GetevalD()  {return pvalue;}
  int   Getpos   () {return position;}
  mystring Getseq() {return sequence;}

  int ThisIsGTscore(float i) { return ( i > score);}
  int ThisIsGTpos  (int i  ) { return ( i > position);}
  double ThisIsGTpvalue(double i) { return (i > pvalue); }

  void PrintMatchScore() {
        cout << "Mt:" << "Score " << setprecision(5) << score << "  "
             << "Pos   " << setprecision(4) << position << "  "
             << "Seq   " << sequence;
  }

  friend ostream & operator << (ostream & os, const MatchScore & M){
    cout << setprecision(5) << M.score << "  "
  << setprecision(9) << M.pvalue << "  "
  << setprecision(4) << M.position << "  "
  << M.sequence;
    return os;
  }
  // Pointers one to the next Match
  ptr_MatchScore next;
private:
  float score;  // for some wierd reason all these vars have to be in this order
  int position;
  mystring sequence; 
  double pvalue;
  float idscore;
};


/*****************************************************************************
MotifScoreList
*****************************************************************************/

class MotifScore {
  typedef MatchScore * ptr_MatchScore;
  typedef MotifScore * ptr_MotifScore;
public:
  MotifScore(mystring motname, int lstart, int hstart, 
			 int linter, int hinter,
			 int length, int num,
			 float ave, float stdev, mystring cons);
  ~MotifScore();
  void AddMS (int sc, int pos, mystring seq);
  void AddMS (int sc, double pval, int pos, mystring seq);
  void AddMS (int sc, float idsc, double pval, int pos, mystring seq);
  friend ostream & operator << (ostream & os, const MotifScore & M);  
  void TraverseMS();
  ptr_MatchScore Mthead;
  ptr_MotifScore next;

  mystring Getname()    { return motifname;};
  mystring Getcons()    { return consensus;};
  int Getlength()       { return length;}
  int Getlstart()       { return lstart;}
  int Gethstart()       { return hstart;}
  int Getlinter()       { return linter;}
  int Gethinter()       { return hinter;}
  int Getmotnum()       { return motnum;}
  float Getavescore()   { return AveSelfScore;}
  
private:
  // These data values are invariant for each motif
  mystring motifname;
  mystring consensus;
  const int length;
  const int lstart;
  const int hstart;
  const int linter;
  const int hinter;
  const int motnum;
  const float AveSelfScore;
  
  // The data values that vary are the score, position, and sequence of
  // each match these are stored for every occurence of a match with a score
  // above a certain threshold in a linked list of Motif Score elements
};


/*****************************************************************************
Score Fingerprint List Item
each Fingerprint will create an instance of this class this will represent
the Score of the Fingerprint, which is represented in turn by the number
of motifs that SCORE
each fingerprint Score object will maintain a linked list of Motif score
objects, thus allowing a query of the number of motifs that MATCH
*****************************************************************************/

class FpScore {
  typedef FpScore * ptr_FpScore;
  typedef MotifScore * ptr_MotifScore;
public:
  FpScore(mystring fpn, int nm, mystring ac, mystring in);
  ~FpScore();
  
  void PrintFpScore();
  void DeleteMotifs();
  void TraverseMt();
  mystring Getfpname() {return fpname;}
  MotifScore * AddMotif(mystring motname, int lstart, int hstart,
     int linter, int hinter, int length, int num,
     float ave, float stdev, mystring consensus);
  int Getnummotifs() {return nummotifs;}
  mystring Getacc() {return accession;}
  mystring Getinfo() {return info;}
  ptr_FpScore next;
  ptr_MotifScore Fphead;
  
private:
  mystring fpname;
  const int nummotifs;
  mystring accession;
  mystring info;
};


/*****************************************************************************
Score Object
This Object maintains a linked list of Fingerprint Score objects
*****************************************************************************/


class ScoreList {
  typedef FpScore * ptr_FpScore;
public:
  ScoreList();
  ~ScoreList();
  ptr_FpScore Schead;
  ptr_FpScore AddFp (mystring fpname, int numots, 
    mystring access, mystring inf);
  friend ostream & operator << (ostream & os, const ScoreList & Sc);
  void DeleteFps();
  void CleanUp();
  void Traverse();
private:
  
};

#endif
