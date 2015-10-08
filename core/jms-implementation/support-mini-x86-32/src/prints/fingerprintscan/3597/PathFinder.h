#ifndef PATHFINDER_H
#define PATHFINDER_H

#include <iostream>
#include <iomanip>
#include <cstdlib> 
#include <algorithm>
#include <cmath>
#include <vector>
using namespace std;


#include "Score.h"
#include "Sequence_database.h"

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
MotifxMatch

This object represents the linkage between a MotifScore object and MatchScore
This is to enable the PathFinder algorithm to use a list of sorted (on pos)
Matches and also to enable each match to be linked back to its corresponding
motif.
The constructor can be called empty (to generate an array of MxM objects), or
with a pointer to a MotifScore and a MatchScore obj.
The data is simple, two pointers to the objects and a next for a linked list.

Get and Set, Match and Motif member functions exist to enable set and retrieval
of the pointers, all variables are also public, thus allowing next to be set
using the normal p = p->next notation for linked list traversal and creation.
*****************************************************************************/

class MotifxMatch {
  typedef MatchScore * ptrMCH;
  typedef MotifScore * ptrMOT;
  typedef MotifxMatch* ptrMxM;

public:
  MotifxMatch(): next(0), match(0), motif(0) {}
  MotifxMatch(ptrMOT mot, ptrMCH mch)
    : next(0), match(mch), motif(mot) {}
  ~MotifxMatch() {}

  ptrMCH GetMatch() {return match;}
  ptrMOT GetMotif() {return motif;}
  void SetMatch(ptrMCH mch) {match = mch;}
  void SetMotif(ptrMOT mot) {motif = mot;}

  // Now asking whether a MxM is greater (or less) than
  // another should result in the Match being queried in the
  // same manner, based on position
  // so to ask this question gives an answer based on position
  // the objects are stored as pointers hence dereferenced
  int operator> (const MotifxMatch& M) { return ((*match) > (*M.match));}
  int operator< (const MotifxMatch& M) { return ((*match) < (*M.match));}
  
  friend ostream & operator << (ostream & os, const MotifxMatch & M) {
    cout  << *(M.motif) << *(M.match) << endl;
    return os;
  }

public:
  ptrMxM next;
  ptrMCH match;
  ptrMOT motif;
};


// ************************************************************************
// PathEl
//
// Another simple object, required to represent each element in a path. A path
// being a route through the data conforming to the rules i.e. a motif 
// following another motif must be greater in number etc.
//
// In the data 1,2a,2b,4a,3,4b
//
// a representative path is 1-2a-3-4b, other paths being; 
// (inc. the generation of the paths)
//
// 1  | 1
// 2a | 1-2a, 1
// 2b | 1-2a, 1-2b, 1
// 4a | 1-2a, 1-2b, 1, 1-2a-4a, 1-2b-4a, 1-4a
// 3  | 1-2a, 1-2b, 1, 1-2a-4a, 1-2b-4a, 1-4a, 1-2a-3, 1-2b-3, 1-3
// 4b | 1-2a, 1-2b, 1, 1-2a-4a, 1-2b-4a, 1-4a, 1-2a-3, 1-2b-3, 1-3, 1-2a-4b, 
//      1-2b-4b, 1-4b, 1-2a-3-4b, 1-2b-3-ab, 1-3-4b
//
// The paths found are seen on the last line, each Path is represented as
// a number of PathEl's separated by -, and a PathEl is represented by a number
// corresponding to the sorted list of Matches
//
// Each PathEl has only two pointers, next and mxm, the former being the usual
// self pointer used for linked lists, and mxm allowing it to point to a
// MotifXMatch object. A path is thus a linked list of pointers to mxm's.
// Both are public
// 
// Constructor is called empty, setting next and mxm to Null.
// called with mxm pointer, leaving next at null, this is the usual -  
// creation followed by assignment of next during addition to linked list
// procedure - and called with both params!
// a copy constructor exists which leaves next at null and assigns mxm as copy
// 
// Functions: an overloaded assignment '=' does the usual copy,  
// Get and set functions were replaced with public access to the variables
//
// **************************************************************************


class PathEl {
  typedef PathEl      * ptrPathEl;
  typedef MotifxMatch * ptrMxM;
  
public:     
  PathEl() : next(0), mxm(0){}
  PathEl( ptrMxM m ) // Only mxm supplied, next is set later
    : next(0), mxm(m) {}
  PathEl( ptrPathEl n , ptrMxM m) // Match and pointer to next supplied
    : next(n), mxm(m) {}
  // The copy constructor doesnt set the next pointer, an actual identical
  // copy of the object can be obtained only by assignment!
  
  PathEl( const PathEl& P):next(0), mxm(P.mxm) {}
  
