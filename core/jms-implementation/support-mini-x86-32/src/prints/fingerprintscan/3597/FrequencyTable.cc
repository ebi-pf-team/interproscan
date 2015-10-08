#include "FrequencyTable.h"

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

/**************** Statistical Significance Parameter Subroutine ****************

 Version 1.0 February 2, 1990
 Version 2.0 March 18,   1993

 Program by: Stephen Altschul

 Address: National Center for Biotechnology Information
   National Library of Medicine
   National Institutes of Health
   Bethesda, MD  20894

 Internet: altschul@ncbi.nlm.nih.gov

See: Karlin, S. & Altschul, S.F. "Methods for Assessing the Statistical
 Significance of Molecular Sequence Features by Using General Scoring
 Schemes,"  Proc. Natl. Acad. Sci. USA 87 (1990), 2264-2268.

 Computes the parameters lambda and K for use in calculating the
 statistical significance of high-scoring segments or subalignments.

 The scoring scheme must be integer valued.  A positive score must be
 possible, but the expected (mean) score must be negative.

 A program that calls this routine must provide the value of the lowest
 possible score, the value of the greatest possible score, and a pointer
 to an array of probabilities for the occurence of all scores between
 these two extreme scores.  For example, if score -2 occurs with
 probability 0.7, score 0 occurs with probability 0.1, and score 3
 occurs with probability 0.2, then the subroutine must be called with
 low = -2, high = 3, and pr pointing to the array of values
 { 0.7, 0.0, 0.1, 0.0, 0.0, 0.2 }.  The calling program must also provide
 pointers to lambda and K; the subroutine will then calculate the values
 of these two parameters.  In this example, lambda=0.330 and K=0.154.

 The parameters lambda and K can be used as follows.  Suppose we are
 given a length N random sequence of independent letters.  Associated
 with each letter is a score, and the probabilities of the letters
 determine the probability for each score.  Let S be the aggregate score
 of the highest scoring contiguous segment of this sequence.  Then if N
 is sufficiently large (greater than 100), the following bound on the
 probability that S is greater than or equal to x applies:
 
  P( S >= x )   <=   1 - exp [ - KN exp ( - lambda * x ) ].
 
 In other words, the p-value for this segment can be written as
 1-exp[-KN*exp(-lambda*S)].

 This formula can be applied to pairwise sequence comparison by assigning
 scores to pairs of letters (e.g. amino acids), and by replacing N in the
 formula with N*M, where N and M are the lengths of the two sequences
 being compared.

 In addition, letting y = KN*exp(-lambda*S), the p-value for finding m
 distinct segments all with score >= S is given by:

                               2             m-1           -y
  1 - [ 1 + y + y /2! + ... + y   /(m-1)! ] e

 Notice that for m=1 this formula reduces to 1-exp(-y), which is the same
 as the previous formula.

*******************************************************************************/

#define MAXIT 50 /* Maximum number of iterations used in calculating K */

int FrequencyTable::gcd(int a, int b)
{
  int c;
  
  if (b<0) b= -b;
  if (b>a) { c=a; a=b; b=c; }
  for (;b;b=c) { c=a%b; a=b; }
  return a;
}


