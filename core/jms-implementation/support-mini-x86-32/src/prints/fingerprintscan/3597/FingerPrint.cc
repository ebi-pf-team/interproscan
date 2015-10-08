#include <iostream>
#include <cstring>
#include <fstream>
#include <cstdlib>
#include <cstdio>
#include <cctype>
#include <iomanip>
#include <cmath>
#include <string>
#include <algorithm>
#include <vector>
#include <map>
using namespace std;

char * GNU_LICENCE = "\
FingerPRINTScan II: A PRINTS fingerprint identification algorithm.\
Copyright (C) 1998,1999  Phil Scordis\
\
Phil Scordis can be contacted at scordis@bioinf.man.ac.uk, and\
Manchester Bioinformatics, rm 2.19, School of Biological Sciences,\
Stopford Building, University of Manchester, Oxford Road,\
Manchester, M13 9PT, UK.\
\
This work was supported by a grant from Astra Pharmaceuticals and\
developed at University College London and Manchester University.\
\
This program is free software; you can redistribute it and/or\
modify it under the terms of the GNU General Public License\
as published by the Free Software Foundation; either version 2\
of the License, or (at your option) any later version.\
\
This program is distributed in the hope that it will be useful,\
but WITHOUT ANY WARRANTY; without even the implied warranty of\
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\
GNU General Public License for more details.\
\
You should have received a copy of the GNU General Public License\
along with this program; if not, write to the Free Software\
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,\
USA, or alternatively look at http://www.gnu.org/copyleft/gpl.html.\
";

#ifndef SEQUENCE_DATABASE_H
#include "Sequence_database.h"
#endif

#ifndef PATHFINDER_H
#include "PathFinder.h"
#endif

#ifndef SUBSTITUTIONMATRIX_H
#include "SubstitutionMatrix.h"
#endif

#ifndef FREQUENCYTABLE_H
#include "FrequencyTable.h"
#endif

// Global variables
float VERSION_NUMBER = 3.596;


//
int threshold          = 15; //Global variable for default threshold of 15%
int DistanceVariance   = 10; //Global distance variable
double PvalueThreshold = 1.0 / double(pow(10.0,1)); // 1e-1
int options            = 15; // the binary options to indicate which printing methods

// SWISS-PROT 38
const mystring DEFAULTDB = "SWISS-PROT 38";
const double DEFAULTDBSEQS   = (double)80000.0 ; //Number of sequences
const double DEFAULTDBLEN    = (double)29610260.0; //Number of residues
double DBSEQS          = DEFAULTDBSEQS ; //Number of sequences
double DBLEN           = DEFAULTDBLEN; //Number of residues
bool UsepDmethod       = 0;

double DatabaseValues::DATABASESEQS   = DBSEQS;
double DatabaseValues::DATABASELENGTH = DBLEN;
bool DatabaseValues::MethodIs_pD = UsepDmethod; // Default to p * N/n method of eval calc if == 1 then p * D is used. 

// FLAGS
int GRAPH         = 0;
int GRAPHDEBUG    = 0; //This will allow the graph function to use unweighted scores
int CONSENSUS     = 0; //Global flag to determine use of consensus alignment function
int shuffle       = 0; //Default not to call shuffle function
int julian_selley = 0; //By default flag is off
int RESTRICT      = 0; // This flag if on, will restrict all tables to reporting


/***************************************************************************/
/*

The Motif Line element represents the structure of elements of the linked list
which facilitates the storage of the motif in the motif object

A description of the data (more info in motif structure)

fd;  FLEELRKGNLEREC    THRB_MOUSE     48    48
     ^         ^     ^
     |         |     |
       line_of_aas     start  interval

Mot_size is am integer storing the length of the motif i.e. the number of amino
acids in the motif in this example 14

*/

class MotifLineEl {
friend class Motif;
typedef char * Ptrchar;
typedef MotifLineEl * ptr_MotLinEl;

private:
  MotifLineEl( mystring line, const int st, const int Int, const int num_aas)
    : start(st), interval(Int), mot_size(num_aas),
      line_of_aas(/*new char [mot_size+1]*/line), next(0)
    {
      // Copy parameter line into new data storage of correct size
      //strcpy(line_of_aas,line);
    }

  ~MotifLineEl() {  /* delete [] line_of_aas; */ }

public:
  void print_MotifLineEl()
    {
      cout << line_of_aas << "\t" << start << "   \t" << interval << endl;
    }

  ptr_MotLinEl next; //linked list implementation requires
  //a pointer to the next object

private:
  const int start;      //start of motif feature in sequence
  const int interval;      //disance from last motif feature
  const int mot_size;      //length of motif feature
  mystring line_of_aas;        //character mystring containing motif aas

};




/***************************************************************************/
// The motif data structure represents the important data in the motif
// entry of the prints database which is of the form seen below
/*

fc; GLABLOOD1
fl; 14
ft; Vitamin K coagulation factor Gla domain motif I - 7
fd; FLEELRKGNLEREC       THRB_MOUSE     48   48
fd; FLEELRKGNLEREC         THRB_RAT     48   48
fd; FLEEVRKGNLEREC       THRB_BOVIN     48   48
fd; FLEEVRKGNLEREC       THRB_HUMAN     47   47

*/
// The following data is provided by the database fragment above
// fc; is the name of the motif  (name)
// fl; is the length of the motif or number of amino acids in each example
// of the motif (size)
// ft; is the information (info)
// fd; FLEELRKGNLEREC  is the motif of length (size) (MotifLineEl.line_of_aas)
// fd; 48 is the start of the motif (MotifLineEl.start)
// fd; 48 is the interval since the last motif (MotifLineEl.interval)]
//
// Another feature of the motif is the fact that a fingerPrint is associated
// with more than one motif so a linked list structure of motifs is the
// best data structure to represent this, the pointer 'next' points at the next motif
// or Null if it is the last motif

class Motif  {

  friend class FingerPrint; //object creation and most manips are private

  typedef MotifLineEl * ptr_MotLinEl;
  typedef Motif * ptr_Motif;
  typedef FrequencyTable * ptr_FrequencyTable;

private:
  Motif( mystring n, const int sz, mystring inf, const int n_codes,
  ptr_Motif p, int motifnum, SubstitutionMatrix * subs);
  ~Motif();
  friend ostream & operator << (ostream & os, const Motif & M);
  void ReturnItem(float &start, float &finish );
  void ReturnItem(float &lowest_start, float &highest_start, float &interval);
  void CalcIntervals();
  void ReturnLen(int &length) { length = size; };
  void CreateFT(int COMPACT);
  void PrintFT();
  void PrintMT() { 
    cout << "mx; " << name << endl 
	 << "mi; " << info << endl
	 << "ml; " << size << endl 
	 << "mc; " << numcodes << endl 
	 << "mn; " << num << endl ;
      };

  mystring name;
  const int size;      // Size of mystring (number of aas in motif)
  int numcodes;  // number of sequences in motif (depth)
  int num; // The motif number
  mystring info;
  SubstitutionMatrix * sm;
  ptr_MotLinEl head;   // pointer to linked list of objects of type
  // MotifLineEl which contain the info
  // stored on each fd; line

  // Stats variables
private:
  float AveSelfScore;
  float StdevSelfScore;
  int StatsON; // Flag variable to ignore stats
  
  int low_start; 
  int high_start; 
  int low_interval; 
  int high_interval;

public:
  void AddItem( mystring line, int st, int Int);
  void insertDistances(int ls, int hs, int li, int hi)
   { low_start=ls; high_start=hs; low_interval=li; high_interval=hi;}
  void ChangeNumCodes(int n_codes) { numcodes = n_codes; }
  void ConstructFT();
  ptr_FrequencyTable ft; // pointer to frequency table of motif data
  ptr_Motif next;  // pointer to next motif in fingerprnt
};



// Constructor initializes variables as described in motif.h
Motif::Motif( mystring n = "\0", const int sz = 0, mystring inf = "\0",
       const int n_codes = 0, ptr_Motif p = 0, int motifnum = 0,
       SubstitutionMatrix * subs = 0)
  : size(sz), numcodes(n_codes), next(p), head(0), num(motifnum),
    AveSelfScore(0), StdevSelfScore(0), ft(0), 
    low_start(0), high_start(0), low_interval(0), high_interval(0),
    sm(subs)
{
  name = n;
  info = inf;
  numcodes = n_codes;
  StatsON = 0;
  // should create ft obj here its data is thrown away in the destructor
  //  ft = new FrequencyTable(size,numcodes);
}


