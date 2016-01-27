#include "PathFinder.h"
#include <stdio.h>
#include <math.h>
#include "cstdlib"

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

/*
PathFinder.cc code modified 2015-09-12 in InterProScan:
- Replaced cout with printf for some output in PathFinder.cc as on some user systems this was printing hex numbers?
    e.g. cout << setw(12) << eval; replaced with  printf("%-12.2g",eval);
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

// Copy constructor, follows template's path list, copying PathEls
// providing the mxm from the the template's path and adding a PathEl
// to the new Path's head, using the Addpath function
Path::Path( const Path& P )
  :next(0), runningTotal(0.0), head(0), 
   tail(0),  lengthFudgeScore(0.0), 
   runningPvalue(1.0),
   runningTotlen(0)   
{
  ptrPathEl p = P.head;
  
  while ( p != 0) {
    AddPathEl(p->mxm);
    p = p->next;
  }
}


/* Deconstructor throws away all PathEl objects in the list*/
Path::~Path()
{
  ptrPathEl q = head;
  
  if (q != 0) {
    while (q != 0) {
      ptrPathEl p = q;
      q = q->next;
      delete p;
    }
  }
}



void Path::AddPathEl( ptrMxM mxm) {
  ptrPathEl p = new PathEl( mxm );
  ptrPathEl q = head;
  
  // Add the score of the newest element to be added to the growing total 
  // for this path
  runningTotal += mxm->match->Getscore();
  double pval = (mxm->match->Getpvalue())?mxm->match->Getpvalue():1;
  // consider * by 0 values

  runningPvalue  *= pval;
  runningTotlen  += mxm->motif->Getlength();
  
  lengthFudgeScore += (mxm->match->Getscore() * mxm->motif->Getlength());
  
  if (q==0)
    head = p;
  else  {
    while (q->next != 0)
      q = q->next;
    q->next = p;  // find the last one!
  }
  tail = p; // Tail points at the last one in the list
}



ostream & operator << (ostream & os, const Path & P)
{ 
  PathEl * q = P.head;

  while (q != 0) {
    cout << *q;
    q = q->next;    
  }
  
  cout << "\tRunTotal= " << P.runningTotal << endl;
  return os;
}


// ****************************************************************************
// PathFinder class needs to be called with the pointer to the FpHead
//
// The constructor is called with a pointer to the FpHead
// this creates a pointer 'mot' looking at FpHead's pointer and then moves to
// the first match pointed at by the first Motif, this motif's list of matches
// pointed at by 'mch' foreach of the matches an MxM is Add-ed. 
// (AddMxM(mot,mch)
// the list of matches is iterated around until the end, then the next motif is
// dealt with and so on.
// AddMxM(MotifScore *, MatchScore *), adds to a pointer a growing list of MxM
// objects, refering to each of the objects, motif and match pairs.
// AddPath adds a Path to the Pathhead list
// ****************************************************************************

// Added GRAPH flag (To switch off pathfinder calculations)
PathFinder::PathFinder(ptrFpScore FpSc, double pvt, float dv, int GRAPH, pDBV dbv)
  : MxMhead(0), Pathhead(0), BestPath(0), BestPathScore(0), 
    BestPathScoreLengthFudge(0) , BestPathScorePvalue(0.0), 
    PvalueThreshold(pvt), DistanceVariance(dv), FpS(FpSc), next(0), pdbv(dbv)
{
  ptrMOT mot = FpS->Fphead;
  while (mot != 0) {
    ptrMCH mch = mot->Mthead;
    
    while (mch != 0) {
      AddMxM(mot,mch);
      mch=mch->next;
    }
    mot = mot->next;
  }
  MxMhead = MergeSortMxM(MxMhead);

  if (!GRAPH) {
    if (!FindBestPath()) {
      cerr << "Fingerprint " 
	   << FpS->Getfpname() 
	   << " matches this sequence" << endl;
      return;
    }
  }
}


//Deconstruction requires clearing of the pathlist and the mxmlist

