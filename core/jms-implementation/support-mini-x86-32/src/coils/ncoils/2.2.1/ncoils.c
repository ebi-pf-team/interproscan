#include "ncoils.h"
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
/* Based on Rob Russell's ncoils */

void predict (char *seq, float **P, int mode){
	int i,j,k,l;
	int verb;
	int window,pt;
	int which,weighted;
	int nseq;
	int t,tc;
	int seqlen;
	int min_seg;
  int error;

	float min_P;

	struct hept_pref *h;

	/* defaults */
	window = 21;
	weighted = 0;
	verb = 0;
	// mode = 2; /* 0 = column mode, 1 = fasta, 2 = concise */
	min_P = 0.5;

  seqlen = strlen(seq);
	h = read_matrix();
	if(verb) {
	   for(i=0; i<strlen(AAs); ++i) if(AAs[i]!='_') {
		pt=(int)(AAs[i]-'A');
		printf("AA %c %4.2f %4.2f %4.2f %4.2f %4.2f %4.2f %4.2f\n",AAs[i],
	     	   h->m[pt][0],h->m[pt][1],h->m[pt][2],h->m[pt][3],h->m[pt][4],
	     	   h->m[pt][5],h->m[pt][6]);
	   }
	   for(i=0; i<h->n; ++i) {
		printf("Window %4d %1d %f %f %f %f %f\n",
			h->f[i].win,h->f[i].w,h->f[i].m_cc,h->f[i].sd_cc,h->f[i].m_g,h->f[i].sd_g,h->f[i].sc);
	   }
	}

	/* See if there is a file for our chosen window length/weight scheme */
	which = -1;
	for(i=0; i<h->n; ++i) {
		if((h->f[i].win == window) && (h->f[i].w == weighted)) { /* match */
			if(verb) printf("Found fitting data for win %4d w %d\n",window,weighted);
			which = i;
		}
	}

	nseq = 0;
	t = 0;
	tc = 0;
	//*P        = (float *)malloc(seqlen*sizeof(float));
  pred_coils(seq,h,window,which,weighted,mode,min_P,&t,&tc,min_seg,*P);
}

void pred_coils(char *seq, struct hept_pref *h, int win, int which, int weighted,int mode, float min_P, int *t, int *tc, int min_seg, float *P) {

	int i,j;
	int len,pos,aa_pt;
	int pt;
	int total_coil_segments;
	int are_there_coils;

	float actual_win;
	float this_score,Gg,Gcc,power;
	float t1,t2,t3,t4;
	float *score;

	char *hept_seq;
	
	len=strlen(seq);

	score    = (float*)malloc(len*sizeof(float));
	//P        = (float*)malloc(len*sizeof(float));
	hept_seq =  (char*)malloc(len*sizeof(char));

/*	printf("Sequence is %s length is %d\n",seq,len); */
	for(i=0; i<len; ++i) { P[i]=0.0; score[i] = 0.0; hept_seq[i] = 'x'; }

	for(i=0; i<(len-win+1); ++i) {
		this_score = 1.0;
		actual_win=0.0;
                for(j=0; ((j<win) && ((i+j)<len)); ++j) {
		   aa_pt = (int)(seq[i+j]-'A');
		   if((aa_pt>=0) && (aa_pt<26) && (AAs[aa_pt]!='_')) {
			pos = j%7; /* where are we in the heptad?  pos modulus 7 */
/*			printf("AA %c in hept %c %7.5f\n",seq[i+j],('a'+pos),h->m[aa_pt][pos]);  */
			if(weighted && (pos==0 || pos==3)) { power = 2.5; }
			else { power = 1.0; }
			actual_win+=power;
			if(h->m[aa_pt][pos]!=-1) {
				this_score*=pow(h->m[aa_pt][pos],power);
			} else {
				this_score*=pow(h->smallest,power);
			}
		    }
                }
		if(actual_win>0) {
		   this_score = pow(this_score,(1/(float)actual_win));
		} else {
		   this_score = 0;
	        }
                for(j=0; ((j<win) && ((i+j)<len)); ++j) {
		    aa_pt = (int)(seq[i+j]-'A');
		    if((aa_pt>=0) && (aa_pt<26) && (AAs[aa_pt]!='_')) {
			pos = j%7; /* where are we in the heptad?  pos modulus 7 */
			if(this_score>score[i+j]) { score[i+j]=this_score; hept_seq[i+j]='a'+pos; }
		    }
		}
       }


	if(mode==1) {
		printf(">SEQ\n");
	}
	are_there_coils=0;
	total_coil_segments=0;
	for(i=0; i<len; ++i) {
		/* Calculate P */
		t1 = 1/(h->f[which].sd_cc);
		t2 = (score[i]-(h->f[which].m_cc))/h->f[which].sd_cc;
		t3 = fabs(t2);
		t4 = pow(t3,2);
		t4 = t3*t3;
		Gcc = t1 * exp(-0.5*t4);
/*		printf("Gcc %f %f %f %f %f\n",t1cc,t2cc,t3cc,t4cc,Gcc); */
		t1 = 1/(h->f[which].sd_g);
		t2 = (score[i]-(h->f[which].m_g))/h->f[which].sd_g;
		t3 = fabs(t2);
		t4 = pow(t3,2);
		t4 = t3*t3;
		Gg = t1 * exp(-0.5*t4);
/*		printf("Gg %f %f %f %f %f\n",t1g,t2g,t3g,t4g,Gg); */
		P[i] = Gcc/(h->f[which].sc*Gg+Gcc);
		if(P[i]>=min_P) {
                   are_there_coils=1;
                   if((i==0) || (P[i-1]<min_P)) { total_coil_segments++; }
		   (*tc)++; 
                }
		(*t)++;
		if(mode==1) {
			if(P[i]>=min_P) { printf("x"); }
			else { printf("%c",seq[i]); }
			if(((i+1)%60)==0) { printf("\n"); }
		} else if(mode==0) {
			printf("%4d %c %c %7.3f %7.3f (%7.3f %7.3f)\n",i+1,seq[i],hept_seq[i],score[i],P[i],Gcc,Gg);
		}
	}
	if(mode==1) { printf("\n"); } 
        if((mode==2) && (are_there_coils==1) && (total_coil_segments>=min_seg)) {
		if(total_coil_segments==1) {
			printf("Pred %4d coil segment",total_coil_segments);
		} else {
			printf("Pred %4d coil segments",total_coil_segments);
		}
	}

	free(score); 
  free(hept_seq);
}