  PathEl& operator= (const PathEl& P )     
    { next = P.next, mxm = P.mxm; 
      return *this;}
  
  ~PathEl () {}
  
  friend ostream & operator << (ostream & os, const PathEl & PE) {
    cout << *(PE.mxm);
    return os;
  }

  // these are here to complete the interface not necessary unless I decide
  // to make the public variables private again!
  //PathEl::SetNext ( ptrPathEl n ){ next  = n;}
  //PathEl::SetMatch( ptrMxM m ){ mxm = m;}
  //ptrPathEl PathEl::GetNext ()   { return next; }
  //ptrMxM PathEl::GetMxM()   { return mxm;}
  
public: // The point is that these used to be private now they are public
  
  ptrPathEl next;
  ptrMxM mxm;
  
};  


// *****************************************************************************
//
// Path
//
// A path is linked list of PathEl objects. 
// An empty constructor creates an empty Path object
// A constructor called with just a pointer to mxm, creates a new PathEl obj
// and makes head point at it.
// A copy constructor, creates a copy of the path including a traversal of its
// own linked list and a copy of each of the elements. Addpath is used because
// addition sorts out the linked list structure
// Two public variables, next and head, are the linked list variables
// A private variable runningTotal is summed as each PathEl is added
// 
// *****************************************************************************

class DatabaseValues {
  friend class PathFinderList;
public:
  void show() { 
    cout << "SEQS: " << DATABASESEQS << endl
	 << "LENGTH: " << DATABASELENGTH << endl;
    if (MethodIs_pD) {
      cout << "p*D" << endl;
    } else {
      cout << "p*N/n" << endl; 
    }
  }
  static double getSeqs() { return DATABASESEQS;   }
  static double getLen () { return DATABASELENGTH; }
  static bool getMethod() { return MethodIs_pD; }
private:
  void set(double i, double j) { DATABASESEQS=i; DATABASELENGTH=j; }
  static double DATABASESEQS;
  static double DATABASELENGTH;
  static bool MethodIs_pD;

  static void setSeqs(double sqs) { DATABASESEQS   = sqs; }
  static void setLen (double len) { DATABASELENGTH = len; }
  static void setMethod_pD() { MethodIs_pD = 1; }
  static void setMethod_pNn() { MethodIs_pD = 0; }

};


class Path {

  typedef PathEl * ptrPathEl;
  typedef Path   * ptrPath;
  typedef MotifxMatch * ptrMxM;
  typedef DatabaseValues * pDBV;


public:
    
  
  Path(): next(0), head(0), runningTotal(0), 
    lengthFudgeScore(0), tail(0) {}
  Path( ptrMxM mxm, pDBV pdbv ): next(0), head(0), 
      runningTotal(0), tail(0), lengthFudgeScore(0), pDBval(pdbv)
    { head = new PathEl(mxm); tail = head; }
  Path( const Path& P );
  ~Path();
  void AddPathEl( ptrMxM mxm );
  float GetrunningTotal() {return runningTotal;}  
  float GetlengthFudge() {return lengthFudgeScore;}
  double GetPvalue() {return runningPvalue;}
  
  // E-value is the Expected Value which is pN/n, not exact prob which is (1 - exp(-1*pN/n)
  // The approximation of the exact prob as pN/n or pD works iff
  // the value is < 0.1, hence the if statement.
  // if (pD < 0.01) { return pD; } else { return (1 - exp(-1*pD));}

  double GetEvalueN() {
    return double(runningPvalue * ( pDBval->getLen() / runningTotlen));
  }

  double GetEvalueD() {
    return double(runningPvalue * pDBval->getSeqs());
  }

  ptrPathEl GreatestEl() {return tail;}
  inline int length() { 
    int length =0; ptrPathEl p=head; while (p) {length++;p=p->next;}
    return length;
  }
  friend ostream & operator << (ostream & os, const Path & P);
  
  ptrPath next;
  ptrPathEl head;
  ptrPathEl tail;

private:
  // Pointer to the DatabaseValues class
  pDBV pDBval;

  float runningTotal;
  float lengthFudgeScore;
  double runningPvalue;
  float runningTotlen;

  //ptrPathEl tail;
};



// ****************************************************************************
// PathFinder class needs to be called with the pointer to the FpHead
//
// The constructor is called with a pointer to the FpHead
// this creates a pointer 'mot' looking at FpHead's pointer and then moves to
// the first match pointed at by the first Motif, this motif's list of matches
// pointed at by 'mch' foreach of the matches an MxM is Add-ed. (AddMxM(mot,mch)
// the list of matches is iterated around until the end, then the next motif is
// dealt with and so on.
// AddMxM(MotifScore *, MatchScore *), adds to a pointer a growing list of MxM
// objects, refering to each of the objects, motif and match pairs.
// AddPath adds a Path to the Pathhead list
// CreatPath creates a new Path to be added to the Pathhead list
// BestPath and BestPathScore are variables used to hold the highest scoring
// path
// *****************************************************************************