int FrequencyTable::karlin(int low, int high, double* pr, double* lambda,double* K, double* H)
  // int low;   /* Lowest score (must be negative)    */
  // int high;   /* Highest score (must be positive)   */
  // double *pr;   /* Probabilities for various scores   */
  // double *lambda;   /* Pointer to parameter lambda        */
  // double *K;   /* Pointer to parameter K             */
  // double *H;   /* Pointer to parameter H             */
{
  int i,j,range,lo,hi,first,last;
  double up,new_val,sum,Sum,av,beta,oldsum,ratio,ftemp;
  double *p,*P,*ptrP,*ptr1,*ptr2;  
  
  /* Check that scores and their associated probabilities are valid     */
  
  if (low>=0) {
    fprintf(stderr,"Lowest score must be negative.\n");
    return 0;
  }
  
  for (i=range=high-low;i> -low && !pr[i];--i);
  
  if (i<= -low) {
    fprintf(stderr,"A positive score must be possible.\n");
    return 0;
  }
 
  for (sum=i=0;i<=range;sum+=pr[i++]) if (pr[i]<0) {
    fprintf(stderr,"Negative probabilities not allowed.\n");
    return 0;
  }
  
  if (sum<0.99995 || sum>1.00005) fprintf(stderr,"Probabilities sum to %.4f.  Normalizing.\n",sum);
  
  p= new double[range+1];
  // calloc(range+1,sizeof(double));
  // Do something with new
  
  for (Sum=low,i=0;i<=range;++i) Sum+=i*(p[i]=pr[i]/sum);
  if (Sum>=0) {
    fprintf(stderr,"Invalid (non-negative) expected score:  %.3f\n",Sum);
    return 0;
  }
  
  /* Calculate the parameter lambda */
  
  up=0.5;
  do {
    up*=2;
    ptr1=p;
    beta=exp(up);
    ftemp=exp(up*(low-1));
    for (sum=i=0;i<=range;++i) sum+= *ptr1++ * (ftemp*=beta);
  }
  while (sum<1.0);
  
  for (*lambda=j=0;j<25;++j) {
    new_val=(*lambda+up)/2.0;
    beta=exp(new_val);
    ftemp=exp(new_val*(low-1));
    ptr1=p;
    for (sum=i=0;i<=range;++i) sum+= *ptr1++ * (ftemp*=beta);
    if (sum>1.0) up=new_val;
    else *lambda=new_val;
  }
  
  /* Calculate the pamameter K */
  
  ptr1=p;
  ftemp=exp(*lambda*(low-1));
  for (av=0,i=low;i<=high;++i) av+= *ptr1++ *i*(ftemp*=beta);
  *H= *lambda*av/log(2.0);
  Sum=lo=hi=0;
  
  //P= (double *) calloc(MAXIT*range+1,sizeof(double));
  P = new double[MAXIT*range+1];
  for (*P=sum=oldsum=j=1;j<=MAXIT && sum>0.001;Sum+=sum/=j++) {
    first=last=range;
    for (ptrP=P+(hi+=high)-(lo+=low);ptrP>=P;*ptrP-- =sum) {
      ptr1=ptrP-first;
      ptr2=p+first;
      for (sum=0,i=first;i<=last;++i) sum+= *ptr1-- * *ptr2++;
      if (first) --first;
      if (ptrP-P<=range) --last;
    }
    ftemp=exp(*lambda*(lo-1));
    for (sum=0,i=lo;i;++i) sum+= *++ptrP * (ftemp*=beta);
    for (;i<=hi;++i) sum+= *++ptrP;
    ratio=sum/oldsum;
    oldsum=sum;
  }
  for (;j<=200;Sum+=oldsum/j++) oldsum*=ratio;
  for (i=low;!p[i-low];++i);
  for (j= -i;i<high && j>1;) if (p[++i-low]) j=gcd(j,i);
  *K = (j*exp(-2*Sum))/(av*(1.0-exp(- *lambda*j)));
  
  delete [] p;
  delete [] P;
  
  return 1;  /* Parameters calculated successfully */
}

double FrequencyTable::p_value (int S, double K, int N, double lambda) {
  return ( 1 - exp( -K * N * exp( -1 * lambda * S)) );
  // An IRIX rounding error causes the result to be 0 in some cases
  //  double loge = log10(exp(1));
  //  return ( 1 - pow(10, ( -K * N * pow(10, (-1 * lambda * S) * loge) * loge )));
}


/***************************************************************************/

// Constructor : create a frequency table from a motif which exists as a
// mystring doesn't need delimiters only the motif length must be known

FrequencyTable::FrequencyTable(const int tl, const int nc, 
          SubstitutionMatrix * subs, int thresh)
  :table_length(tl), num_of_codes(nc), sm(subs), threshold(thresh)
{
  ft_table = new int * [table_length];
  profile  = new int * [table_length];

  if (ft_table && profile)   {
    for (int i=0; i < table_length;i++)  {
      ft_table[i] = new int [ALPHABETSIZE];
      profile[i] = new int [ALPHABETSIZE];
      
      for (int j=0; j < ALPHABETSIZE;j++) {
	ft_table[i][j] = 0; //Initialize with zeros
	profile[i][j]  = 0; //Init with zeros
      }
    }
  }
  else {
    cerr << "Error with memory allocation, FrequencyTable:: ft_table or profile\
      not allocated";
    return;
  }

  //sm = new SubstitutionMatrix; // construct a SM object
}


// destructor : clears up the data structures unallocates 'new' data
FrequencyTable::~FrequencyTable()
{
  for (int i = 0; i < table_length; i++) {
    delete [] ft_table[i]; delete [] profile[i];
  }
  delete [] ft_table;  delete [] profile;
  //  delete sm; 
}


