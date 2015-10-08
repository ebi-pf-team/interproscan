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

void Sequence::Shuffle() {
  int len = strlen(residue_list.peek());
  vector<char> residue_list_vec(&residue_list[0], &residue_list[len]);
  random_shuffle(residue_list_vec.begin(), residue_list_vec.end(), rnd);
  residue_list = "";
  for (int i=0; i < len; i++) {
    residue_list += residue_list_vec[i];
  }
}

 
SequenceDatabase::~SequenceDatabase()
{
  Ptr_Sequence q = head;

  if (q == 0)  {
    while (q != 0)  {
      Ptr_Sequence p = q;
      q = q->next;
      delete p;
    }
  }
  
}

int SequenceDatabase::ReadSequence(mystring file_name)
{

  // Open sequence file
  ifstream from(file_name.peek()); 
  //filename.peek() returns the char * from a mystring
  if (!from)
    {
      cerr << "SequenceDatabase::ReadSequence() Cannot open file " 
	   << file_name << endl;
      return 0;  //Failed
    }
  
  const int BUFFSIZE = 1024;
  
  char buffer[BUFFSIZE]; // Store each line in temporary storage

  mystring sequence_name;
  mystring sequence_info;
  mystring sequence_whole;
  mystring seq;  // Temporary for sequence line info
  
  // Read in from formatted sequence file
  while ( !from.eof() && from.getline(buffer,BUFFSIZE))
    {
      //cerr << "Read line: " << buffer << endl;
      
      mystring buf = buffer;
      
      if ( buf[0] == '>') {
	cerr << "The sequence file is formatted incorrectly" << endl
	     << "It should be :" << endl
	     << "nm; Sequence Name" << endl 
	     << "in; Information" << endl
	     << "sq; SEQUENCE\nsq; SEQUENCE" << endl
	     << "en;"<< endl
	     << "Maybe the sequence format submitted was Fasta format" << endl
	     << "If so, then use the -f flag" << endl;
	
	return 0; // Fail (maybe because it is Fasta format
      } 
      else if ( buf[0] == 'n') {
	//cerr << "READ NAM" << endl;
	GetData(buf,sequence_name,'n','m',4);
      }
      else if ( buf[0] == 'i') {
	//cerr << "READ INF" << endl;
	GetData(buf,sequence_info,'i','n',4);
      }
      else if ( buf[0] == 's') {
	//cerr << "READ SEQ" << endl;
	GetStrippedData(buf,seq,'s','q',4);
	sequence_whole += seq;
	seq[0]= '\0';
      }
      else if ( buf[0] == 'e') {
	//cerr << "ADD SEQ" << endl;
	AddSequence(sequence_info, sequence_name, sequence_whole);
	sequence_name[0] = '\0';
	sequence_info[0] = '\0';
	sequence_whole[0]= '\0';
	
      }
      
    }
  return 1; // Successfull
}


int SequenceDatabase::ReadFasta(mystring file_name)
{
  
  // Open sequence file
  ifstream from(file_name.peek()); 
  //filename.peek() returns the char * from a mystring
  
  if (!from)  {
    cerr << "SequenceDatabase::ReadSequence() Cannot open file " 
	 << file_name << endl;
    return 0;  //Failed
  }
  
  const int BUFFSIZE = 1024; 
  // Getline does not delimit at the end of the buffer
  
  char buffer[BUFFSIZE]; // Store each line in temporary storage
  
  mystring sequence_name;
  mystring sequence_info;
  mystring sequence_whole;
  mystring seq;  // Temporary for sequence line info
  
  int startedFlag = 0; // used to indicate whether to add sequence or not
  
  // Read in from fasta formatted file
  while ( !from.eof() && from.getline(buffer,(BUFFSIZE-1))) {
    //cout << buffer << endl;
    mystring buf = buffer;
    if ( buf[0] == '>') {
      if (startedFlag) {
	
	AddSequence(sequence_info, sequence_name, sequence_whole);
	sequence_name[0] = '\0';
	sequence_info[0] = '\0';
	sequence_whole[0]= '\0';
	
	GetData(buf,sequence_name,'>',1);
	sequence_info = "Fasta sequence";
	startedFlag = 1;
	
      } 
      else {
	
	GetData(buf,sequence_name,'>',1);
	sequence_info = "Fasta sequence";
	startedFlag = 1;
      }
    }
    else {
      
      GetStrippedData(buf,seq,0);
      sequence_whole += seq;
      seq[0]= '\0';
    }
    
  }
  
  // this is done here becuse of that old chestnut, trying to use
  // the topline of a recod to delimit data, always misses the last
  // bit of data

  AddSequence(sequence_info, sequence_name, sequence_whole);
  //  cout << "RESULT OF READIN\n" 
  //       << "SeqName: " <<  sequence_name << endl
  //       << "SeqInf : " <<  sequence_info << endl
  //       << "Seq    : " <<  sequence_whole << endl
  //     << "Pad?   : " <<  padding << endl;
  
  return 1; // Successfull
}

void SequenceDatabase::AddSequence(mystring in, mystring nm, mystring res)
{
  Ptr_Sequence p = new Sequence(in, nm, res);
  Ptr_Sequence q = head;
  
  if (q == 0) { 
    head = p; 
  }
  else {
    while (q->next != 0)
      q = q->next;
    q->next = p;
  }
}


void SequenceDatabase::PrintSequences()
{
 
 Ptr_Sequence q = head;
 
 if (q != 0)  {
    while (q != 0)  {
        Ptr_Sequence p = q;
        q = q->next;
        p->PrintSequence();
    }
 }
 cout << endl;
 
}

// More generalized function to extract a mystring from the ASCII file
// The buffer is sent as buffer, and extracted into name
int SequenceDatabase::GetData(mystring & buffer, mystring & name ,char a 
    ,char b, int offset)
{

      if ((buffer[0] == a) && (buffer[1] == b))
      {
             int i = offset;
             while (buffer[i] != '\0' && i < buffer.length())
             {
              name += buffer[i++];
             }
            return 1; //get succeeds
      }
      else  
      {
             //name[0] = '\0';
             return 0; //get has failed
      }
}

// Overloaded Getdata, responds to one character
int SequenceDatabase::GetData(mystring & buffer, mystring & name ,char a 
         , int offset)
{
  
  if ((buffer[0] == a) )
    {
      int i = offset;
      while (buffer[i] != '\0' && i < buffer.length())
 {
   name += buffer[i++];
 }
      return 1; //get succeeds
    }
  else  
    {
      //name[0] = '\0';
      return 0; //get has failed
    }
}
// Specialization of GetData which strips spaces from the mystring
int SequenceDatabase::GetStrippedData(mystring & buffer, mystring & name ,
          char a ,char b, int offset)
{

  if ((buffer[0] == a) && (buffer[1] == b))
    {
      int i = offset;
      while (buffer[i] != '\0' && i < buffer.length())
 {
   if (isspace(buffer[i]) || (buffer[i] == '*'))
     name += "";
   else
     name += buffer[i];
   i++; 
 }
      return 1; //get succeeds
    }
  else  
    {
      //name[0] = '\0';
      return 0; //get has failed
    }
}

// Specialization of GetData which strips spaces from the mystring
// Overloaded GetStrippedData
int SequenceDatabase::GetStrippedData(mystring & buffer, mystring & name,
          int offset)
{
  int i = offset;
  while (buffer[i] != '\0' && i < buffer.length()) {
    if (isspace(buffer[i]) || (buffer[i] == '*'))
      name += "";
    else
      name += buffer[i];
    i++; 
  }
  return 1; 
}
