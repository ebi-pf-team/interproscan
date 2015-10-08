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


/*****************************************************************************
Motif Score Elements
*****************************************************************************/

//All work is done inline in the header file

/*****************************************************************************
MotifScoreList
*****************************************************************************/

/* Constructor for Motif data items, list head and next pointer*/
MotifScore::MotifScore( mystring mt, int lst, int hst, 
			int lin, int hin, 
			int len, int num ,float ave, 
			float stdev, mystring cons)
  :  lstart(lst), hstart(hst), linter(lin), hinter(hin), 
     length(len), Mthead(0), next(0), motifname(mt), motnum(num), 
     AveSelfScore(ave), consensus(cons)
{
}

/* Deconstructor throws away all motif objects in the list*/
MotifScore::~MotifScore()
{
  ptr_MatchScore q = Mthead;
  
  if (q != 0)
    while (q != 0)   {
      ptr_MatchScore p = q;
      q = q->next;
      delete p;
    }
}

/* Add Match to head of list*/
void MotifScore::AddMS (int sc, int pos, mystring seq)
{
  ptr_MatchScore p = new MatchScore(sc, pos, seq);
  ptr_MatchScore q = Mthead;
  
  if (q==0)
    Mthead = p;
  else  {
    while (q->next != 0)
      q = q->next;
    q->next = p;
  }
}

void MotifScore::AddMS (int sc, double pval,int pos, mystring seq){
  ptr_MatchScore p = new MatchScore(sc, pval, pos, seq);
  ptr_MatchScore q = Mthead;
  
  if (q==0)
    Mthead = p;
  else {
    while (q->next != 0)
      q = q->next;
    q->next = p;
  }  
}

void MotifScore::AddMS (int sc, float idsc, double pval,int pos, mystring seq){
  ptr_MatchScore p = new MatchScore(sc, idsc, pval, pos, seq);
  ptr_MatchScore q = Mthead;
  
  if (q==0)
    Mthead = p;
  else {
    while (q->next != 0)
      q = q->next;
    q->next = p;
  }  
}

/* extension of << operator to output Motif data*/
ostream & operator << (ostream & os, const MotifScore & M)
{
  cout << setw(14)<< M.motifname << "  " 
       << setprecision(3) << M.length << " ";
  return os;
}


void MotifScore::TraverseMS()
{
  MatchScore * q = Mthead;
  while (q != 0) {
    q->PrintMatchScore();  //print matches
    q = q->next;
  }
}

/*****************************************************************************
Score Fingerprint List Item
each Fingerprint will create an instance of this class this will represent
the Score of the Fingerprint, which is represented in turn by the number
of motifs that SCORE
each fingerprint Score object will maintain a linked list of Motif score
objects, thus allowing a query of the number of motifs that MATCH
*****************************************************************************/

/* Constructor for Fingerprint data items, list head and next pointer*/
FpScore::FpScore(mystring fpn, int nm, mystring ac, mystring in)
 : nummotifs(nm), next(0), Fphead(0),
   fpname(fpn), accession(ac) ,info(in)
{
}

/* Deconstructor throws away all fingerprint list items*/
FpScore::~FpScore()
{
  ptr_MotifScore q = Fphead;
  
  if (q != 0)
    while (q != 0) {
      ptr_MotifScore p = q;
      q = q->next;
      delete p;
    }
}

/* Add Motif to fingerprint list*/
MotifScore * FpScore::AddMotif (mystring motname, int lstart, int hstart, int
                                linter, int hinter, int length, int num,
				float ave, float stdv, mystring consensus)
{
  ptr_MotifScore p = new MotifScore(motname,lstart,hstart,
				    linter,hinter,length,num,
				    ave,stdv,consensus);
  ptr_MotifScore q = Fphead;
  
  if (q==0)
    Fphead = p;
  else    {
    while (q->next != 0)
      q = q->next;
    q->next = p;
  }
  return p;
}