// Insert a list-item into the frequency table list
void FrequencyTable::InsertListItem(const char aa, const int position)
{
  // at ft_table[position] insert an list_item with the amino acid aa
  // of course have to check whether it exists at this position so as to
  // instead increment the counter
  int index = (int(aa) - int('A'));
  if (0 <= index && index <= 25) {
    ft_table[position][index]++;
  }
}

void FrequencyTable::insertProfileElement(int pos, int col, int value) {
  *(*(profile+pos)+col) = value;
}

void FrequencyTable::insertFreqElement(int pos, int col, int value) {
  *(*(ft_table+pos)+col) = value;
}


void FrequencyTable::ComputeProfile(int COMPACT)
{
  float Wd = 0.0;
  //  float MaxWd = 0.0;
  //  char MaxCh = ' ';
  //  mystring consensus ="";
  
  for(int col=0; col < ALPHABETSIZE; col++) {
    // Each column in the profile corresponds to an amino acid in the alphabet
    
    for (int pos=0; pos < table_length; pos++) {
      // Each row in the profile corresponds to a position in the motif (a
      // column)
      // Profiles are organised counter-intuitively (with the columns of a motif
      // in rows)
      
      float sum=0;
      for (int d=0; d < ALPHABETSIZE; d++) {
	// The score of each coordinate is constructed by scoring each
	// amino acid in turn against the mutation matrix, but multiplying by
	// the specific positional weight of that amino acid
	Wd = Weight(char(d+int('A')),pos); // or (LogWeight(d,pos)
	if (Wd) { sum += Wd * sm->matrixLookup(d,col); }

      }
      profile[pos][col] = int(sum * MatrixMultiplier);
    }

  }


  if (COMPACT) {
    //cout << "* ";
    //for (int i=0; i<table_length; i++) {
    //  printf("%-5d",i);
    //}
    //cout << endl;
    for (int col=0; col<ALPHABETSIZE; col++) {
      printf("%c ",char(col+int('A')));
      for (int pos=0; pos < table_length; pos++) {
	printf("%-5d",ft_table[pos][col]);
      }
      cout << endl;
    }
    
    //    cout << "* ";
    //    for (int i=0; i<table_length; i++) {
    //  printf("%-5d",i);
    //}
    //cout << endl;
    for (int col=0; col<ALPHABETSIZE; col++) {
      printf("%c ",char(col+int('A')));
      for (int pos=0; pos < table_length; pos++) {
	printf("%-5d",profile[pos][col]);
      }
      cout << endl;
    }
  }

  // Create a vector of the probabilities of each score for the
  // altschul program
  vector<int> prof;// a vector of ints to store all scores in the profile
  
  for (int i=0; i < table_length; i++){
    for (int j=0; j < ALPHABETSIZE; j++) {
      prof.push_back(profile[i][j]);//store each element of the profile
    }
  }
  // sort the list of scores
  sort(prof.begin(), prof.end(), greater<int>());

  int last = 9999;  //initialize the integer to store the last value
  int a; //a temporary integer
  vector<int> unique; //a vector to store the unique values in the score list
  
  int sum=0;
  for (vector<int>::iterator k= prof.begin(); k < prof.end(); ++k) {
    a = *k; // store each profile value
    // check to see if the current value is unique or
    // if value is smaller than -950 i.e. JOU values
    if ( a == last || a < -950) { 
      // Do nothing
    } else {  // if the value is unique then push onto the unique list
      unique.push_back(a);
      last = a; // this value has been observed, no longer unique
    }
    if (a > -950) { sum++; }
  }
  
  vector<int> unique_count; // count the number of occurrences of each unique number
  //vector<float> unique_prob;// compute the probability of each unique number
  int xpos=0;

  map<int,double, greater<int> > unique_c_p;

  for (vector<int>::iterator x=unique.begin();x<unique.end();++x) {
    // add the number of occurrences in profile of each unique score
    int counter=0;
    //count(prof.begin(),prof.end(),*x,counter); 
    counter = count(prof.begin(),prof.end(),*x); 
    //count the number of occurrences in the prof vector 
    //of each of the values in the unique vector
    if (counter > -900) { // remove the JOU -999 or -989 entries
      //unique_count.push_back(counter);
      //unique_prob.push_back(float(counter)/float(sum));
      unique_c_p[*x]= double(double(counter) / double(sum));
      //cout << *x << "-> " << (double(counter) / double(sum)) << endl;
    }
  }
  //  sort(unique.begin(),unique.end(), less<int>());
  //  cout << endl;
  //int pos =0;
  //for (vector<int>::iterator p=unique.begin();p<unique.end();++p) {
  //  printf("%3d -> %f\n",*p,unique_c_p[pos++]);
  //}

  int highest = *unique.begin();
  int lowest= *(unique.end() - 1);

  const int SIZE = (-1 * lowest) + highest;
  double * plist = new double[SIZE+1];
  int j=0;
  for (int i=lowest; i < (highest+1); i++,j++ ) {
    plist[j] = unique_c_p[i];
  }

  double H = 0.0;

  karlin(lowest,highest,plist,&lambda,&K,&H);
  if (COMPACT)  { printf("KK; %f\nll; %f\n",K,lambda);}
}