PathFinder::~PathFinder()
{
  ptrMxM  mxm  = MxMhead;
  ptrPath path = Pathhead;

  while (mxm != 0) {
    ptrMxM delmxm = mxm;
    mxm = mxm->next;
    delete delmxm;
  }
  
  while (path != 0) {
    ptrPath delpath = path;
    path = path->next;
    delete delpath;
  }
}


// AddMxM 
// create a MxM object with the supplied ptrs to motif and match
// and add it to the end of the growing list
void PathFinder::AddMxM(ptrMOT motif, ptrMCH match ) {
  
  ptrMxM r = new MotifxMatch(motif, match);
  if (!r) {cerr << "Memory Allocation Error"<< endl; exit(0);}
  
  ptrMxM s = MxMhead;
  
  if (s == 0) {
    MxMhead = r;
  } else { 
    while (s->next != 0) {
      s = s->next;
    }
    s->next = r;
  }
  
}


// CreatePath
// Supply a pointer to an MxM object and it creates a Path object and appends
// it to the list headed by Pathhead
void PathFinder::CreatePath(ptrMxM mxm, pDBV pdbv)
{
  ptrPath p = new Path(mxm, pdbv);
  ptrPath q = Pathhead;

  if ( q == 0) {
    Pathhead = p;
  }
  else {
    while ( q->next != 0)
      q= q->next;
    q->next= p;
  }
}


// AddPath
// Supply a pointer to a Path obj, and it appends it to the list headed
// by Pathhead, this is the case of an already created Path obj
void PathFinder::AddPath( ptrPath path ) {
  ptrPath p = path;
  ptrPath q = Pathhead;
    
  if (q==0) {
    Pathhead = p; 
  }
  else  {
    while (q->next != 0)
      q = q->next;
    q->next = p;
  }
}

void PathFinder::PrintMxM()
{
  cout.setf(ios::left | ios::fixed);
  
  ptrMxM m = MxMhead;

  while (m) {
    printf("%d, %f, %d, %d, %d, %e\n",
    m->motif->Getmotnum(), m->match->Getidscore(),  
    m->match->Getpos(), m->motif->Getlength(), 
    FpS->Getnummotifs(),m->match->Getpvalue() );
    m=m->next;
  }
}