// Destructor throws away used memory
Motif::~Motif()
{
  ptr_MotLinEl q = head;
  if (q != 0) {
    while (q != 0) {
      ptr_MotLinEl p = q;
      q = q->next;
      delete p;
    }
  }
  if (ft) { delete ft; }
}


// Overload of the << for the Motif data type
ostream & operator << (ostream & os, const Motif & M)
{
  cout << "Motif name" << M.name << endl;
  cout << "Motif size= "<< M.size << endl;
  cout << "Number of codes= "<< M.numcodes << endl;
  cout << "Motif Info= "<< M.info << endl;

  MotifLineEl * q = M.head;

  while (q != 0)  {
    q->print_MotifLineEl();
    q = q->next;
  }
  return os;
}


// Add item to array of Motifs
void Motif::AddItem( mystring line, int st, int Int)
{
  ptr_MotLinEl p = new MotifLineEl(line,st,Int,size);
  ptr_MotLinEl q = head;

  if (q==0)
    head = p;
  else {
    while (q->next != 0)
      q = q->next;
    q->next = p;
  }
}



// return average start and end positions stored in the MotifLineEls
void Motif::ReturnItem(float &start, float &finish )
{

  // This function returns the start and end as calculated as
  // start = average(start)
  // end   = average(start) + motif_size
  // this is not the best representation. Standard deviation would be better

  ptr_MotLinEl p = head;  // point at head of list of Motif line elements

  int i = 0;
  while (p != 0) {
    start += p->start;  // add p->start to the summed start
    i++;    // count the number of items in list
    p = p->next;     // move to next item
  }
  start = float ( start /  i );  // average start
  finish = start + size;  // finish = average start + size of motif
}

// An altenative would be to store the highest and lowest values
// the int is irrelevant merely distinguishes the functions

void Motif::ReturnItem(float &lowest_start,
         float &highest_start, float &interval )
{
  ptr_MotLinEl p = head;  // point at head of list of Motif line elements

  lowest_start = p->start;
  highest_start= p->start;

  while (p != 0) {
    lowest_start  = (lowest_start  > p->start )? p->start:lowest_start;
    highest_start = (highest_start < p->start )? p->start:highest_start;
    p = p->next;
  }
  // Hacked on_flag into code to retrieve the interval
  p = head;    // point at head of list of Motif line elements

  int i = 0;
  while (p != 0) {
    interval += p->interval;
    i++;
    p = p->next;
  }
  interval = float ( interval /  i );  // average interval
}

void Motif::CalcIntervals()
{
  if ( low_start==0 && high_start==0 && low_interval==0 && high_interval==0 ) {
    // only if all are zero will they be computed
    ptr_MotLinEl p = head;  // point at head of list of Motif line elements
    
    int inter = p->interval;
    int st    = p->start;

    low_start = st;
    high_start = st;
    low_interval = inter;
    high_interval = inter;
    
    while  (p != 0) {
      inter = p->interval;
      st = p->start;
      
      low_start     = (low_start  > st )? st:low_start;
      high_start    = (high_start < st )? st:high_start;
      low_interval  = (low_interval  > inter )? inter:low_interval;
      high_interval = (high_interval < inter )? inter:high_interval;
      
      p = p->next;
    }
  }
}

// PrintFT calls the frequencytable function PrintTable pointed at by ft
void Motif::PrintFT()
{
  ft->PrintTable();
}

void Motif::ConstructFT() {
  ft = new FrequencyTable(size,numcodes,sm,threshold);
}

// ### This function can only function on the original data.
// Create a frequency table for the motif and asign the pointer ft to it
// Insert all list items in the Motif to populate the frequency matrix
// Compute Profile 
void Motif::CreateFT(int COMPACT)
{
  ft = new FrequencyTable(size,numcodes,sm,threshold);
  ptr_MotLinEl q = head;

  while ( q != 0) {
    for (int char_indx=0 ;char_indx < size; char_indx++)
      ft->InsertListItem ( q->line_of_aas[char_indx], char_indx );
    q= q->next;
  }

  CalcIntervals();

  if (COMPACT) {
    PrintMT();
    cout << "ls; " << low_start << endl;
    cout << "hs; " << high_start << endl;
    cout << "li; " << low_interval << endl;
    cout << "hi; " << high_interval << endl;
  }

  ft->ComputeProfile(COMPACT);
}



/***************************************************************************/
// FingerPrint data structure represents the important data in the Fingerprint
// entry of the prints database which is of the form seen below
/*
      gc; GLABLOOD
      gn; COMPOUND(3)

      si; SUMMARY INFORMATION
      si; -------------------
      sd;   26 codes involving 3 elements
      sd;    1 codes involving 2 elements
      bb;

      fpname = "GLABLOOD"
      num_of_motifs = 3
      num_of_codes = 26

// Plus a linked list of motifs
// head is a pointer to the linked list of Motifs


*/


class FingerPrint  {

//friend class PrintsDbase;
typedef Motif * ptr_Motif;

public:
  FingerPrint(mystring n, const int nm, const int nc, mystring ac, 
       mystring fpi, SubstitutionMatrix * subs);
  ~FingerPrint();
  void AddMotif(mystring n, int sz, mystring inf, int num_of_codes, int motif_num );
  void PrintFinger();
  void CreateFrequencyTables (int COMPACT);
  void ScoreSequence(mystring seq, FpScore * fpscore);
  friend ostream & operator << (ostream & os, const FingerPrint & Fp);

  ptr_Motif head;
  FingerPrint * next;
public:
  mystring fpname;
  const int num_of_motifs;
  const int num_of_codes;
  mystring accession;
  mystring fpinfo;
  SubstitutionMatrix * sm;
};


// Construct the fingerprint
FingerPrint::FingerPrint(mystring n, const int nm, const int nc,mystring ac, 
    mystring fpi, SubstitutionMatrix * subs )
      : num_of_motifs(nm), num_of_codes(nc), fpname(n), 
 accession(ac), fpinfo(fpi), sm(subs), next(0)
{
  // strncpy (fpname, n ,14);
  head = 0;
}


// Deconstruct the fingerprint
FingerPrint::~FingerPrint()
{
  ptr_Motif q = head;
  
  if (q != 0)
    {
      while (q != 0)
 {
   ptr_Motif p = q;
   q = q -> next;
   delete p;
 }
    }
  // head == 0  no structures to delete
}


// Add a motif onto the linked list of motifs
void FingerPrint::AddMotif(mystring name, int size, mystring info,
      int num_of_codes, int motif_num)
{
  ptr_Motif p = new Motif(name,size,info,num_of_codes,0,motif_num,sm);
  ptr_Motif q = head;
    
  if (q == 0)
    head = p;
  else
    {
      while (q->next != 0)
	q=q->next;
      q->next = p;
    }
}


// cycle through the linked list of motifs and create the frequency tables
void FingerPrint::CreateFrequencyTables (int COMPACT)
{
  if (COMPACT) {
    cout << "gc; " << fpname << endl;
    cout << "gx; " << accession << endl;
    cout << "gn; " << num_of_motifs << endl;
    cout << "gi; " << fpinfo << endl;
    cout << "gm; " << num_of_codes << endl;
  }

  ptr_Motif q = head;
  
  if (q != 0)    {
    while (q != 0) {
      q->CreateFT(COMPACT);
      q = q-> next;
    }
  }
}


// Provide a best score for the analysis of a sequence against
// the frequency table