//return the weight of the amino acid at a given position
// Simple calculation 
//  Count of aa in position / number of sequences
// Probability of finding an amino acid in this position
//
float FrequencyTable::Weight(const char aa, const int position)
{
  int index = (int(aa) - int('A'));
  if (0 <= index && index <= 25) {
    float value = float(ft_table[position][index]) / num_of_codes;
    return value;
  }
  return 0;
}


float FrequencyTable::Weight(const int index, const int position)
{
  if (0 <= index && index <= 25) {
    float value = float(ft_table[position][index]) / num_of_codes;
    return value;
  }
  return 0;
}


float FrequencyTable::LogWeight(const char aa, const int position)
{
  int index = (int(aa) - int('A'));
  if (0 <= index && index <= 25) {
    int oneminussum = 1 - ft_table[position][index];
    int oneplustotal = 1 + table_length;
    return (log( float(oneminussum) / float(oneplustotal) )
     /  log( 1.0 / oneplustotal ));
  }
  else
    return 0;
}


// Print table representation for test purposes
void FrequencyTable::PrintTable()
{
  cout << "FREQUENCY TABLE" << endl
       << "===============" << endl
       << "Depth of table " << num_of_codes << endl;
  cout << "[  ] ";
  for (int j=0; j < 26;j++) {
    printf("%-4c",char(j+'A'));
  }
  cout << "|" << endl;
  
  for (int i = 0; i < table_length; i++) {
    printf("[%-2d] ",i);
    for (int j=0; j < 26;j++) {
      ft_table[i][j] ? printf("%-4d",ft_table[i][j]): printf("    ");
    }
    cout <<  "|" << endl;
  }
}

// Calculate the score of any amino acid at a position in the
// frequency table
int FrequencyTable::Score(const char aa, const int position, int * idscore)
{
  int index = (int(aa) - int('A'));
  if (0 <= index && index <= 25) {
    *idscore += ft_table[position][index];
    return profile[position][index];
  }
  return 0;
}

int FrequencyTable::Consensus() {
  float MSc = 0.0;
  char  Mch = ' ';

  for (int i = 0; i < table_length;i++)  {
    for (int j = 0; j <= 25; j++) {
      float Sc = ft_table[i][j] / float(num_of_codes); 
      if (Sc > MSc) { 
	MSc = Sc; 
	Mch = (MSc > 0.5) ? (char(j+int('A'))):(char(j+int('a')));
      }
    }
    consensus += Mch;
    MSc = 0;
  }
}


int FrequencyTable::Scoremystring(const char * seq, int * idscore)
{
  int sum = 0;
  int counter = 0;
  int index = 0;
  // Score mystring is passed a pointer to an offset position in the sequence
  for (int i = 0; i < table_length;i++)  {
    // Attempting to speed up this program by not making the call to a function 
    //  sum += ( Score(seq[i],counter++,idscore));
    
    index = int(seq[i]) - int('A');
    if (0 <= index && index <= 25) {
      //*idscore += ft_table[counter][index];
      //sum +=  profile[counter++][index];
      *idscore += *( *(ft_table + counter) + index);
      sum += *( *(profile + counter++) + index);
    }
  }
  return sum;
}