// FindBestPath
// This is the Algorithm behind the whole PathFinder object and its
// Associated data objects, Path, PathEl and  
// 
int PathFinder::FindBestPath()
{
  int debugmode = 0;
  ptrMxM  pmxm = MxMhead;
  ptrPath path = Pathhead;

  // # the threshold of pathlist DEATH
  int MAXALLOWSIZE = 200000;
  // the pvalue-threshold
  double pthreshold = PvalueThreshold;
  
  int pathCreated =0; // False
  int path_iterations =0;
  int ignoreDistances = 0; //False
  float varpercent;

  if (DistanceVariance) {
    float distance_variance = DistanceVariance; //%  
    varpercent = float(distance_variance) / 100;
  } else {
    ignoreDistances = 1;
    varpercent = 0.0;
  }  
  
  float l_int = 0.0;
  float h_int = 0.0;

  float lowerlimit, upperlimit, position, intermatchdist;
  double pvalue;
  int mxmnum, OutOfRange;

  int pmxm_num=0;
  while (pmxm != 0) {
    pmxm_num++;
    // For each iteration of the MxM list the 
    // path must be reset to Pathhead
    
    //   lowerlimit = (pmxm->motif->Getlstart()) * (1 - varpercent); // -%
    //   upperlimit = (pmxm->motif->Gethstart()) * (1 + varpercent); // +%

    position = pmxm->match->Getpos();
    pvalue = pmxm->match->Getpvalue();
    mxmnum = pmxm->motif->Getmotnum();
    
    
    path = Pathhead;
    pathCreated = 0;
    
    // The acceptable flag is true only if the pvalue of the match is less than
    // the pvalue threshold
    int acceptable = (pvalue < pthreshold) ;
    int outside_intermotif_distance = 0;

    if (path == 0  &&  acceptable)  {
      // Head of Pathlist points nowhere
      // Thus the only operation that is required is the creation
      // of a new path with one element the current MxM
      CreatePath(pmxm,pdbv);
      pathCreated = 1;
    } else {

      // #################
      // # This is the important number 'counter' when it reaches 200000
      // # then we must kill this PathFinder as it will probably fail!!!!
      // # Kill the Pathfinder if list is greater than MAXALLOWSIZE
      // cout << "Path Iterations " << path_iterations << endl;
      if (path_iterations > MAXALLOWSIZE) { 
	cerr << "ERROR: Calculation has exceeded maximum allowed complexity" << endl; 
	//Dont need to return any error on STDOUT because there will be a result
	return 0;
      }
      if (debugmode) {
	cout << "---------------\nstart path iteration with   motif number " << mxmnum << " @ "<< position << " - "<< pvalue << endl;
      }
      while (path != 0) {
	path_iterations++;
	// Iterate around all paths getting the Motif number of 
	// that path's greatest member (tail points to the last one added)
	int pathGreatestnum = path->tail->mxm->motif->Getmotnum();
	int last_match_pos = path->tail->mxm->match->Getpos();
	int last_match_len = path->tail->mxm->motif->Getlength();
	int notoverlapping = !( position < (last_match_pos + last_match_len - 1) );

	if (debugmode) {
	  cout << "path\n" << *path << endl;
	  cout << "\t" << (acceptable?"acceptable":"not acceptable") << endl;
	  cout << "\t" << (notoverlapping?"not overlapping":"overlapping") << endl;
	}
	if ((mxmnum > pathGreatestnum) && acceptable && notoverlapping )  {
	  GetInterMotifDistance(mxmnum,pathGreatestnum,l_int,h_int);
	  intermatchdist = (position - (last_match_pos + last_match_len));

	  // COMPUTE the average intermotif distance
	  // and then based on the calculation of taking a percentage of this mean distance
	  // and taking this off the lower limit and adding it to the upper.
	  float mean   = float(h_int + l_int) / 2.0;
	  float lowest = l_int - (mean * varpercent);
	  float highest= h_int + (mean * varpercent);
	  lowest = ((lowest < 0) ? 0:lowest); 
	  //zero's anything computed to be less than 0 (negatives other than -1 are not important
	  
	  if (!ignoreDistances) {	   
	    if (l_int == -1 && intermatchdist == -1) {
	      // The -1 special case
	      outside_intermotif_distance = 0;
	      // Distance is ok
	    } else if ((lowest <= intermatchdist) && (intermatchdist <= highest) ) {
	      //	    if ((fabs(l_int* (1 - varpercent) ) <= fabs(intermatchdist)) && 
	      //		(fabs(intermatchdist) <= fabs(h_int * (1 + varpercent)))) {
	      outside_intermotif_distance = 0;
	      // Distance is ok
	    } else {
	      outside_intermotif_distance = 1;
	      if (debugmode) {
		cout << "\tOutside intermotif distance" << endl;
	      }

	      // Distance is bad. Ummm'kay.
	    }

	    if (debugmode) {
	      cout << "\t--" << endl;
	      cout << "\t\tact  " << l_int << " < " << h_int << endl;
	      cout << "\t\told  " << (fabs(l_int* (1 - varpercent) ))  << " < " << fabs(intermatchdist) << " < " << (fabs(h_int * (1 + varpercent))) << endl;
	      cout << "\t\tnew  " << lowest  << " < " << fabs(intermatchdist) << " < "  << highest << endl 
		   << "\t\tmean " << mean << endl;
	      cout << "\t\tdevi " << (mean * varpercent) << endl; 
	    }
	  
	  } else {
	    //hence ignore distance criteria
	    outside_intermotif_distance = 0;
	  }
	  
	
	  if (!outside_intermotif_distance) {
	    // Finally all criteria are met so add to the list
	    // this operation requires the duplication of the
	    // current path and then the new MxM PathEl is appended to that
	    
	    ptrPath h = new Path(*path);
	    h->AddPathEl( pmxm );
	    AddPath(h); 
	    pathCreated = 1; // True

	    if (debugmode) {
	      cout << "\tinside intermotif distance\n\n\tOn the basis of the above ADD\nnew\n" << *h << endl;
	    }
	    // This path has been dealt with, go on to next; 
	    // But first check if it needs to be added to the BestPath pointer
	    if (h->GetrunningTotal() > BestPathScore) {
	      BestPath = h;
	      BestPathScore = h->GetrunningTotal();
	      BestPathScoreLengthFudge = h->GetlengthFudge();
	      BestPathScorePvalue = h->GetPvalue();
	    }
	    path = path->next;
	  } else {
	    // If the current MxM cannot be appended to this
	    // path then go to next 
	    if (debugmode) {
	      cout << "\n\tOn the basis of the above Dont ADD" << endl;
	    }
	    
	    path = path->next;
	  }
	}
	else {
	  // If the current MxM cannot be appended to this
	  // path then go to next 
	  path = path->next;
	}
      }
    
      // a path cannot be created if the element is out of range
      if (!pathCreated && acceptable && !outside_intermotif_distance ){
	CreatePath(pmxm,pdbv); 
      }
      // All paths have been iterated around now change MxM and repeat
    } // end else 
    pmxm = pmxm->next;
  } 
  // All MxM's have been iterated around, return!  
  return 1;
}