void FingerPrint::ScoreSequence(mystring seq, FpScore * fpscore)
{
  ptr_Motif q = head;

  mystring consensus = "";

  if (q != 0) {
    while (q != 0) {// cycle around motifs
      // get length data from the Motif
      int length = 0;
      q->ReturnLen(length);

      // Stop unecessary calling of consensus function
      if (CONSENSUS) {
	consensus = q->ft->Getcons();
      } else {
	consensus = "";
      }

      MotifScore *
	p_motif = fpscore->AddMotif(q->name, q->low_start, q->high_start,
        q->low_interval,q->high_interval,
        length, q->num, q->AveSelfScore, 
        q->StdevSelfScore,consensus);

      // Create a motifscore object and hand on its pointer to DigestSequence 
      if (GRAPH) { 
	//In a debugging mode I want to digest the sequence with the unweighted
	//function, even though the GRAPH flag is set
	if (GRAPHDEBUG) {
	  //cout << "GRAPHDEBUG" << endl;
	  q->ft->DigestSequenceUnweighted(seq, p_motif); 
	} else {
	  //cout << "GRAPH" << endl;
	  q->ft->DigestSequenceGraph(seq, p_motif);
	}
      } else { 
	q->ft->DigestSequence(seq, p_motif); 
      } // Once a motif has been digested and scored 
      // the function cycles onto the next in the linked list 
      q=q->next; 
    } // No more Motifs 
  } // No Motifs at all 
}


// Print the FingerPrint data structure
void FingerPrint::PrintFinger()
{
  cerr << "Print FingerPrint" << endl;
  cout << endl;
  cout << "FINGERPRINT" << endl
       << "===========" << endl;
  cout << "FingerPrint name= "<< fpname << endl;
  cout << "Number of motifs= "<< num_of_motifs << endl;
  cout << "Number of codes=  "<< num_of_codes << endl;
  cout << "Accession code= "<< accession << endl;
  cout << "Fingerprint Info= "<< fpinfo << endl;
  
  
  ptr_Motif q = head;
  
  if (q != 0) {
    while (q != 0){
      cerr << "Print New Motif " << q->name << endl;
      cout << endl << (*q) << endl;
      q->PrintFT();
      q = q->next;
    }
  }

}


ostream & operator << (ostream & os, const FingerPrint & Fp)
{
  cout << Fp.fpname;
  return os;
}


/***************************************************************************/
// Prints Database
//
/*

This object takes one parameter the name of the file in which the data
is stored; this is the prints database flat file

*/


class PrintsDbase {
public:

  PrintsDbase(const char * name, mystring seqfilename, 
    SubstitutionMatrix * subssm);
  ~PrintsDbase();
  void ParsePrints(int printing_flag, int UseFasta, float eval, int COMPACT);
  void ParseBlocks(int printing_flag, int UseFasta, int COMPACT);
  void ParseCompact(int want_printing, int UseFasta, float eval);
private:
  char * file_name;
  const mystring sequencefilename;
  FingerPrint * FingerPrintHead;

  FingerPrint * AddFingerPrint (mystring fpname,
					     const int num_mots,
					     const int num_codes,
					     mystring accession,
					     mystring fpinfo,
					     SubstitutionMatrix * subs);
  FingerPrint * AddFingerPrint (mystring fpname,
					     const int num_mots,
					     const int num_codes,
					     mystring accession,
					     mystring fpinfo,
					     SubstitutionMatrix * subs,
					     int COMPACT);
  void FindFingerPrintMatches(float evalthresh, int UseFasta);
  int  GetNumcodes(char * b);
  int  GetNumMotifs (char * b);

  void GetMTname(char * b, char * name);
  int  GetFPname(char * b, char * name);
  int  GetAcCode(char * b, char * name);
  int  GetFpInfo(char * b, char * name);
  int  GetNum   (char * buffer, int offset, int chars);
  void GetString(char * buffer, char * word, int offset, int length);
  //  int PrintsDbase::GetStringDelim(char * buffer, char * word, int offset, char delim, int maxlen);
  inline int GetStringDelim(char * buffer, char * word, int offset, char delim, int maxlen){
    int j=0;
    for (int i=offset ; i < maxlen + offset; i++) {
      if (buffer[i] == delim) {
	word[j]= '\0'; 
	return i+1;
      } else { word[j++] = buffer[i]; }
    }
  }
  int starts(const char a, const char b, char * buf);
  SubstitutionMatrix * sm;  
  void ERROR(char * s, char * s1 );
  void ERROR(char * s, mystring s1 );
  void ERROR(mystring  s, mystring s1 );
  void ERROR(mystring  s, char * s1 );

};


// Constructor

PrintsDbase::PrintsDbase(const char * name, mystring seqfilename, 
    SubstitutionMatrix * subssm)
:  sequencefilename(seqfilename), FingerPrintHead(0), sm(subssm)
{
   file_name = new char [strlen(name)+1];
   strcpy(file_name,name);
}

// Deconstructor of prints database
PrintsDbase::~PrintsDbase()
{
    delete [] file_name;
}


FingerPrint * PrintsDbase::AddFingerPrint (mystring fpname,
        const int num_mots,
        const int num_codes,
        mystring accession,
        mystring fpinfo, 
        SubstitutionMatrix * subs)
{
  FingerPrint* p= new FingerPrint(fpname,num_mots,num_codes,accession,fpinfo,sm);
  if (!p) { cerr << "Memory alloc error in AddFingerPrint" << endl;exit(0);}

  FingerPrint* q= FingerPrintHead;

  if (q==0) {
    FingerPrintHead = p;
  }else {
    while (q->next != 0) {
      q = q->next;
    }
    q->next = p;
  }
  return p;
}

FingerPrint * PrintsDbase::AddFingerPrint (mystring fpname,
        const int num_mots,
        const int num_codes,
        mystring accession,
        mystring fpinfo, 
        SubstitutionMatrix * subs, int COMPACT)
{
  FingerPrint* p= new FingerPrint(fpname,num_mots,num_codes,accession,fpinfo,sm);
  if (!p) { cerr << "Memory alloc error in AddFingerPrint" << endl;exit(0);}

  FingerPrint* q= FingerPrintHead;

  if (q==0) {
    FingerPrintHead = p;
  }else {
    while (q->next != 0) {
      q = q->next;
    }
    q->next = p;
  }
  return p;
}


void PrintsDbase::ERROR(char * s= "", char * s1="" )
{
  cerr << s << ' ' << s1 <<'\n';
  exit(1);
}

void PrintsDbase::ERROR(char * s= "", mystring s1="" )
{
  cerr << s << ' ' << s1 <<'\n';
  exit(1);
}

void PrintsDbase::ERROR(mystring s= "", mystring s1="" )
{
  cerr << s << ' ' << s1 <<'\n';
  exit(1);
}

void PrintsDbase::ERROR(mystring s= "", char * s1="" )
{
  cerr << s << ' ' << s1 <<'\n';
  exit(1);
}

// Identify the Name discriminator gc and extract the name of the fingerprint
int PrintsDbase::GetFPname(char * b, char * name)
{

  if ((b[0] == 'g') && (b[1] == 'c'))
    {
      for (int i = 0; i < 14; i++)
	name[i] = b[i+4];
      return 1; //True
    }
  else
    name[0] = '\0';
  return 0; //False
}

// Identify the Accession Code discriminator gx and extract
int PrintsDbase::GetAcCode(char * b, char * name)
{

  if ((b[0] == 'g') && (b[1] == 'x'))
    {
      for (int i = 0; i < 14; i++)
	name[i] = b[i+4];
      return 1; //True
    }
  else
    name[0] = '\0';
  return 0; //False
}

// Identify the Fingerprint title discriminator gt and extract
int PrintsDbase::GetFpInfo(char * b, char * name)
{

  if ((b[0] == 'g') && (b[1] == 't'))
    {
      int i= 0;
      while (b[i] != '\n' && i < strlen(b)) {
	name[i] = b[i+4];
	i++;
      } name[i] = '\0';
      return 1; //True
    }
  else
    name[0] = '\0';
  return 0; //False
}

// Identify the Motif name discrimiator fc and extract the motif name
void PrintsDbase::GetMTname(char * b, char * name )
{
  int i;
  for (i = 0; i < 15; i++)
    name[i] = b[i+4];
  name[i] = '\0';
}