class PathFinder {
  typedef MotifxMatch * ptrMxM;
  typedef MatchScore  * ptrMCH;
  typedef MotifScore  * ptrMOT;
  typedef Path * ptrPath;
  typedef PathFinder * ptrPathFinder;
  typedef FpScore * ptrFpScore;
  typedef DatabaseValues * pDBV;

public:
    
  // Constructor only one form, this creates the MxM list
  PathFinder(ptrFpScore FpSc, double pvt, float dv, int GRAPH, pDBV dbv);
  // Destructor
  ~PathFinder();
  void CreatePath ( ptrMxM mxm, pDBV pdbv); // create new path and add to list
  void AddPath( ptrPath path );  // add an existing one to the list
  float Getbestscore() {return BestPathScore;}
  float GetbestLF() {return BestPathScoreLengthFudge;}
  double GetbestPV() {return BestPathScorePvalue;}
  //Pvalue as the sort criteria
  bool operator> (const PathFinder& P) {
    return ( BestPathScorePvalue > P.BestPathScorePvalue);}
  bool operator< (const PathFinder& P) {  
    return ( BestPathScorePvalue < P.BestPathScorePvalue);}
  friend ostream & operator << (ostream & os, const PathFinder & Pf);
  inline int length_MxM() { 
    int length =0;  ptrMxM p = MxMhead; 
    while (p) { length++; p=p->next;}
    return length; 
  }  
public: // Variables
  ptrMxM  MxMhead;
  ptrPath Pathhead;
  ptrPath BestPath;
  ptrPathFinder next;
  ptrFpScore FpS;
  void PrintMxM();
private:
  double PvalueThreshold;
  float DistanceVariance;
  int GetInterMotifDistance(int n, int m, float & l_int, float & h_int);
  int FindBestPath();
  void AddMxM (ptrMOT motif, ptrMCH match);
  MotifxMatch* MergeSortMxM ( MotifxMatch* p );
  MotifxMatch* divide ( MotifxMatch* p );
  MotifxMatch* merge ( MotifxMatch* p,  MotifxMatch* q );
  float BestPathScore;
  float BestPathScoreLengthFudge;
  double BestPathScorePvalue;

  pDBV pdbv;

  /*
    int operator> (const PathFinder& P) {
    return ( BestPathScoreLengthFudge > P.BestPathScoreLengthFudge);}
    int operator< (const PathFinder& P) {  
    return ( BestPathScoreLengthFudge < P.BestPathScoreLengthFudge);}
    // The preceeding code allows the Length fudge to be used as the sort criteria 
    int operator> (const PathFinder& P) { 
    return ((BestPathScore) > (P.BestPathScore));}
    int operator< (const PathFinder& P) { 
    return ((BestPathScore) < (P.BestPathScore));}
  */
};


// ****************************************************************************
// 
// ****************************************************************************


class PathFinderList {
  typedef PathFinder * ptrPathFinder;
  typedef PathFinderList * ptrPFL;
  typedef FpScore * ptrFpScore;
  typedef DatabaseValues * pDBV;

public:
  PathFinderList(double EVALTHRESHOLD);
  ~PathFinderList();
  void setDBVs(pDBV dbv,double l, double s, bool pD){
    //Makes the member pointer point at the instance of the DatabaseValues class
    //And sets the values
    pdbv = dbv; pdbv->DATABASESEQS = s; pdbv->DATABASELENGTH = l;
    if (pD) { pdbv->setMethod_pD(); } else { pdbv->setMethod_pNn(); }
  }
  void WeedOutEmptyPathFinders();
  void Sort();
  void PrintOutFirstList(int julian_selley);
  void PrintOutSecondList(int restrict);
  void PrintOutThirdList(int restrict);
  //  void PathFinderList::PrintOutStatsList();
  void PrintOutMxMlist();  
  void PrintOutAlign( Sequence * seq, int restrict);

  ptrPathFinder CreatePathFinder(ptrFpScore FpSc, double pvt, float dv, int GRAPH); 
    //create PF obj and add to list
  friend ostream & operator << (ostream & os, const PathFinderList & Pfl);
  
  ptrPathFinder head;

private:
  ptrPathFinder MergeSortPFL ( ptrPathFinder p);
  ptrPathFinder divide ( ptrPathFinder p);
  ptrPathFinder merge ( ptrPathFinder p, ptrPathFinder q);  
  static const int ARBITRARY_VALUE_SCORE_THRESHOLD = 1000;
  double EVALTHRESHOLD;
  pDBV pdbv;

};


#endif