// Delete motifs  
//
//
void FpScore::DeleteMotifs()
{
  if (Fphead==0) {
    //cerr << "Motif List Empty Fphead=" << Fphead << endl;
    return;  //List empty
  }
  else {
    if (Fphead->Mthead==0) {
      // hence this is the first motif in the list and it is empty
      ptr_MotifScore r = Fphead;
      Fphead = r->next;
      delete r;
      //cerr << "Delete first Motif" << endl;
      DeleteMotifs(); //Recursion necessary here
      return;
    }
    else {
      if (Fphead->next==0) { //If  Mthead !=0 AND next==0 
 // there is something in the match list but no more in the
 // Motif list
 //cerr << "Dont del first motif" << endl;
 return;
      }
    }
      
    ptr_MotifScore p = Fphead;
    ptr_MotifScore q = Fphead;
    
    
    // move through the motifs looking for empty lists
    //
    while (p->Mthead != 0 && p->next != 0)  {
      q=p;      // Assign p's value to q
      p=p->next;// Move p onwards, thus q always points at the previous
    }
    
    if (p->Mthead == 0) {  // If true then delete this Motif
      
      q->next = p->next; // Assign the value pointed at by the next pointer
      // in the motif to be deleted to the previous next
      // preserving the link
      delete p;          // then delete the Motif leaving the chain intact
      DeleteMotifs();    // recursive call, function will terminate at any
      // of the returns either list empty or no head==0's
    }
    else {
      return; //otherwise we have come to the end of the list
    }
  }
  return;
}

/* Print Fingerprint data*/
void FpScore::PrintFpScore()
{
  
  cout << "Fp  " << fpname << "  motifs  " << nummotifs << endl;
  cout << "Ac " << accession << " inf  " << info << endl;
  ptr_MotifScore q = Fphead;
  
  while (q != 0)
    {
      cout << *(q)<<endl;  //print motifs
      q = q->next;
    }
  
}


void FpScore::TraverseMt()
{
  ptr_MotifScore q = Fphead;
  while (q != 0)    {
    q->TraverseMS(); 
    q = q->next;
  }
}

/*****************************************************************************
Score Object
This Object maintains a linked list of Fingerprint Score objects
*****************************************************************************/

/* Constructor for Score list*/
ScoreList::ScoreList()
{
  Schead = 0;
}

/* Deconstructor for score list throws away all fingerprints*/
ScoreList::~ScoreList()
{
  ptr_FpScore q = Schead;
  
  if (q != 0)  {
    while (q != 0) {
      ptr_FpScore p = q;
      q = q->next;
      delete p;
    }
  }
}

/* Add a fingerprint to the score list*/
FpScore * ScoreList::AddFp (mystring fpname, int numots, mystring access, mystring inf)
{
  ptr_FpScore p = new FpScore(fpname,numots,access, inf);
  ptr_FpScore q = Schead;
  
  if (q==0)
    Schead = p;
  else
    {
      while (q->next != 0)
       q = q->next;
      q->next = p;
    }
  return p;
}


void ScoreList::DeleteFps()
{

  if (Schead==0) {
    //cerr << "List Empty" << endl;
    return;  //List empty
  }
  else  {
    if (Schead->Fphead==0) {
      // hence this is the first Fp in the list and it is empty
      
      ptr_FpScore r = Schead;
      Schead = r->next;
      //cerr << "delete Fp" << endl;
      delete r;
      DeleteFps(); //Recursion necessary here
      return;
    }
    else {
      if (Schead->next==0) { //If  Fphead !=0 AND next==0
 // there is something in the Motif list but no more in the
 // Fp list
 return;
      }
    }
     
    ptr_FpScore p = Schead;
    ptr_FpScore q = Schead;
    
    // Iterate along list of Fingerprint objects
    
    while (p->Fphead != 0 && p->next != 0) {
      q=p;         // Assign p's value to q
      p=p->next;   // Move p onwards, thus q always points at the prev
    }
    
    if (p->Fphead == 0){ // If true then delete this Motif
      
      q->next = p->next; 
      // Assign the value pointed at by the next
      // in the Fp to be deleted to the previous next
      // preserving the link
      delete p;    // then delete the Fp leaving the chain intact
      DeleteFps(); //recursive call, function will terminate at any
      //of the returns either list empty or no head==0's
    }
    else {
      return; //otherwise we have come to the end of the list
    }
  }
  return;
}


void ScoreList::CleanUp()
{
  FpScore * p = Schead;
  while (p!=0)
    {
      p->DeleteMotifs();
      p=p->next;
    }
  DeleteFps();
}


/* Output fingerprint data*/
ostream & operator << (ostream & os, const ScoreList & Sc)
{
  
  FpScore * q = Sc.Schead;
  while (q != 0)
    {
      q->PrintFpScore();
      cout << endl;
      q = q->next;
    }
  
  return os;
}


/* Traverse fingerprints*/
void ScoreList::Traverse()
{
  
  FpScore * q = Schead;
  while (q != 0)
    {
      q->TraverseMt();
      //cout << "New fingerprint" << endl;
      q = q->next;
    }
  
}