// identify the number of motifs discrimintor and extract the number
// convert the character representation of the number of motifs into an integer
// and return this value
// If 0 is returned the calling function cycles until this line is found
int PrintsDbase::GetNumMotifs (char * b)
{
  char numb[3];

  if ((b[0] == 'g') && (b[1] == 'n'))  {
    if (b[4] == 'C')  {
      for (int i = 4; i < 15; i++)  {
 if ((b[i]=='(') && (b[i+2]==')'))   {
   numb[0] = b[i+1];
   numb[1] = '\0';
 }
 if ((b[i]=='(') && (b[i+2]!=')'))   {
   numb[0] = b[i+1];
   numb[1] = b[i+2];
   numb[2] = '\0';
 }
      }
      return (atoi (numb));  // Conver character mystring to int
    }
    else
      return 1; // Not a compound fingerprint thus only one motif
  }
  return 0; // The motif number line has not been found
}


int PrintsDbase::GetNum(char * b, int offset, int length) {
  char numb[6];
  int i;
  for (i=offset; i < length + offset; i++) {
    numb[i-offset] = (isdigit(b[i]) || b[i]=='-') ? b[i] : '\0';
  }
  numb[i-offset] = '\0';
  return atoi(numb);
}

void PrintsDbase::GetString(char * buffer, char * word, int offset, int length) {
  int i;
  for (i=offset ; i < length + offset; i++) {
    word[i-offset] = (buffer[i] == ' ' && buffer[i+1] == ' ') ? '\0' : buffer[i];
  }
  word[i-offset] = '\0';
}




// Identfy the number of codes discriminator sd and return the integer
int PrintsDbase::GetNumcodes(char * b)
{
  char numb[6];
  if (b[0] == 's' && b[1] == 'd')
    {
      int i = 0;
      for (i = 0; i < 5; i++)
 {
   numb[i] = isdigit(b[i+4]) ? b[i+4] : ' ';
 }
      b[i] = '\0';
      return atoi(numb);
    }
  else
    return 0;
}

int PrintsDbase::starts(const char a, const char b, char * buf) {
  if (buf[0] == a   && buf[1] == b   &&
      buf[2] == ' ' && buf[3] == ' ' && buf[4] == ' ') {
    return 1;
  } else {
    return 0;
  }

}



// Constructor of prints data base
/*

Strips irrelevant data from the prints database flat file and calls the
constructors of the FingerPrint and Motif objects

All data for the FingerPrint, Motif and MotifLine Element is stripped
from the text file and provided to the relevant objects

Finally the scoring occurs

*/

void PrintsDbase::FindFingerPrintMatches(float evalthreshold, int UseFasta){
  // Start the sequence database reading
  SequenceDatabase SeqDB;
  DatabaseValues * DBV = new DatabaseValues(); // Intialize database size variable holder

  if (UseFasta) {
    if (!SeqDB.ReadFasta(sequencefilename))
      ERROR("Sequence file problem", sequencefilename);
  } else {
    if (!SeqDB.ReadSequence(sequencefilename))
      ERROR("Sequence file problem", sequencefilename);
  }
  
  Sequence * seqp = SeqDB.head;

  while (seqp != 0) {
    FingerPrint * fingp = FingerPrintHead;
    seqp->fppoint = seqp->scorelist.Schead;
    if (shuffle) { seqp->Shuffle();}
    // the FPScore pointer fppoint has to be reset to the head of the list

    while ( fingp != 0) {
      seqp->fppoint = seqp->scorelist.AddFp(fingp->fpname,
					    fingp->num_of_motifs,
					    fingp->accession,
					    fingp->fpinfo);
      
      fingp->ScoreSequence(seqp->Getresidue_list(), seqp->fppoint);
      // INCREMENT fingp and fppoint in unison
      seqp->fppoint=seqp->fppoint->next;
      fingp=fingp->next;
    }
    

    seqp->scorelist.CleanUp();
    //cleanup- delete all motifs and fingerprints

    cout << "Sn; " << seqp->name << endl;
    cout << "Si; " << seqp->info << endl;

    FpScore * FpS = seqp->scorelist.Schead;

    PathFinderList * pathFL = new PathFinderList(double(evalthreshold));

    // Set the Database Values, only pathFL can do this.
    pathFL->setDBVs(DBV, DBLEN, DBSEQS, UsepDmethod);
    //DBV->show(); //Present the values

    // create the PathFinderList object

    while (FpS != 0) {
      PathFinder* pf = pathFL->CreatePathFinder(FpS,
						PvalueThreshold,
						DistanceVariance,
						GRAPH);
      FpS = FpS->next;
    }

    if(!GRAPH) { //If the graphscan option is indicated dont destroy empty paths  
      pathFL->WeedOutEmptyPathFinders();
      pathFL->Sort();
    }


    // Determin output configuration based on binary representation of the options number
    int option_score = options;

    if (0 < option_score && option_score < 16) {
      
      option_score = option_score -8;
      if (option_score >= 0)
	pathFL->PrintOutFirstList(julian_selley);
      else
	option_score = option_score +8;
      
      option_score = option_score -4;
      if (option_score >= 0)
	pathFL->PrintOutSecondList(RESTRICT);
      else
	option_score = option_score +4;

      option_score = option_score -2;
      if (option_score >= 0)
	pathFL->PrintOutThirdList(RESTRICT);
      else
	option_score = option_score +2;
      
      option_score = option_score -1;
      if (option_score >= 0 && CONSENSUS) {
	pathFL->PrintOutAlign(seqp, RESTRICT);
      }
      else
	option_score = option_score +1;
    } else { 
      // To print out the MxM list provide a -o value greater than 16
      pathFL->PrintOutMxMlist();
    }
    // delete pathFL;

    Sequence * todelete = seqp;
    seqp=seqp->next;
    delete todelete;
  } // finished iterating around sequences
}


