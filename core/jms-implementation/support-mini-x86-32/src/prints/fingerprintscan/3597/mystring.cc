#include "mystring.h"
#include <cstdlib>
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


const int max_strbuf = 50000;


void error(char * s )
{
   cerr << s;
   exit (1);
}

mystring::mystring()
{
   p = new srep;
   p->s = new char[1];
   p->s[0] = '\0';
}

mystring::mystring(const mystring& x)
{
   x.p->n++;
   p = x.p;
}

mystring::mystring(const char* s)
{
   p = new srep;
   p->s = new char[ strlen(s)+1 ];
   strcpy(p->s, s);
}

mystring::~mystring()
{
   if (--(p->n) <= 0)
   {
      delete[] p->s;
      delete p;
   }
}

mystring& mystring::operator= (const char * s)
{
   if (p->n > 1)
   {
      p->n--;			//disconnect self
      p= new srep;
   }
   else				//free old mystring
      delete[] p->s;

   p->s = new char[ strlen(s)+1 ];
   strcpy (p->s, s);
   return *this;
}

mystring& mystring::operator= (const mystring& x)
{
   x.p->n++; 	//protect against "st = st"
   // how could the if ever be entered?
   if (--p->n <= 0)
   {
      delete[] p->s;
      delete p;
   }

   p = x.p;
   return *this;
}

ostream& operator<<(ostream& s, const mystring& x)
{
   return s << x.p->s /*<< " [" << x.p->n << "]" << endl*/;
}

istream& operator>>(istream& s, mystring& x)
{
   char buf[1000];
   s.get(buf,1000,'\n');
   x = buf;
   //cout << "echo: " << x << endl;
   return s;
}

char& mystring::operator[] (int i)
{
   if (i<0 || strlen(p->s)<i)
   	error("index out of range");
   
   if (p->n > 1) //clone to maintain value semantics
   {
      srep* np = new srep;
      np->s = new char[ strlen(p->s)+1 ];
      strcpy(np->s, p->s);
      p->n--;
      p = np;
   }
   return p->s[i];
}

const char& mystring::operator[] (int i) const
{
   if (i<0 || strlen(p->s)<i)
   	error("index out of range");
   return p->s[i];
}

int mystring::length() const { return strlen(p->s); }

void mystring::mystring_copy(char * s)
{
   strcpy(s, p->s);  
}


mystring &mystring::operator+=(const mystring &s)
{
#ifdef __DEBUG
        if(length() + s.length() >= max_strbuf)
                error("Insufficient buffer space, mystring &mystring::op+=(mystring&)");
#endif

	char buf[length() + s.length() +2];
        strcpy(buf, p->s);
        strcat(buf, s.p->s);
        return *this = buf;
}

mystring &mystring::operator+=(const char *c)
{
#ifdef __DEBUG
        if(length() + strlen(c) >= max_strbuf)
                error("Insufficient buffer space, mystring &mystring::op+=(char*)");
#endif

	char buf[length() + strlen(c)+2];
        strcpy(buf, p->s);
        strcat(buf, c);
        return *this = buf;
}

mystring &mystring::operator+=(const char c)
{
        char * buf = new char [strlen(p->s) + 2];
        strcpy(buf, p->s);
        buf[strlen(p->s)] = c;
        buf[strlen(p->s)+1] = '\0';
        *this = buf;
        delete [] buf;
        return *this;
        
}