// Send unknown amino acid sequence
// break up the sequence into motif length sized fragments and score each
// return reference to the best scoring fragment and position of the fragment
// also a pointer to the character mystring which represents the fragment
void FrequencyTable::DigestSequence(mystring seq, MotifScore * motifscore)
{
  int profilescore = 0; // variable to hold the score of each fragment 
  const int motif_size = table_length;
  // length of the frequency table is equivalent to the motif size
  
  // Choose motif_size sized fragments and score
  //
  // Number of fragments of size m from a sequence of size n which is
  // padded at both ends is f = n - (m - 1)
  // padding at both ends means that every character is visited by every
  // part of the motif (consistency)
  //
  // calculate the number of fragements required to cover the whole sequence
  const int num_fragments = seq.length() - (motif_size-1);
  char * n = new char [motif_size+1];
  if (!n) { cerr << "Error with mem alloc"; return; }
  
  // Begin loop through all fragments
  int sum;
  int counter;
  int index;

  int padding = 35; // Maybe this should be global
  for (int frg_c= 0; frg_c < num_fragments; frg_c++)  {
    // Return values are evaluated depending on score of fragment
    // the highest scoring fragment is returned along with its
    // position in the amino acid sequence and its score
    // frg_c = fragment count, this represents the start position in the
    // sequence for the fragment in question, i.e. the offset
    // so Scoremystring is passed a pointer not to the begining of the
    // sequence but to the begining + offset i.e. the begining of the fragment
    int idscore =0;
    //      score = Scoremystring(seq.peek() + frg_c, &idscore);
    sum = 0; counter = 0; index = 0;
    
    // Score mystring is passed a pointer to an offset position in the sequence
    for (int i = 0; i < table_length;i++)  {
      // Attempting to speed up this program by not making the call to a function 
      index = int(*( (seq.peek() + frg_c) + i) ) - int('A');
      if (0 <= index && index <= 25) {
	idscore += *( *(ft_table + counter) + index);
	sum += *( *(profile + counter++) + index);
      }
    }
    profilescore = sum;

    // if the score achieved for the individual fragment of the sequence
    // is greater than or equal to the designated threshold it will be
    // reported
    if (profilescore >= threshold) {
      int x = 0;
      for ( int pos = frg_c; pos < (motif_size+frg_c); pos++,x++) {
	*(n + x)  = *(seq.peek() + pos) ; //copy scoring fragment into n
      }
      n[x] = '\0' ; // Add mystring terminator to end of fragment
      // Padding of ## increases the position reported
      // here it is corrected and 1 is added to move from 0,1,2,3 to 1,2,3,4
      // then a match is added to the score list structure
      
      double pval = p_value(profilescore, K, seq.length(), lambda);
      if (!pval) { pval = 1.0 / (1.0 * pow(10.0, 16)); }
      
      motifscore->AddMS( profilescore,
			 float(idscore) * 100 / float(table_length * num_of_codes),
			 pval,
			 (frg_c - padding) + 1, n);
    }
  } // end of loop through all fragments
  delete n;
}
// Send unknown amino acid sequence
// break up the sequence into motif length sized fragments and score each
// return reference to the best scoring fragment and position of the fragment
// also a pointer to the character mystring which represents the fragment
void FrequencyTable::DigestSequenceUnweighted(mystring seq, MotifScore * motifscore)
{
  int score = 0; // variable to hold the score of each fragment 
  const int motif_size = table_length;
  // length of the frequency table is equivalent to the motif size
  
  // Choose motif_size sized fragments and score
  //
  // Number of fragments of size m from a sequence of size n which is
  // padded at both ends is f = n - (m - 1)
  // padding at both ends means that every character is visited by every
  // part of the motif (consistency)
  //
  // calculate the number of fragements required to cover the whole sequence
  const int num_fragments = seq.length() - (motif_size-1);
  char * n = new char [motif_size+1];
  if (!n) { cerr << "Error with mem alloc"; return; }
  
  // Begin loop through all fragments
  int sum;
  int counter;
  int index;

  int padding = 35; // Maybe this should be global
  for (int frg_c= 0; frg_c < num_fragments; frg_c++)  {
    // Return values are evaluated depending on score of fragment
    // the highest scoring fragment is returned along with its
    // position in the amino acid sequence and its score
    // frg_c = fragment count, this represents the start position in the
    // sequence for the fragment in question, i.e. the offset
    // so Scoremystring is passed a pointer not to the begining of the
    // sequence but to the begining + offset i.e. the begining of the fragment
    int idscore =0;
    //      score = Scoremystring(seq.peek() + frg_c, &idscore);
    sum = 0; counter = 0; index = 0;
    
    // Score mystring is passed a pointer to an offset position in the sequence
    for (int i = 0; i < table_length;i++)  {
      // Attempting to speed up this program by not making the call to a function 
      index = int(*( (seq.peek() + frg_c) + i) ) - int('A');
      if (0 <= index && index <= 25) {
	idscore += *( *(ft_table + counter) + index);
	sum += *( *(profile + counter++) + index);
      }
    }
    score = sum;

    // if the score achieved for the individual fragment of the sequence
    // is greater than or equal to the designated threshold it will be
    // reported
    float percen = (float(idscore) / float(table_length * num_of_codes)) * 100.0;

    if (percen >= threshold) {
      
      int x = 0;
      for ( int pos = frg_c; pos < (motif_size+frg_c); pos++,x++) {
	*(n + x)  = *(seq.peek() + pos) ; //copy scoring fragment into n
      }
      n[x] = '\0' ; // Add mystring terminator to end of fragment
      // Padding of ## increases the position reported
      // here it is corrected and 1 is added to move from 0,1,2,3 to 1,2,3,4
      // then a match is added to the score list structure
      
      double pval = p_value(score, K, seq.length(), lambda);
      if (!pval) { pval = 1.0 / (1.0 * pow(10.0,16)); }
      
      motifscore->AddMS( score, percen, pval,(frg_c - padding) + 1, n);
    }
  } // end of loop through all fragments
  delete n;
}