void PrintsDbase::ParsePrints(int want_printing, int UseFasta, float evalthreshold,int COMPACT)
{
  // THIS FUNCTION PARSES THE PRINTS DATABASE FILE
  // WITH THE compact FLAG SET TO TRUE THIS FUNCTION WILL WRITE
  // TO STANDARD OUTPUT A COMPACT VERSION OF THE DATABASE

  // Open file input stream
  ifstream from(file_name);
  const int BUFFSIZE = 1024;
  const int NAMESIZE = 16;
  const int MAXMOTSIZE = 40;

  char buffer[BUFFSIZE];   // Store each line in temporary storage
  char fpname[NAMESIZE];   // Store each FingerPrint name
  char mtname[NAMESIZE];   // Store each Motif name
  char mot_inf[BUFFSIZE];  // Store Motif information
  char motif[MAXMOTSIZE];  // Store line of amino acids
  char accession[NAMESIZE];// Store the Accession code
  char fpinfo[BUFFSIZE];   // Score the fingerprint info line

  char numb[4];   // Store numbers before conversion to int atoi()
  char start[5];  // Store start before atoi()
  char inter[5];  // Store inter before atoi()

  int nummots = 0;  // Number of Motifs
  int numcodes = 0;  // Number of Codes
  int motsize = 0;  // Size (length) of individual motif (feature)

  // Setup parameters for st and int detection (within each fd line)
  int mot_start_pos       = 50;
  int mot_inter_pos       = 55;
  const int char_len      = 5;
  const int mot_max_size  = 30;


  fpname[0]='\0';      mtname[0]='\0';// Insert mystring terminator at begining
  // of mystring to intialize

  if (!from) ERROR("cannot open input file",file_name);
  if (COMPACT) {
    cout << "## Compact version of " << file_name << endl;
  }

  int DEBUG = 0;


  // Big loop Whole FingerPrint - loops round once for each fingerprint
  while (!from.eof() && from.getline(buffer,BUFFSIZE)) {
    if (buffer[0] == '#') {
      ERROR("The database looks like the Compact database\n",
     "Use the -c option to read the compact version\n");
    }
    // New FingerPrint hence reinitialize all relevant data
    nummots =0; numcodes =0; fpname[0]='\0';
    // New addition of accession and fpinfo
    accession[0]='\0'; fpinfo[0]='\0';

    // looking for gc;
    int namegot = GetFPname(buffer,fpname);
    // dont enter this loop unless the above function fails to find
    // under perfect conditions this should never enter

    while ((namegot == 0) && !from.eof() && from.getline(buffer,BUFFSIZE)) {
      namegot = GetFPname(buffer,fpname);
    }

    // looking for gx;
    int foundaccession = 0;
    // always ask a lazy evaluator to check if found first then you
    // dont get the problem of reading the next line for no reason
    while (!foundaccession && !from.eof() && from.getline(buffer,BUFFSIZE)) {
      foundaccession = GetAcCode(buffer, accession);
    }

    // small loop until gn; is found
    while (!nummots && !from.eof() && from.getline(buffer,BUFFSIZE) ){
      nummots = GetNumMotifs(buffer);
    }

    int foundfpinfo = 0;
    // small loop until gt; is found
    while (!foundfpinfo && !from.eof() && from.getline(buffer,BUFFSIZE) ){
      foundfpinfo = GetFpInfo(buffer, fpinfo);
    }

    // small loop only until summary sd; is found
    while (!from.eof() && from.getline(buffer,BUFFSIZE) && (numcodes == 0)){
      numcodes = GetNumcodes(buffer);
    }

    // Create fingerprint object here then create motifs
    // The old fingerprint object took fpname,nummots,numcodes
    // The New as of 060597 will add accession and fpinfo
    if (DEBUG) {
      cerr <<  fpname << " " 
	   << nummots << " " 
	   << numcodes << " " 
	   << accession << " " 
	   << fpinfo << endl;
    }
    
    FingerPrint* fp = AddFingerPrint(fpname,nummots,numcodes,accession,fpinfo,sm);



    // medium loop round motifs (big 'for' loop though!)
    for (int g = 0;g < nummots;g++) {
      // New motif so intialize all relevant Motif data
      mtname[0]= '\0'; motsize =0; mot_inf[0] = '\0'; motif[0]='\0';

      while (!from.eof() && from.getline(buffer,BUFFSIZE)
      && (mtname[0] == '\0')) {
	// GetMTname is called 137325 times is this an overhead!
	if ((buffer[0] == 'f') && (buffer[1] == 'c')) {
	  GetMTname(buffer,mtname);
	}
	else
	  mtname[0] = '\0';
	
      }
      
      if (buffer[0] == 'f' && buffer[1] == 'l')    {
	numb[0] = buffer[4];
	numb[1] = buffer[5];
	numb[2] = '\0';
	motsize = atoi(numb);
      }
      
      // each time this line is encountered a line is consumed
      // from the input file into buffer, this is done repeatedly,
      // each time a check is made to ensure the file hasn't ended

      if (0) { cerr << buffer << endl; }
      if (from.eof() || !from.getline(buffer,BUFFSIZE))
	ERROR("End of input","HERE!");
      
      
      if (buffer[0] == 'f' && buffer[1] == 't') {
	//strncpy(mot_inf, buffer, 60);
	int i= 0;
	while (buffer[i] != '\n' && i < strlen(buffer)) {
	  mot_inf[i] = buffer[i+4];
	  i++;
	} mot_inf[i] = '\0';
      }
      
      if (from.eof() || !from.getline(buffer,BUFFSIZE))
	ERROR("End of input","");
      
      int motnum = g+1;  // g is the counter of the motifs hence starts at 0

      if (DEBUG) {
	cerr << mtname << " " 
	     << motsize<< " "
	     << mot_inf<< " "
	     << numcodes << " " 
	     << motnum << endl;
      }
    

      // create each motif here - adds motif to fingerprints linked list
      fp->AddMotif(mtname, motsize, mot_inf, numcodes, motnum);
      
      // Loop around one motif, however ensure before
      // that correct pointer is looked at
      Motif * q = fp->head;
      while (q->next != 0)
	q=q->next;
       
      int difference = motsize - mot_max_size;
            
      if ( difference > 0 ) {
	mot_start_pos += difference;
	mot_inter_pos += difference;
      }

      int j = 0;
      int k = 0;
      int l = 0;
      
      while (buffer[0] == 'f' && buffer[1] == 'd') {
      int pos = mot_max_size + 4 + 1;
      if ( difference > 0 ) {
	pos += difference;
      }	

	start[0] = '\0'; inter[0] = '\0'; motif[0] = '\0';
	
	// Loop around length of motif then add mystring terminator
	for (k = 0; k < motsize; k++)  {
	  motif[k]=buffer[k+4];
	}
	motif[k] = '\0';

	// consume whitespace after motif
	while (buffer[pos] == ' ' || buffer[pos] == '\t') {
	  pos++;
	}
	// reached the sequence_name, consume non-whitespace
	while (buffer[pos] != ' ' && buffer[pos] != '\t') {
	  pos++;
	}
	
	int str_len = 5;
	mot_start_pos = pos + 3;
	mot_inter_pos = pos + 9;
		 
	// loop around st and int, mystring terminate and convert to ints
	for (l = 0; l < str_len; l++)  {
	  start[l] = buffer[l + mot_start_pos];
	  inter[l] = buffer[l + mot_inter_pos];
	}
	start[l] = '\0'; inter[l] = '\0';
	
	int st = atoi(start); int Int = atoi(inter);
	
	// Add item to linked list of MotifLineElements
	q->AddItem(motif,st,Int);
	
	if (from.eof() || !from.getline(buffer,BUFFSIZE))
	  ERROR("End of input","");

	j++;  // increment counter of elements
	
      } //end of motif
      
      if (numcodes != j) {
	q->ChangeNumCodes(j); //this should reset the numcodes
	//cerr << mtname << " numcodes=" << numcodes << " & J=" << j << endl;
      }

      // check here that there is an end of file (EOF)
      if (from.eof()) ERROR("End of file","");
      
    } // end of one fingerprint all motifs extracted
    
    // after finger print is complete then create frequency tables
    // for each motif
    
    fp->CreateFrequencyTables(COMPACT);
 
    // For the test model print the whole data structure
    // only when command line param is -p
    
    if (want_printing)
      fp->PrintFinger();
    
    // And score against a sequence and produce some output
    
    // ScoreSequence is passed the sequence and the fingerprint
    // Score object's pointer
    // the list of sequence objects id iterated round
    
    // end of scope of fp (the fingerprint datastructure )
  } // Next Fingerprint or end of PRINTS database
  // end of whole data base file
  
  if (COMPACT) { // Dont go any furthur if only parsing
    exit(0);
  }

  FindFingerPrintMatches(evalthreshold,UseFasta);

  return;
} // end of PrintsDbase Parse




