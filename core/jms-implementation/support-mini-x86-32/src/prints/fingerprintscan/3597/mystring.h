#ifndef MYSTRING_H
#define MYSTRING_H
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

// "Mystring" With Help from Adrian

#include <iostream>
#include <cstring>
using namespace std;

class mystring {
   
   struct srep {
      char * s; //pointer to data
      int n;    //reference count
      srep() { n = 1; }
   };
   
   srep * p;  //p is a pointer to the structure

public:
   mystring(const char *);	    // mystring x = "abc"
   mystring();			    // mystring x;
   mystring(const mystring &);      // mystring x = mystring ...
   ~mystring();
   
   // copy and convert
   mystring& operator= (const char *);
   mystring& operator= (const mystring &);
   
   //append to current mystring up to max. length 256 (with length check)
   mystring &operator+=(const mystring &s);
   mystring &operator+=(const char *c);
   mystring &operator+=(const char c);
   
   char& operator[] (int i);
   
   const char& operator[](int i) const;

   const char *peek() const { return p->s; }
        

   friend ostream& operator<<(ostream&, const mystring&);
   friend istream& operator>>(istream&, mystring&);

   friend int operator==(const mystring &x, const char *s)
   	{ return strcmp(x.p->s, s) == 0; }

   friend int operator==(const mystring &x, const mystring &y)
   	{ return strcmp(x.p->s, y.p->s) == 0; }

   friend int operator!=(const mystring &x, const char *s)
   	{ return strcmp(x.p->s, s) != 0; }

   friend int operator!=(const mystring &x, const mystring &y)
   	{ return strcmp(x.p->s, y.p->s) != 0; }
   
   int length() const;
   
   void mystring_copy(char * s);	
};

#endif

