#ifndef __NCOILS_H__
#define __NCOILS_H__

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>

#define AAs "A_CDEFGHI_KLMN_PQRST_VW_Y_"
#define PI  3.1415

/* Include file for Ncoils */

struct hept_pref {
	float **m;		/* 20 x 7 amino acid heptad weights */
	float smallest;		/* Smallest of the above */
	int n;			/* statistical fitting data (weighted) */
	struct fit_dat *f;
};

struct fit_dat {
	int win;		/* Window length */
	float m_cc, sd_cc;	/* mean/sd for coiled-coils */
	float m_g,  sd_g;	/* mean/sd for globular */
	float sc;		/* scaling factor */
	int w;			/* 1= weighted, 0=un-weighted */
};


struct hept_pref *read_matrix();
void exit_error();
void predict(char *seq, float **p, int);
void process_seq_stdin(int);
void output_coils_summary(char *, float *, int);

void pred_coils(char *seq, struct hept_pref *h,int win,int which,
   int weighted,int fasta,float min_P, int *t, int *tc, int min_segs, float *P);

int weight_size = { 6 };

char weight_prefix [6] = {
 'w', 'w', 'w', 'u', 'u', 'u'
};


float weight [6][6] = {
  {
    14, 1.89, 0.30, 1.04, 0.27, 20
  },
  {
    21, 1.79, 0.24, 0.92, 0.22, 25
  },
  { 
    28, 1.74, 0.20, 0.86, 0.18, 30
  },
  {
    14, 1.82, 0.28, 0.95, 0.26, 20
  },
  {
    21, 1.74, 0.23, 0.86, 0.21, 25
  },
  {
    28, 1.69, 0.18, 0.80, 0.18, 30
  }
};


char matrix_prefix [20] = {
  'L', 'I', 'V', 'M', 'F', 'Y', 'G', 'A', 'K', 'R', 
  'H', 'E', 'D', 'Q', 'N', 'S', 'T', 'C', 'W', 'P'
}; 


int matrix_size     = { 20 };
int matrix_row_size = {7};

float matrix [20][7] = {
{2.998,0.269,0.367,3.852,0.510,0.514,0.562},
{2.408,0.261,0.345,0.931,0.402,0.440,0.289},
{1.525,0.479,0.350,0.887,0.286,0.350,0.362},
{2.161,0.605,0.442,1.441,0.607,0.457,0.570},
{0.490,0.075,0.391,0.639,0.125,0.081,0.038},
{1.319,0.064,0.081,1.526,0.204,0.118,0.096},
{0.084,0.215,0.432,0.111,0.153,0.367,0.125},
{1.283,1.364,1.077,2.219,0.490,1.265,0.903},
{1.233,2.194,1.817,0.611,2.095,1.686,2.027},
{1.014,1.476,1.771,0.114,1.667,2.006,1.844},
{0.590,0.646,0.584,0.842,0.307,0.611,0.396},
{0.281,3.351,2.998,0.789,4.868,2.735,3.812},
{0.068,2.103,1.646,0.182,0.664,1.581,1.401},
{0.311,2.290,2.330,0.811,2.596,2.155,2.585},
{1.231,1.683,2.157,0.197,1.653,2.430,2.065},
{0.332,0.753,0.930,0.424,0.734,0.801,0.518},
{0.197,0.543,0.647,0.680,0.905,0.643,0.808},
{0.918,0.002,0.385,0.440,0.138,0.432,0.079},
{0.066,0.064,0.065,0.747,0.006,0.115,0.014},
{0.004,0.108,0.018,0.006,0.010,0.004,0.007}
};

#endif // __NCOILS_H__