void PrintsDbase::ParseCompact(int want_printing, int UseFasta, float evalthreshold)
{
  // THIS FUNCTION PARSES THE PRINTS DATABASE FILE
  // WITH THE FOLLOWING FLAG SET TO TRUE THIS FUNCTION WILL WRITE
  // TO STANDARD OUTPUT A COMPACT VERSION OF THE DATABASE
  // int COMPACT // sent as a parameter
  // Open file input stream
  ifstream from(file_name);
  const int BUFFSIZE = 512;
  const int NAMESIZE = 16;
  const int MAXMOTSIZE = 40;

  char buffer[BUFFSIZE];   // Store each line in temporary storage
  char fpname[NAMESIZE];   // Store each FingerPrint name
  char mtname[NAMESIZE];   // Store each Motif name
  char mot_inf[BUFFSIZE];  // Store Motif information
  char motif[MAXMOTSIZE];  // Store line of amino acids
  char accession[NAMESIZE];//Store the Accession code
  char fpinfo[BUFFSIZE];   //Score the fingerprint info line

  char numb[4];   // Store numbers before conversion to int atoi()
  char start[5];  // Store start before atoi()
  char inter[5];  // Store inter before atoi()

  int nummots = 0;  // Number of Motifs
  int numcodes = 0;  // Number of Codes
  int motsize = 0;  // Size (length) of individual motif (feature)

  fpname[0]='\0';      mtname[0]='\0';// Insert mystring terminator at begining
  // of mystring to intialize

  if (!from) ERROR("cannot open input file",file_name);
  from.getline(buffer,BUFFSIZE);
  if (buffer[0] != '#') {
    ERROR("The Compact database seems to be incorrectly formatted","");
  } 


  // Big loop Whole FingerPrint - loops round once for each fingerprint
  while (!from.eof() && from.getline(buffer,BUFFSIZE)) {

    nummots =0; numcodes =0; 
    fpname[0]='\0'; accession[0]='\0'; fpinfo[0]='\0';

    GetString(buffer,fpname,4,14);

    from.getline(buffer,BUFFSIZE);
    GetString(buffer,accession,4,8);

    from.getline(buffer,BUFFSIZE);
    nummots = GetNum(buffer,4,4);

    from.getline(buffer,BUFFSIZE);
    GetString(buffer,fpinfo,4,100);
    
    from.getline(buffer,BUFFSIZE);
    numcodes = GetNum(buffer,4,4);

    // Create fingerprint object here then create motifs
    FingerPrint* fp = AddFingerPrint(fpname,nummots,numcodes,accession,fpinfo,sm);

    for (int g = 0;g < nummots;g++) {
      // New motif so intialize all relevant Motif data
      mtname[0]= '\0'; motsize =0; mot_inf[0] = '\0'; motif[0]='\0';

      from.getline(buffer,BUFFSIZE);
      GetString(buffer,mtname,4,16); // mx
      from.getline(buffer,BUFFSIZE);
      GetString(buffer,mot_inf,8,100); // mi
      from.getline(buffer,BUFFSIZE);
      motsize = GetNum(buffer,4,4); // ml
      from.getline(buffer,BUFFSIZE); // mc
      numcodes = GetNum(buffer,4,4);
      
      int motnum = g+1;  // g is the counter of the motifs hence starts at 0
      from.getline(buffer,BUFFSIZE); // mn
      motnum = GetNum(buffer,4,4);
      // create each motif here - adds motif to fingerprints linked list
      fp->AddMotif(mtname, motsize, mot_inf, numcodes, motnum);
      // Loop around one motif, however ensure before
      // that correct pointer is looked at
      Motif * motifptr = fp->head;
      while (motifptr->next != 0) {
	motifptr = motifptr->next;
      }
      //###################################################################
      //using the motifptr place the ls,hs,li,hi values
      // and parse the profile
      // then collect the K and l vals
      // Then the motif is complete!!!!! back to top!!!
      //###################################################################

      from.getline(buffer,BUFFSIZE); // ls
      int ls = GetNum(buffer,4,5);
      from.getline(buffer,BUFFSIZE); // hs
      int hs = GetNum(buffer,4,5);
      from.getline(buffer,BUFFSIZE); // li
      int li = GetNum(buffer,4,5);
      from.getline(buffer,BUFFSIZE); // hi
      int hi = GetNum(buffer,4,5);
      // place ls,hs,li,hi
      motifptr->insertDistances(ls,hs,li,hi);
      motifptr->ConstructFT(); // construct the FT so profile can be added
      
      // parse frequency table
      //from.getline(buffer,BUFFSIZE); // profile row *,0,1 ....

      const int maxlen = 5;
      int wordpos =0;

      static const int ALPHABETSIZE = 26;
      for (int i=0; i < ALPHABETSIZE; i++) {
	from.getline(buffer,BUFFSIZE); // profile row
	int offset = 2;
	char numb[15];
	int count = 0;
	while (count < motsize) {
	  //	  offset = GetStringDelim(buffer,numb,offset,',',5);
	  // function calls to GetStringDelim are removed due to performance considerations
	  wordpos =0;
	  int cnt;
	  for (cnt=offset ; cnt < maxlen + offset; cnt++) {
	    // add buffer pos cnt to numb pos wordpos and post-increment wordpos
	    numb[wordpos++] = buffer[cnt]; 
	  }
	  numb[wordpos] = '\0';
	  offset = cnt;

	  motifptr->ft->insertFreqElement(count,i,atoi(numb));
	  count++;
	}
      }

      // parse profile
      //from.getline(buffer,BUFFSIZE); // profile row *,0,1 ....
      
      for (int i=0; i < ALPHABETSIZE; i++) {
	from.getline(buffer,BUFFSIZE); // profile row
	int offset = 2;
	char numb[10];
	int count = 0;
	
	while (count < motsize) {
	  //offset = GetStringDelim(buffer,numb,offset,',',5);
	  
	  wordpos =0;
	  int cnt;
	  for (cnt=offset ; cnt < maxlen + offset; cnt++) {
	    // add buffer pos cnt to numb pos wordpos and post-increment wordpos
	    numb[wordpos++] = buffer[cnt]; 
	  }
	  numb[wordpos] = '\0';
	  offset = cnt;
	  
	  motifptr->ft->insertProfileElement(count,i,atoi(numb));
	  count++;
	} 
      }

      // get K and lambda
      char altschulK[15];
      from.getline(buffer,BUFFSIZE); // KK line
      GetString(buffer,altschulK,4,15);
      motifptr->ft->SetK(atof(altschulK));
      
      char altschulL[15];
      from.getline(buffer,BUFFSIZE); // ll line
      GetString(buffer,altschulL,4,15);
      motifptr->ft->Setl(atof(altschulL));
    } //end of motif
 
    // after finger print is complete then create frequency tables
    // for each motif

    //    fp->CreateFrequencyTables(0);
 
    // For the test model print the whole data structure
    // only when command line param is -p

    if (want_printing)
      fp->PrintFinger();

    // And score against a sequence and produce some output

    // ScoreSequence is passed the sequence and the fingerprint
    // Score object's pointer
    // the list of sequence objects id iterated round

    // end of scope of fp (the fingerprint datastructure )
  } // Next Fingerprint or end of PRINTS database
  // end of whole data base file

  FindFingerPrintMatches(evalthreshold, UseFasta);

  return;
} // end of Analyse for compact dbase (Pval)