int PathFinder::GetInterMotifDistance(int n, int m, float & l_intermotif_distance, float & h_intermotif_distance ) {
  ptrMOT p = FpS->Fphead;
  
  //cout << FpS->Getfpname() << endl;
  //cout << "Find the distance between " << n << " and " << m << endl; 

  l_intermotif_distance = 0.0;
  h_intermotif_distance = 0.0;
  
  while (p->next != 0 && p->Getmotnum() < m) {
    p = p->next;
  }

  // Some motif lists have members missing

  //int i=m;

  while (p->next != 0 && p->Getmotnum() < n) {
    
    if ( p->Getmotnum() > m) { // && p->Getmotnum() < n ) {
       l_intermotif_distance += (p->Getlinter() + p->Getlength());
       h_intermotif_distance += (p->Gethinter() + p->Getlength());
    }
    //cout << "i=" << i << " n=" << n << "  Motif" << p->Getmotnum() << endl;
    p = p->next; //i++;

  }

  l_intermotif_distance += p->Getlinter();
  h_intermotif_distance += p->Gethinter();

  //cout << "l= " << l_intermotif_distance << endl;
  //cout << "h= " << h_intermotif_distance << endl;

}

ostream & operator << (ostream & os, const PathFinder & Pf)
{ 
  if (Pf.BestPath != 0) {
    cout << *(Pf.BestPath) << endl;
  }
  return os;
  
}


MotifxMatch* PathFinder::MergeSortMxM ( MotifxMatch* p ) {
  ptrMxM head = p;
  ptrMxM q;

  if (p && p->next) {
    q = divide(p);
    p = MergeSortMxM(p);
    q = MergeSortMxM(q);
    head = merge(p,q);
  }
  return head;
}

MotifxMatch* PathFinder::divide ( MotifxMatch* p ) {
  ptrMxM q,r;
  q = p;
  r = p->next->next;

  while (r != 0) {
    r = r->next;
    q = q->next;
    if (r != 0) { r = r->next; } // move r two pos for each one q
  }
  // essentially moved q half of the way through the list
  r = q->next;   // break the list after q
  q->next = 0;

  return r;   // return pointer to second half
}