// Send unknown amino acid sequence
// break up the sequence into motif length sized fragments and score each
// return reference to the best scoring fragment and position of the fragment
// also a pointer to the character mystring which represents the fragment
void FrequencyTable::DigestSequenceGraph(mystring seq, MotifScore * motifscore)
{
  int score = 0; // variable to hold the score of each fragment 
  const int motif_size = table_length;
  // length of the frequency table is equivalent to the motif size
  
  // Choose motif_size sized fragments and score
  //
  // Number of fragments of size m from a sequence of size n which is
  // padded at both ends is f = n - (m - 1)
  // padding at both ends means that every character is visited by every
  // part of the motif (consistency)
  //
  // calculate the number of fragements required to cover the whole sequence
  const int num_fragments = seq.length() - (motif_size-1);
  char * n = new char [motif_size+1];
  if (!n) { cerr << "Error with mem alloc"; return; }
  
  // Begin loop through all fragments
  int sum;
  int counter;
  int index;
  int matches;

  int padding = 35; // Maybe this should be global
  for (int frg_c= 0; frg_c < num_fragments; frg_c++) {
    // Return values are evaluated depending on score of fragment
    // the highest scoring fragment is returned along with its
    // position in the amino acid sequence and its score
    // frg_c = fragment count, this represents the start position in the
    // sequence for the fragment in question, i.e. the offset
    // so Scoremystring is passed a pointer not to the begining of the
    // sequence but to the begining + offset i.e. the begining of the fragment
    int idscore =0; int tmp =0;
    //      score = Scoremystring(seq.peek() + frg_c, &idscore);
    sum = 0; counter = 0; index = 0; matches = 0;

    // Score mystring is passed a pointer to an offset position in the sequence
    for (int i = 0; i < table_length;i++)  {
      // Attempting to speed up this program by not making the call to a function 
      //  sum += ( Score(seq[i],counter++,idscore));
      
      index = int(*( (seq.peek() + frg_c) + i) ) - int('A');
      if (0 <= index && index <= 25) {
	tmp = *( *(ft_table + counter) + index);
	sum += *( *(profile + counter++) + index);
      }

      if (tmp) {
	idscore += tmp;
	matches++;
      }
      tmp = 0;
    }
    score = sum;
    
    // if the score achieved for the individual fragment of the sequence
    // is greater than or equal to the designated threshold it will be
    // reported      
    float percen = (float(idscore) / float(table_length * num_of_codes)) * 100.0;
    float weight = float(matches) / float(table_length);
    float adjustedidscore = percen * weight;
   
    if (adjustedidscore >= threshold) {
      int x = 0;
      for ( int pos = frg_c; pos < (motif_size+frg_c); pos++,x++) {
	*(n + x)  = *(seq.peek() + pos) ; //copy scoring fragment into n
      }
      n[x] = '\0' ; // Add mystring terminator to end of fragment
      // Padding of ## increases the position reported
      // here it is corrected and 1 is added to move from 0,1,2,3 to 1,2,3,4
      // then a match is added to the score list structure
      
      double pval = p_value(score, K, seq.length(), lambda);
      if (!pval) { pval = 1.0 / (1.0 * pow(10.0,16)); }

      motifscore->AddMS( score, 
			 adjustedidscore, 
			 pval, 
			 (frg_c - padding) + 1, n);
    }
  } // end of loop through all fragments
  delete n;
}