void PrintsDbase::ParseBlocks(int want_printing, int UseFasta, int COMPACT){
  // THIS FUNCTION PARSES THE BLOCKS DATABASE FILE
  // WITH THE FOLLOWING FLAG SET TO TRUE THIS FUNCTION WILL WRITE
  // TO STANDARD OUTPUT A COMPACT VERSION OF THE DATABASE
  // Open file input stream
  ifstream from(file_name);
  const int BUFFSIZE = 512;
  const int NAMESIZE = 16;
  const int MAXMOTSIZE = 100;
  
  char buffer[BUFFSIZE];   // Store each line in temporary storage

  mystring fpname;   // Store each FingerPrint name
  mystring mtname;   // Store each Motif name
  mystring motinf;   // Store Motif information
  mystring motif;    // Store line of amino acids
  mystring accession;// Store the Accession code
  mystring fpinf;    // Score the fingerprint info line
  mystring oldfpname;// remember the name of the previous fingerprint
  
  int nummots = 0;   // Number of Motifs
  int depth = 0;     // Number of Codes
  int width = 0;     // Size (width) of individual 'block' (feature)
  int motnum = 1;

  int lint = 0;
  int hint = 0;

  int start = 0;
  int startn_1 = 0;
  int counts = 0;

  int got_de = 0; // Flag to stop needless reading of the DE line
  int  encountered_end_of_header = 0;
  int count_data_lines = 0;

  FingerPrint* fp;
  Motif * q;

  if (!from) ERROR("cannot open input file",file_name);
  if (COMPACT) {
    cout << "## Compact version of " << file_name << endl;
  }

  int DEBUG = 0;

  while (!from.eof() && from.getline(buffer,BUFFSIZE)) {

    if (DEBUG) {  cout << buffer << endl; }

    if (buffer[3] == '=' && buffer[75] == '=') {
      if (DEBUG) {  cout << "-----DN  encountered_end_of_header" << endl;}
      encountered_end_of_header = 1;
    } else if (buffer[0] == '\0' || 
    	(!encountered_end_of_header && 
	 buffer[0] == ' ' && buffer[1] == ' ' && buffer[2] == ' ')) {
      // deal with blank line, i.e. Do nothing
      if (DEBUG) {  cout << "----DN " << endl; }
    }
    else if (starts('I','D',buffer)) {
      // deal with ID line
      // Contains Family name and the word block
      //ID   PYRIDINE_REDOX_1; BLOCK
      //cout << "ID   line: " << buffer << endl;
      
      int i =5; fpname[0] = '\0'; 
      while (buffer[i] != ';') {
	fpname += buffer[i];
	i++;
      }

      if (fpname != oldfpname) { got_de=0;}
      motnum=1;
      // different fingerprint so look at the de line this time
      
      if (DEBUG) {  
	cout << "**FN " << fpname << endl;
      }
      fpinf = "";
      // fpname identified
    }
    else if (starts('G','N',buffer)) {
      // deal with GN line (added by phil, with pre-parser)
      //GN   7;
      int i=5; mystring gn = "";
      while (buffer[i] != ';') {
	gn += buffer[i];
	i++;
      }
      nummots = atoi(gn.peek());
    }
    else if (starts('A','C',buffer)) {
      // deal with AC line
      //AC   BL00076D; distance from previous block=(40,256)

      int i =5; accession[0] = '\0'; 
      while (buffer[i] != ';') {
	accession += buffer[i];
	i++;
      }

      // Skip to ( 
      while (buffer[i] != '(') { i++; } i++; // skip '('

      // take chars to ,
      mystring lint_s;
      while (buffer[i] != ',') {
	lint_s += buffer[i];
	i++;
      } i++; // skip ','

      // take chars to )
      mystring hint_s;
      while (buffer[i] != ')') {
	hint_s += buffer[i];
	i++;
      }

      hint = atoi(hint_s.peek());
      lint = atoi(lint_s.peek());

      // accession, lint and hint 
      if (DEBUG) {  
	cout << "**AC " << accession << endl; 
	cout << "**HI " << hint << endl; 
	cout << "**LI " << lint << endl; 
	count_data_lines = 0;
      }
    }
    else if (starts('D','E',buffer)) {
      // deal with DE line
      //DE   Pyridine nucleotide-disulphide oxidoreductases class-I.
      if (!got_de) {
	int i=5;
	while (buffer[i] != '.' && buffer[i] != '\n') {
	  fpinf += buffer[i];
	  i++;
	}
	got_de = 1; // true
      }
      // fpinf
      if (DEBUG) {  
	cout << "**DE " << fpinf << endl; 
      }
      
    }
    else if (starts('B','L',buffer)) {
      // deal with BL line
      //BL   GQA motif; width=49; seqs=60; 99.5%=2634; strength=1321

      int i=5;
      while (buffer[i] != ';') {
	motinf += buffer[i];  // extract motif info
	i++;
      }
      
      while (buffer[i] != '=') {i++;}
      i++; // skip '='
      
      mystring width_s;
      while (buffer[i] != ';') {
	width_s += buffer[i];  // extract depth
	i++;
      }
      width = atoi(width_s.peek());

      i++; // skip ';'
      while (buffer[i] != '=') {i++;} i++; // skip '='

      mystring depth_s;
      while (buffer[i] != ';') {
	depth_s += buffer[i];  // extract depth
	i++;
      }
      depth = atoi(depth_s.peek());
     

      if (DEBUG) {  
	cout << "**MI " << motinf << endl; 
	cout << "**MD " << depth << endl; 
	cout << "**MW " << width << endl; 
      }

      // width and depth
      // Enough data is known to create a FingerPrint and motif

      // First check that this is a new fingerprint other than just a
      // new motif.

      if (oldfpname != fpname) {
	// new fingerprint and motif
	oldfpname = fpname;
	if (DEBUG) {  
	  printf("AddFp(%s,%d,%d,%s,%s)\n",fpname.peek(),nummots,depth,accession.peek(),fpinf.peek());
	}
	fp = AddFingerPrint(fpname,nummots,depth,accession,fpinf,sm,COMPACT);

      }
       if (DEBUG) {  
	 printf("Addmot(%s,%d,%s,%d,%d)\n",accession.peek(),width,motinf.peek(),depth,motnum);
       }
      fp->AddMotif( accession , width , motinf , depth , motnum );
      
      q = fp->head;
      while (q->next != 0)
	q=q->next;
        
      startn_1 = start;
      if (motnum == 1) {
	start = 0;
      } else {
	start = startn_1 + width + lint;  // still requires the interval
      }

      // indicate next motif
      motnum++;
      // clear all motif vars 
      accession=""; motinf=""; width=0; depth=0;

    }
    else if (buffer[0] == '/' && buffer[1] == '/') {
      // end of block - deal with motif or fingerprint
      //cout << "//   line: " << endl << endl;
    }
    else  { 
      // assumption this is the data line
      //  DYR_BPT4 (  13) TVDGFNELAFGLGDGLPWGRVKKDLQNFKAR  60
      // DYR_HALVO (  42) VVLGRTTFESMR  65
      //DRT1_ARATH (  67) VVMGRKTWESIP   9
      
      int i =0;
      mystring seqname;
      mystring seq;

      while (buffer[i] == ' ') { i++; } // consume whitespace
      while (buffer[i] != ' ') {
	seqname += buffer[i];
	i++;
      }
      while (buffer[i] != ')') { i++; } // consume number and brackets
      i+=2; // skip ') ' 
      while (buffer[i] != ' ') {
	seq += buffer[i];
	i++;
      }
      // seq and seqname
      q->AddItem(seq,start,lint);

      if (DEBUG) {
	printf("%d--Additem(%s,%d,%d)\n",++count_data_lines,seq.peek(),start,lint);
      }
    }
  }
  fp = FingerPrintHead;

  while (fp->next != 0) {
    fp->CreateFrequencyTables(COMPACT);
    if (want_printing)
      fp->PrintFinger();
    fp=fp->next;
  }

  if (COMPACT) { // Dont go any furthur if only parsing
    exit(0);
  }
}