MotifxMatch* PathFinder::merge ( MotifxMatch* p,  MotifxMatch* q ) {
  ptrMxM head, r;
  
  if (!p || !q) {
    cerr << "Mergesort called with empty lists" << endl;
    exit(0);
  }
  if ( (*p) < (*q) ) {
    head = p;
    p = p->next;
  } else {
    head = q;
    q = q->next;
  }

  r = head;
  
  while ( p && q ) {
    if ((*p) < (*q)) {
      r = r->next = p;
      p = p->next;
    } else {
      r = r->next = q;
      q = q->next;
    }
  }
  
  if (p != 0) {
    r->next = p;
  }
  else {
    r->next = q;
  }
  return head;
}



// ***************************************************************************
// PathFinderList object
// a list of PathFinderObjects
// ***************************************************************************


PathFinderList::PathFinderList(double evalth)
  : head(0), EVALTHRESHOLD(evalth) 
{
}


PathFinderList::~PathFinderList() 
{
  ptrPathFinder p = head;
  
  while (p != 0) {
    ptrPathFinder del = p;
    p = p->next;
    delete del;
  }
}


PathFinder* PathFinderList::MergeSortPFL ( ptrPathFinder p ) {
  ptrPathFinder head = p;
  ptrPathFinder q;

  if (p && p->next) {
    q = divide(p);
    p = MergeSortPFL(p);
    q = MergeSortPFL(q);
    head = merge(p,q);
  }
  return head;
}

PathFinder* PathFinderList::divide ( ptrPathFinder p ) {
  ptrPathFinder q,r;
  
  q = p;
  
  r = p->next->next;

  while (r != 0) {
    r = r->next;
    q = q->next;
    if (r != 0) { r = r->next; } // move r two pos for each one q
  }
  // essentially moved q half of the way through the list
  r = q->next;   // break the list after q
  q->next = 0;

  return r;   // return pointer to second half
  
}

PathFinder* PathFinderList::merge ( ptrPathFinder p,  ptrPathFinder q ) {
  ptrPathFinder head, r;

  if (!p || !q) {
    cerr << "Mergesort called with empty lists" << endl;
    exit(0);
  }
  if ( (*p) < (*q) ) { // Sort > greater than < less than 
    head = p;
    p = p->next;
  } else {
    head = q;
    q = q->next;
  }

  r = head;
  
  while ( p && q ) {
    if ((*p) < (*q)) { // Sort greater than > less than <
      
      r = r->next = p;
      p = p->next;
    } else {
      r = r->next = q;
      q = q->next;
    }
  }
  
  if (p != 0) {
    r->next = p;
  }
  else {
    r->next = q;
  }
  return head;
}


PathFinder* PathFinderList::CreatePathFinder(ptrFpScore FpSc, double pvt, float dv, int GRAPH)
{
  ptrPathFinder p = new PathFinder(FpSc,pvt,dv,GRAPH,pdbv);
  ptrPathFinder q = head;

  if ( q==0 ) {
    head = p;
  } else {
    while ( q->next != 0)
      q= q->next;
    q->next= p;
  }
  return p;
}

void PathFinderList::WeedOutEmptyPathFinders() {
  ptrPathFinder p = head;
  ptrPathFinder q, t;

  //cout << "Weed'n'" << endl;
  //if (!p) { cout << "p doesnt point anywhere!" << endl; }
  if (p && p->GetbestLF() == 0) {
    q = p->next; //
    delete p;
    head = q; // make head point at the next element
    p = head; // reset p
  }
  while (p && p->next) {
    if (p->next->GetbestLF() == 0) {
      //    cout << "This one needs to be removed ";
      //cout << "p-> " << p->FpS->Getfpname() << endl;
      q = p->next->next;
      t = p->next;
      p->next = q;
      delete t;
    }
    p=p->next;
  }
}

void PathFinderList::Sort() { 
  head = MergeSortPFL( head ); 
}