struct hept_pref *read_matrix() {

	int i,j;
	int pt,aa_len;
	int win;


	struct hept_pref *h;

	aa_len = strlen(AAs);

	h = (struct hept_pref*)malloc(sizeof(struct hept_pref));

	h->m = (float**)malloc(aa_len*sizeof(float*));
	for(i=0; i<aa_len; ++i) {
		h->m[i]=(float*)malloc(7*sizeof(float));
		for(j=0; j<7; ++j) {
			h->m[i][j]=-1;
		}
	}
	h->f = (struct fit_dat*)malloc(sizeof(struct fit_dat));
	h->n = 0;
	h->smallest=1.0;

  for(i=0; i< weight_size; i++){
    h->n = i + 1;
		if(strncmp(&weight_prefix[i], "u", 1)==0) { 
      h->f[i].w=0; 
    } else { 
      h->f[i].w=1; 
    }
		h->f[i].win   = (int)(weight[i][0]);
		h->f[i].m_cc  = weight[i][1]; 
		h->f[i].sd_cc = weight[i][2];
		h->f[i].m_g   = weight[i][3];
		h->f[i].sd_g  = weight[i][4];
		h->f[i].sc    = weight[i][5];

		h->f = (struct fit_dat*)realloc(h->f,((h->n)+1)*sizeof(struct fit_dat)); 
  }
  
  int m;
  for(m=0; m < matrix_size; m++){
    pt = (int)(matrix_prefix[m]-'A');
    //printf("pt is %d, m is %d\n", pt, m);
		if(h->m[pt][0]==-1) {
			for(i=0; i< matrix_row_size; ++i) {
					h->m[pt][i] = matrix[m][i];
          //printf(" element %f \n", h->m[pt][i]); 
					if(h->m[pt][i]>0) {
						if(h->m[pt][i]<h->smallest) { h->smallest = h->m[pt][i]; }
					} else {
						h->m[pt][i]=-1; /* Don't permit zero values */
					}
				}
			} else {
				printf("Warning: multiple entries for AA %c in matrix file\n", matrix_prefix[m]);
			}
  } 
	return h;
}

void process_seq_stdin(int summary_output) {

    char c;
    char *header, *seq;
    int n_header = 100, n_seq = 100;
    header = (char *)malloc(n_header + 1);
    seq = (char *)malloc(n_seq + 1);
    int h_len = 0, s_len = 0;
    float *res;

    while ((c = fgetc(stdin)) != EOF) {
        if (c == '>') {
            // new sequence
            if (s_len > 0) {
                // not the first entry: process previous sequence
                seq[n_seq] = '\0';
                res = (float *) malloc(strlen(seq)*sizeof(float));
                if (summary_output)
                    predict(seq, &res, 3); /* mode = 2; 0 = column mode, 1 = fasta, 2 = concise, 3 = nothing -> use function below to output i5 format */
                else
                    predict(seq, &res, 0);
                if (summary_output) output_coils_summary(header, res, s_len);
                s_len = h_len = 0;
            }
            while ((c = fgetc(stdin)) != '\n') {
                // Store header; do we want to trim the header to just the first word after '>'?
                header[h_len] = c;
                h_len++;
                if (h_len > n_header) {
                    n_header += 100;
                    header = (char *)realloc(header, 1 + n_header);
                }
                header[h_len] = '\0';
            }
        } else if (c != '\n') {
            seq[s_len] = c;
            s_len++;
            while ((c = fgetc(stdin)) != '\n') {
                seq[s_len] = c;
                s_len++;
                if (s_len > n_seq) {
                    n_seq += 100;
                    seq = (char *)realloc(seq, 1 + n_seq);
                }
            }
        }
    }
    if (s_len > 0) {
        res = (float *) malloc(strlen(seq)*sizeof(float));
        if (summary_output)
            predict(seq, &res, 3);
        else
            predict(seq, &res, 0);
        if (summary_output) output_coils_summary(header, res, s_len);
        s_len = h_len = 0;
    }
}

void output_coils_summary(char *id, float res[], int seqlen)
{
    int i;
    int open = 0;

    printf(">%s\n", id);
    for(i = 0; i < seqlen; i++) {
        if (res[i] > 0.5) {
            if (! open) {
               printf("%d", i + 1);
               open = 1;
            }
        } else {
            if (open) {
                printf(" %d\n", i);
                open = 0;
            }
        }
    }
    if (open)
        printf(" %d\n", i);
    printf("//\n");
}