// Main function to allow for input of the sequences via commandline arguments
int main (const int argc,char * argv[])
{
  int matrix = 62;

  if (argc == 3 && argv[1][0] == '-' && argv[1][1] == 'C' ) {
    int Compact = 1; // This particular option is the point of this if.

    int want_printing = 0; // want_printing is false
    int UseFasta = 1; // The flag to indicate Fasta use is true by default
    int Eval = 10;

    
    mystring seqfilename;
    seqfilename = "";

    SubstitutionMatrix * sm = new SubstitutionMatrix(matrix);

    // Produce the Dbase object    
    //cerr << "Creating the .profile database using Blosum" << matrix << endl;

    PrintsDbase Dbase(argv[2], seqfilename, sm);

    if (argv[1][2] == 'b') { 
      Dbase.ParseBlocks(want_printing, UseFasta, Compact); 
    } else { Dbase.ParsePrints(want_printing, UseFasta, Eval, Compact); }

    cerr << "exiting" << endl;
  }
  else if (argc < 3) {
    cout << "#######################################################################" << endl
	 << GNU_LICENCE << endl
	 << "#######################################################################" << endl
	 << endl << "Insufficient Arguments" << endl << endl
	 << argv[0] << " Databasefile " << "Sequencefile " << "[-arglist] > outputfile" << endl
	 << endl
	 << "The Database file format is the .profile format" << endl
	 << "The Sequence file format is fasta-format" << endl
	 << endl
	 << "\t-a   Calculate consensus sequences for the Alignment view (Table 4) [EXPERIMENTAL] " << endl
	 << "\t-c   Set database format to .profile format (Default)" << endl
	 << "\t-C   Output the .profile format of the database" << endl
	 << "\t\tThe syntax of this command is different to all other commands." << endl
	 << "\t\tbeing:\t"<<argv[0]<<" -C prints.dat > prints.pval" << endl 
	 << "\t\tor:   \t"<<argv[0]<<" -Cb blocks.dat > blocks.pval" << endl
	 << "\t-d # Percentage allowed distance deviation between motifs." << endl 
	 << "\t     (where # is a number in the range 0 .. 100)" << endl
	 << "\t     (0 is a special case which turns off distance checking)" << endl
	 << "\t-e # E-value threshold." << endl
	 << "\t     (where # is a floating point number)" << endl
	 << "\t-E #1 #2  E-value calculation parameters." << endl
	 << "\t     (where #1 is the number of sequences in the primary database (default "<< DEFAULTDBSEQS <<"))" << endl
	 << "\t     (where #2 is the number of resides   in the primary database (default "<< DEFAULTDBLEN <<"))" << endl
	 << "\t     ( the default values are based on "<< DEFAULTDB <<")" << endl

	 << "\t-f   Explicitly state the format of the provided sequence is in the fasta format" << endl
	 << "\t      (Currently default)" << endl
	 << "\t-F   Explicitly state the format of the provided sequence is the PRINTS in house format" << endl
	 << "\t      (Now defunct)" << endl
	 << "\t-G   Set options to produce output compatable with the GRAPHScan program" << endl
	 << "\t-m   Set the scoring matrix (62 (default), 45 or 80)" << endl
	 << "\t      (This only makes any difference when creating the .pval database)" << endl
	 << "\t-M   Use E=p.D calculation method " << endl
	 << "\t-n   Use the conventional database format (Slow when computing pvals)" << endl
	 << "\t-o # " << endl
	 << "\t     (where # = a decimal number corresponding to the type of output" << endl
	 << "\t     required, this number corresponds to the number of bits turned on" << endl
	 << "\t     in the following matrix)" << endl
	 << "\t        TableNo  4 3 2 1" << endl
	 << "\t        BitValue 1 2 4 8" << endl
	 << "\t        Eg.      0 0 1 1 = 4+8 = 12, which will switch on " << endl
	 << "\t                                tables 1 and 2 in the output" << endl
	 << "\t        Table 1 = shortest description of the results, only the" << endl
	 << "\t                  top scoring hits" << endl
	 << "\t        Table 2 = medium description of the results, the ten" << endl
	 << "\t                  top scoring hits detailed by fingerprint" << endl
	 << "\t        Table 3 = most detailed description of the results," << endl
	 << "\t                  the ten top scoring hits, detailed by" << endl
	 << "\t                  individual motif" << endl
	 << "\t        Table 4 = EXPERIMENTAL alignment of the sequence and the " << endl
	 << "\t                  consensus sequence of the motif." << endl
	 << "\t                  (-a flag needs to be on too)" << endl
	 << "\t-R   Restrict all results in all tables to those which score below the Evalue" << endl
	 << "\t     threshold." << endl
	 << "\t-s   select the random shuffle option (which shuffles all sequences)" << endl
	 << "\t-t # Specify the Profile score threshold (where # is a positive integer (default= 15))" << endl
	 << "\t-w   Output the GNU licence" << endl
	 << "\t-x # Specify the P-value score threshold " << endl 
	 << "\t     (where # is a positive integer representing the negative power to which 10 is raised)" << endl
	 << "\nVERSION NUMBER " << VERSION_NUMBER << endl
	 << endl;
    return 1;
  }
  else {
    
    int want_printing = 0; // want_printing is false
    int UseFasta = 1; // The flag to indicate Fasta use is true by default
    int Compact = 0;// by default do not create the compact dbase
    int UseCompact = 1; //by default use the compact dbase
    int p =0;
    int BLOCKS = 0;
    float Eval = 0.1; // Default evalue threshold 0.1
    int paramDEBUG = 0;
    mystring seqfilename;
    
    for (int i= 0; i < argc; i++) {
      if ( (argv[i][0] == '-')) {
	for (int j=1; j < strlen(argv[i]); j++) {
	  switch (argv[i][j])
	    {
	    case 'a': CONSENSUS=1; break;
	    case 'b': BLOCKS =1; break;
 	    case 'c': UseCompact =1; break;
	    case 'C': Compact = 1;  UseCompact =0; break;
	    case 'd': 
	      if (argc < (i+2)) { cout << "The -d flag requires 1 value, the Distance variance value" << endl; exit(0);}
	      DistanceVariance = atoi(argv[i+1]);
	      if (0 <= DistanceVariance && DistanceVariance <= 100) { 
		break;
	      } else { cerr << "Distance variance must be between 0 and 100%" << endl; exit(0);}
	    case 'D': paramDEBUG = 1; break;
	    case 'e': 
	      if (argc < (i+2)) { cout << "The -e flag requires 1 value, the Evalue threshold" << endl; exit(0);}	      
	      Eval = (atof(argv[i+1]))?atof(argv[i+1]):0.1; break;
	    case 'E': 
	      if (argc < (i+3)) { cout << "The -E flag requires 2 values, number_of_sequences and length_in_residues" << endl; exit(0);}
	      DBSEQS = double(atof(argv[i+1])?atof(argv[i+1]):DBSEQS);
	      DBLEN = double(atof(argv[i+2])?atof(argv[i+2]):DBLEN); 
	      break;
	    case 'f': UseFasta = 1; break;
	    case 'F': UseFasta = 0; break;
	    case 'g': GRAPH = 1; 
	      break;
	    case 'G': // Graphscan flag, produce options suitable for the GRAPHScan program
	      GRAPH = 1; threshold = 1; options = 17; PvalueThreshold = 10; 
	      break;
	    case 'I': // Switch on GRAPHDEBUG flag, which produced a graph with the unweighted scoring
	      GRAPHDEBUG = 1; break;
	    case 'j': julian_selley = 1; break; //Add description to 1st table
	    case 'm': 
	      if (argc < (i+2)) { cout << "The -m flag requires 1 value, the Matrix choice 45 62 or 80" << endl; exit(0);}
	      matrix = (atoi(argv[i+1]))?atoi(argv[i+1]):62; 
	      if (matrix == 62 || matrix == 45 || matrix == 80) { }
	      else {
		cerr << "Only Blosum 45, 62 and 80 are available" << endl;
		exit(0);
	      } break;
	    case 'M': UsepDmethod = 1; break; //Set the flag so as to Use P * D method
	    case 'n': UseCompact =0; break; //Use the old database  
	    case 'o': 
	      if (argc < (i+2)) { cout << "The -o flag requires 1 value, the options variable" << endl; exit(0);}
	      options =  (atoi(argv[i+1]))?atoi(argv[i+1]):15; 
	      break;
	    case 'p': want_printing = 1; break;
	    case 'R': RESTRICT =1; break;
	    case 's': shuffle = 1; break;
	    case 't':	      
	      if (argc < (i+2)) { cout << "The -t flag requires 1 value, the score threshold" << endl; exit(0);}
	      threshold = (atoi(argv[i+1]))?atoi(argv[i+1]):15; 
	      break;
	    case 'v': cout << VERSION_NUMBER << endl; return 1; exit(0);
	    case 'w': cout << GNU_LICENCE << endl; return 1; break;
	    case 'x': 
	      if (argc < (i+2)) { cout << "The -x flag requires 1 value, the pvalue threshold" << endl; exit(0);}

	      p = (atoi(argv[i+1]))?atoi(argv[i+1]):3;
	      PvalueThreshold = 1.0 / double(pow(10.0, p)); 
	      break;
	    }
	}
      }
      else {
      }
    }

    if(paramDEBUG){ 
      cerr << "DEBUG - parameters -------------------- \n\n";
      cerr << "OPTIONS        " << options << endl;
      cerr << "Threshold      " << threshold << endl;
      cerr << "PvalThreshold  " << PvalueThreshold << endl;
      cerr << "Eval threshold " << Eval << endl;
      cerr << "Distance Var   " << DistanceVariance << endl;
      cerr << "DBSEQS         " << DBSEQS << endl;
      cerr << "DBLEN          " << DBLEN << endl;
      if (UsepDmethod) { cerr << "Method is pD" << endl; } else { cerr << "Method is pN/n" << endl; }
      if (GRAPH) { cerr << "GRAPH on" << endl; }
      if (CONSENSUS) { cerr << "CONSENSUS on" << endl; }
      if (BLOCKS) { cerr << "BLOCKS on"<< endl;}
      if (UseCompact) { cerr << "UseCompact " << endl;}
      if (Compact) { cerr << "Create Compact " << endl;}
      if (UseFasta) { cerr << "Use fasta " << endl;}
      cerr << "\nDEBUG - parameters --------------------\n";
    }	      
    
    seqfilename = argv[2];
    SubstitutionMatrix * sm = new SubstitutionMatrix(matrix);

    // Produce the Dbase object    
    PrintsDbase Dbase(argv[1], seqfilename, sm);

    if (UseCompact) {
      Dbase.ParseCompact(want_printing, UseFasta, Eval);
    }
    else {
      //cerr << "Creating the .profile database using Blosum" << matrix << endl;
      if (BLOCKS) {
	Dbase.ParseBlocks(want_printing, UseFasta, Compact);
      } else {
	Dbase.ParsePrints(want_printing, UseFasta, Eval, Compact);
      }
    }
  }
  return 0;
}