void PathFinderList::PrintOutFirstList(int julian_selley) {
  PathFinder * p = head;
  cout.setf(ios::left);   
  int nothingInList = 1; // There is nothing in the list
  double eval = 0.0;

  cout << "1TBS" << endl;  
  while (p) {
    if (p->BestPath != 0) {

      if (pdbv->getMethod()) { 
	eval = p->BestPath->GetEvalueD();} 
      else {
	eval = p->BestPath->GetEvalueN();
      }


      FpScore * fps = p->FpS;
      if (eval < EVALTHRESHOLD)  {
	cout << "1TBH " 
	     << setw(16) << fps->Getfpname();
//	     << setw(12) << setiosflags(ios::scientific) <<  eval;
	     	printf("%-12.2g",eval);
	if (julian_selley) {
	  cout << "   " << setw(70) << fps->Getinfo(); 
	  cout << "   " << setw(16) << fps->Getacc(); 
	}
	cout << endl;
	nothingInList = 0; // There is something in the list
      }
    }  
    p=p->next;
  }
  if (nothingInList) {
    cout << "1TBN "
	 << setw(21) << "NO SIGNIFICANT RESULTS ABOVE ";
//	 << setw(8) << setprecision(3)<< setiosflags(ios::scientific) <<  EVALTHRESHOLD
//	 << setw(8) << setiosflags(ios::scientific) << setprecision(3) << EVALTHRESHOLD
	  printf("%-10.3g", EVALTHRESHOLD);
	 cout << endl;
  }
  cout << "1TBF" << endl;
}


void PathFinderList::PrintOutSecondList(int restrict) {
  PathFinder * p = head;
  int CountToTen = 0;
  double eval = 0.0;
  cout.setf(ios::left | ios::fixed);     
  cout << "2TBS" << endl;

  cout << "2TBT ";
  cout << setw(16) << "FingerPrint";
  cout << setw(10) << "No.Motifs";
  cout << setw(9) << "SumId";
  cout << setw(9) << "AveId";
  cout << setw(11) << "ProfScore";
  cout << setw(12) << "Ppvalue";
  cout << setw(12) << "Evalue";
  cout << setw(24) << "GraphScan";
  cout << endl;

  // Iterate around BestPaths
  while (p) {
    // Only if a best path has been assigned to the pathfinder object
    if (p->BestPath != 0 && CountToTen < 10) {
      //      eval = p->BestPath->GetEvalueN(); 
      if (pdbv->getMethod()) { 
	eval = p->BestPath->GetEvalueD();} 
      else {
	eval = p->BestPath->GetEvalueN();
      }

      if (!restrict || (eval < EVALTHRESHOLD)) {
	// Iterate around each match in the best list
	PathEl * h =  p->BestPath->head; // point at each PathEl in turn
	FpScore * fps = p->FpS;  // The respective FpScore object
	
	float sumIdentity = 0.0;
	int sumProfscore = 0;
	double prodPvalue = 1.0;
	int numMotifs   = 0;
	float averageIdentity = 0.0;
	
	//// The sum of each match's score * length
	mystring asciiGraphic = "";
	
	for (int i = 1; i <= (fps->Getnummotifs()); i++) { 
	  asciiGraphic += '.';
	}
	prodPvalue = p->BestPath->GetPvalue();
	// enter loop of matches calculating values
	while (h) {
	  MatchScore * mch = h->mxm->match;
	  float idscore =  mch->Getidscore();
	  sumIdentity += idscore;
	  sumProfscore += mch->Getscore();
	  numMotifs += 1;
	  for (int i = 1; i <= (fps->Getnummotifs()); i++) { 
	    if (i == h->mxm->motif->Getmotnum()) {
	      if (idscore > 30) {
		asciiGraphic[i-1] = 'I';
	      } else {
		asciiGraphic[i-1] = 'i';
	      }
	    }
	  }
	  h=h->next;
	}
	averageIdentity 
	  = (numMotifs) ? ( float(sumIdentity / float(numMotifs))) : sumIdentity;
	
	cout << "2TB";
	cout << (eval < EVALTHRESHOLD  ? "H ":"N ");
	cout << setw(16) << fps->Getfpname();
	cout << setw(3)  << numMotifs;
	cout << setw(4)  << "of";
	cout << setw(3)  << fps->Getnummotifs();
//	cout << setw(9)  << setprecision(2) << sumIdentity;
	printf("%-9.2f",sumIdentity);
//	cout << setw(9)  << setprecision(2) << averageIdentity;
	printf("%-9.2f",averageIdentity);
	cout << setw(11) << sumProfscore;
//	cout << setw(12) << setiosflags(ios::scientific) <<  prodPvalue; //printf("%-12.2e",prodPvalue);
	printf("%-12.3g",prodPvalue);
//	cout << setw(12) << eval;
	printf("%-12.2g",eval);
	cout << setw(16) << resetiosflags(ios::scientific)<< asciiGraphic;
	cout << endl;
	CountToTen++;
      } 
    }
    p=p->next;
  }
  cout << "2TBF" << endl;
}

void PathFinderList::PrintOutAlign( Sequence * seq, int restrict) {
  PathFinder * Pf = head;
  const mystring sequence = seq->Getresidue_list();
  const int seqlen  = strlen(sequence.peek());
  double eval = 0.0;
  const int padding = 35;
  const int scale   = seqlen - (padding * 2);
  const int width = 74;

  while (Pf) {
    // Only if a best path has been assigned to the pathfinder object
    Path * BPath = Pf->BestPath;

    if (BPath != 0) {

      if (pdbv->getMethod()) { 
	eval = BPath->GetEvalueD();
      } else {
	eval = BPath->GetEvalueN();
      }

     if (!restrict || (eval < EVALTHRESHOLD)) {

	PathEl * BPe  = BPath->head; // point at each PathEl in turn
	FpScore * fps = Pf->FpS;  // The respective FpScore object
      
	int sum = 0;
	mystring * sstrings = new mystring[3];

	mystring seqstring = "";
	mystring constring = "";
	mystring seqrep = "";

	// create a representation of the full sequence
	for (int i=padding;i<seqlen-35;i++) {
	  seqrep += sequence[i];
	}

	// enter loop of Path Elements calculating values
	while (BPe) {
	  
	  MotifScore * mot = BPe->mxm->motif;
	  MatchScore * mch = BPe->mxm->match;
	  const int pos = mch->Getpos() - 1;
	  const int len = mot->Getlength();

	  const int spacer = (pos - sum);
	  const int extent = (spacer + len);

	  for (int i=0;i<spacer;i++) {
	    seqstring += " ";
	    constring += " ";
	  }
	  seqstring += mch->Getseq();
	  constring += mot->Getcons();

	  BPe = BPe->next; //Next path element
	  sum += extent;
	}

	//Make all strings the same length by padding out with spaces
	for (int k= constring.length();k<seqrep.length();k++) {
	  constring += " ";
	  seqstring += " ";
	}

	sstrings[0] = seqrep;
	sstrings[1] = constring;
	sstrings[2] = seqstring;


	int c = 0;
	int w = 1;
	int number = 2;
	mystring final_string ="";

	while (c < seqrep.length()) {
	  for (int j=0;j<number;j++) {
	    for (int l=(width * (w-1));l<(width * w);l++) {
	      if (l < seqrep.length()) {
		final_string += sstrings[j][l];
	      }
	    }
	    final_string += "\n4TBL ";
	  }
	  final_string += "\n4TBS ";
	  c+=width;
	  w++;
	}
	cout << "4TBT ";
	for (int m=0;m<width;m++) { cout << "-";} cout << endl;
	cout << "4TBN " << fps->Getfpname() << endl; 
	cout << "4TBS " << final_string;
	for (int p=0;p<width;p++) { cout << "-";} cout << endl;
      }
    }
    Pf = Pf->next;
  }
}

void PathFinderList::PrintOutThirdList(int restrict) {
  PathFinder * p = head;
  int CountToTen = 0;
  cout.setf(ios::left | ios::fixed);
  double eval = 0.0;

  cout << "3TBS" << endl;
  cout << "3TBT ";  // 3rd TaBle Title line
  cout << setw(16) << "MotifName";
  cout << setw(10) << "No.Mots";
  cout << setw(8)  << "IdScore";
  cout << setw(8)  << "PfScore";
  cout << setw(10) << "Pvalue";
  cout << setw(56) << "Sequence";
  cout << setw(5)  << "Len";
  cout << setw(5)  << "low";
  //cout << setw(5)  << "pos";
  cout << setw(6)  << "pos";
  cout << setw(5)  << "high";
  cout << endl;

  // Iterate around BestPaths 
  int i = 0;
  while (p) {
    // Only if a best path has been assigned to the pathfinder object
    if (p->BestPath != 0){
      if (i < 10)  {
	// Iterate around each match in the best list
	if (pdbv->getMethod()) { 
	  eval = p->BestPath->GetEvalueD();} 
	else {
	  eval = p->BestPath->GetEvalueN();
	}

	if (!restrict || (eval < EVALTHRESHOLD)) {

	  PathEl * h =  p->BestPath->head; // point at each PathEl in turn
	  FpScore * fps = p->FpS;  // The respective FpScore object
	  
	  // enter loop of matches calculating values
	  while (h) {
	    MotifScore * mot = h->mxm->motif;
	    MatchScore * mch = h->mxm->match;
	    
	    cout << "3TB";
	    cout << ((eval < EVALTHRESHOLD )?"H ":"N ");
	    cout << setw(16) << fps->Getfpname();
	    //cout << setw(16) << fps->Getacc();
	    cout << setw(3)  << mot->Getmotnum(); 
	    cout << setw(4)  << "of";
	    cout << setw(3)  << fps->Getnummotifs();
	    cout << setw(8) << setprecision(2) << mch->Getidscore();
	    cout << setw(8) << setprecision(2) << mch->Getscore();
	    printf("%-10.2e", mch->Getpvalue());
	    // cout << setw(10) << setprecision(3) << mch->Getpvalue();
	    cout << setw(56) << mch->Getseq();
	    cout << setw(5)  << mot->Getlength();
	    cout << setw(5)  << mot->Getlstart();
	    //cout << setw(5)  << mch->Getpos();
	    cout << setw(6)  << mch->Getpos();
	    cout << setw(5)  << mot->Gethstart();
	    cout << endl;
	    
	    h=h->next;
	    
	  }
	  cout << "3TBB"<< endl;
	  i++;
	}
      }
    }
    p=p->next;
  }
  cout << "3TBF" << endl;
}


void PathFinderList::PrintOutMxMlist() {
  PathFinder * p = head;

  while (p) { p->PrintMxM(); p=p->next; }
}


ostream & operator << (ostream & os, const PathFinderList & Pfl)
{ 
  PathFinder * p = Pfl.head;
  while (p) {

    if (p->BestPath != 0) {
      
      // Iterate around each match in best list
      PathEl * h =  p->BestPath->head;
      FpScore * fps = p->FpS;
      
      cout << fps->Getfpname()<< "  " << p->Getbestscore() << endl;
      cout << "Motif   Score  Sequence              low pos high" << endl;
      while (h) {
	MotifScore * mot = h->mxm->motif;
	MatchScore * mch = h->mxm->match;
	
	cout << mot->Getmotnum() << " of " << fps->Getnummotifs();
	cout << "  " << setprecision(4) << mch->Getscore();
	//cout << "  " << setfill(32) << mch->Getseq();
	//cout << "  " << setfill(4)  << mot->Getlstart();
	//cout << "  " << setfill(4)  << mch->Getpos();
	//cout << "  " << setfill(4)  << mot->Gethstart();
	
	
	cout << "  " << setfill('\x20') << mch->Getseq();
	cout << "  " << setfill('\x4')  << mot->Getlstart();
	cout << "  " << setfill('\x4')  << mch->Getpos();
	cout << "  " << setfill('\x4')  << mot->Gethstart();
	
	cout << endl;
	h=h->next;
      }
      
      cout << endl;
    }
    
    p=p->next;
  }
  return os;
  
}
