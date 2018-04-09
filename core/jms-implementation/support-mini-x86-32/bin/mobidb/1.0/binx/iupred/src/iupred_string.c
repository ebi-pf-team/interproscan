#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <math.h>



#define AA "GAVLIFPSTCMWYNQDEKRH"
#define AAN 20
#define MSL 40000
#define ML 1000

#define DLC 1
#define DUC 100
#define DWS 10
#define DMin_Ene 0.3
#define DJOIN 45
#define DDEL 35


#define MAX(a,b) ((a)>(b)?(a):(b))
#define MIN(a,b) ((a)<(b)?(a):(b))


typedef struct 
{
  char name[1000];
  int le;
  char *seq; 
  double expscore;
  double *eprof;
  double *smp;
  double *en;
  int ngr;
  int **gr;
} SEQ_STR;


typedef struct 
{
  double **CC;
  double *distro;
  double min, max;
  double step;
  double cutoff;
  int nb;
} P_STR;


void 		read_mat(char *path, char *fn, double **MAT, double *matave);
int 		getargs(char *line,char *args[],int max);
void 		read_ref(char *path, char *fn, double **REF);
void 		Get_Histo( P_STR *P, char *path, char *);
double          **DMatrix(int n_rows, int n_cols);
void            *my_malloc(size_t size);
void 		IUPred(SEQ_STR *SEQ, P_STR *P);
void 		getRegions(SEQ_STR *SEQ );
void 		Get_Seq(char *seq, SEQ_STR *SEQ);


int LC, UC, WS;
double Min_Ene;
int JOIN, DEL;
int Flag_EP;
double EP;


int main(int argc, char **argv)
{
  P_STR *P;
  SEQ_STR *SEQ;
  int i,j;
  int type;
  char *path;
  
  
  
  if (argc!=3) {
    printf(" Usage: %s sequence type \n",argv[0]);
    printf("      where type stands for one of the options of \n");
    printf("      \"long\", \"short\" or \"glob\"\n");
    exit(1);
  }
  if ((path=getenv("IUPred_PATH"))==NULL) {
    fprintf(stderr,"IUPred_PATH environment variable is not set\n");
    path="./";
  } 

  if ((strncmp(argv[2],"long",4))==0) {
    type=0;   
  }
  else if ((strncmp(argv[2],"short",5))==0) {
    type=1;   
  }
  else if ((strncmp(argv[2],"glob",4))==0) {
    type=2;   
  }
  else {
    printf("Wrong argument\n");exit(1);
  }
  
  
  SEQ=malloc(sizeof(SEQ_STR));
  Get_Seq(argv[1],SEQ);
  if (SEQ->le==0) {printf(" Sequence length 0\n");exit(1);}
  
#ifdef DEBUG 
  printf("%s %d\n%s\n",SEQ->name,SEQ->le,SEQ->seq);
#endif 


  P=malloc(sizeof(P_STR)); 
  P->CC= DMatrix(AAN,AAN);

  if (type==0) {

    LC=1;
    UC=100;	 
    WS=10;
    Flag_EP=0;   

    read_ref(path,"ss",P->CC);
    Get_Histo(P, path, "histo");


    IUPred(SEQ,P);

    for (i=0;i<SEQ->le;i++) 
      printf("%5d %c %10.4f\n",i+1,SEQ->seq[i],SEQ->en[i]);
  }
  if (type==1) {
    LC=1;
    UC=25;	 
    WS=10;
    Flag_EP=1;
    EP=-1.26;

    read_ref(path,"ss_casp",P->CC);
    Get_Histo(P, path, "histo_casp");

    IUPred(SEQ,P);

    printf("# Prediction output \n");
    printf("# %s\n",SEQ->name);
    for (i=0;i<SEQ->le;i++) 
      printf("%5d %c %10.4f\n",i+1,SEQ->seq[i],SEQ->en[i]);
  }
  if (type==2) {
    char *globseq;

    LC=1;
    UC=100;	 
    WS=15;
    Flag_EP=0;



    read_ref(path,"ss",P->CC);
    Get_Histo(P,path,"histo");

    IUPred(SEQ,P);

    Min_Ene=DMin_Ene;
    JOIN=DJOIN;
    DEL=DDEL;

    getRegions(SEQ);
    globseq=malloc((SEQ->le+1)*sizeof(char));
    for (i=0;i<SEQ->le;i++) globseq[i]=tolower(SEQ->seq[i]);

    printf("# Prediction output \n");
    printf("# %s\n",SEQ->name);
    printf("Number of globular domains: %5d \n",SEQ->ngr);
    for (i=0;i<SEQ->ngr;i++) {     
      printf("          globular domain   %5d.    %d - %d \n",
	     i+1,SEQ->gr[i][0]+1,SEQ->gr[i][1]+1);          
      for (j=SEQ->gr[i][0];j<SEQ->gr[i][1]+1;j++) {
	globseq[j]=toupper(globseq[j]);
      }
    }
    printf(">%s\n",SEQ->name);
    for (i=0;i<SEQ->le;i++) {
      if ((i>0)&&(i%60==0)) printf("\n");
      else if ((i>0)&&(i%10==0)) printf(" ");
      printf("%c",globseq[i]);
    }
    printf("\n");
    free(globseq);

    
#ifdef DEBUG
    for (i=0;i<SEQ->le;i++) 
      printf("%5d %c %10.4f\n",i,SEQ->seq[i],SEQ->en[i]);
#endif
  }
  
  free(SEQ->seq);
  free(SEQ->eprof);free(SEQ->en);free(SEQ->smp);
  free(SEQ);
  
  
  return 0;
}


void IUPred(SEQ_STR *SEQ, P_STR *P)
{
  int i,j, a1, a2,   p;
  int naa;
  double n2;
  double min, max, step;
  
  naa=SEQ->le;
  min=P->min; max=P->max;step=P->step;
  
  SEQ->eprof=malloc(naa*sizeof(double));
  for (i=0;i<naa;i++) SEQ->eprof[i]=0;
  SEQ->en=malloc(naa*sizeof(double));
  for (i=0;i<naa;i++) SEQ->en[i]=0;
  SEQ->smp=malloc(naa*sizeof(double));
  for (i=0;i<naa;i++) SEQ->smp[i]=0;

  
  SEQ->expscore=0;
  for (i=0;i<naa;i++) {     
    a1=strchr(AA,((toupper(SEQ->seq[i]))))-AA;
    if ((a1<0) || (a1>=AAN)) continue;
    n2=0;
    for (j=0;j<naa;j++) if (((abs(i-j))>LC)&&((abs(i-j))<UC)) {
      a2=strchr(AA,((toupper(SEQ->seq[j]))))-AA;
      if ((a2<0) || (a2>=AAN)) continue;
     SEQ->eprof[i]+=P->CC[a1][a2];
     n2++;
   }
   SEQ->expscore+=SEQ->eprof[i]/(naa*n2);
   SEQ->eprof[i]/=n2;
   
  }
  
  if (Flag_EP==0) {
    for (i=0;i<naa;i++) {
      n2=0;
      for (j=MAX(0,i-WS);j<=MIN(naa,i+WS+1);j++) {
	SEQ->smp[i]+=SEQ->eprof[j];
	n2++;
      }
      SEQ->smp[i]/=n2;
    }
  } else {
    for (i=0;i<naa;i++) {
      n2=0;
      for (j=i-WS;j<i+WS;j++) {
	if ((j<0)||(j>=naa)) SEQ->smp[i]+=EP;
	else SEQ->smp[i]+=SEQ->eprof[j];   
	
	n2++;
      }
      SEQ->smp[i]/=n2;
    }
  }

  for (i=0;i<naa;i++) {

    if (SEQ->smp[i]<=min+2*step) SEQ->en[i]=1;
    if (SEQ->smp[i]>=max-2*step) SEQ->en[i]=0;
    if ((SEQ->smp[i]>min+2*step)&&(SEQ->smp[i]<max-2*step)) {
      p=(int)((SEQ->smp[i]-min)*(1.0/step));     
      SEQ->en[i]=P->distro[p];
    }

#ifdef DEBUG
    printf("%5d %10.4f %10.4f %10.4f\n",
	   i,SEQ->eprof[i], SEQ->smp[i],SEQ->en[i]);    
  
#endif  

  }

}



void getRegions(SEQ_STR *SEQ )
{
  int naa;
  int i, k,kk;
  int **GR, **mGR;
  int in_GR;
  int nr, mnr;
  int beg_GR, end_GR;
  int beg,end;


  naa=SEQ->le;

  
  GR=NULL;
  nr=0;
  in_GR=0;
  beg_GR=end_GR=0;
  

  for (i=0;i<naa;i++) {

    if ((in_GR==1)&&(SEQ->smp[i]<=Min_Ene)) {
      GR=realloc(GR,(nr+1)*sizeof(int *));
      GR[nr]=malloc(2*sizeof(int));
      GR[nr][0]=beg_GR;
      GR[nr][1]=end_GR;
      in_GR=0;
      nr++;
    }
    else if (in_GR==1) 
      end_GR++;
    if ((SEQ->smp[i]>Min_Ene)&&(in_GR==0)) {
      beg_GR=i;
      end_GR=i;
      in_GR=1;
    }
  }
  if (in_GR==1) {
    GR=realloc(GR,(nr+1)*sizeof(int *));
    GR[nr]=malloc(2*sizeof(int));
    GR[nr][0]=beg_GR;
    GR[nr][1]=end_GR;
    in_GR=0;
    nr++;
  }
  

  mnr=0;
  k=0;mGR=NULL;
  kk=k+1;
  if (nr>0) {
    beg=GR[0][0];end=GR[0][1];
  }
  while (k<nr) {   
    if ((kk<nr)&&(GR[kk][0]-end)<JOIN) {
      beg=GR[k][0];
      end=GR[kk][1];
      kk++;
    }
    else if ((end-beg+1)<DEL) {
      k++;
      if (k<nr) {
	beg=GR[k][0];
	end=GR[k][1];
      }
    }
    else {
      mGR=realloc(mGR,(mnr+1)*sizeof(int*));
      mGR[mnr]=malloc(2*sizeof(int));
      mGR[mnr][0]=beg;
      mGR[mnr][1]=end;
      mnr++;
      k=kk;
      kk++;
      if (k<nr) {
	beg=GR[k][0];
	end=GR[k][1];
      }
    }    
  }


  for (i=0;i<nr;i++) free(GR[i]);
  free(GR);
  
  SEQ->ngr=mnr;
  SEQ->gr=mGR;


  
}  


void Get_Histo(P_STR *P, char *path, char *fn)
{
  FILE *f;
  char ln[ML];
  int i,nb, set;
  double v, min, max, cutoff, c; 
  char *fullfn;
  int sl;
  
  sl=strlen(path)+strlen(fn)+2;
  fullfn=malloc(sl*sizeof(char));
  sprintf(fullfn,"%s/%s",path,fn);

  if ((f=fopen(fullfn,"r"))==NULL) {
    printf("Could not open %s\n",fullfn);
    exit(1);
  }



  fscanf(f,"%*s %lf %lf %d\n",&min, &max, &nb);

  P->distro=malloc(nb*sizeof(double ));
  for (i=0;i<nb;i++) P->distro[i]=0;

  
  for (i=0,set=0;i<nb;i++) {
    fgets(ln,ML,f);
    if (feof(f)) break;
    if (ln[0]=='#') continue;
    
    sscanf(ln,"%*s %lf %*s %*s   %lf\n", &c,&v);
    if ((set==0)&&(v<=0.5)) {set=1;cutoff=c;}
    P->distro[i]=v;
  }

  fclose(f);
  P->max=max;
  P->min=min;
  P->nb=nb;
  P->cutoff=cutoff;	



  P->step=(max-min)/nb;
  P->cutoff-=P->step;

}


void read_ref(char *path, char *fn, double **REF)
{

  FILE *f;
  int p1,p2;
  double v;
  char line[1000],s[20];
  int i,j;
  char *fullfn;
  int sl;

  sl=strlen(path)+strlen(fn)+2;
  fullfn=malloc(sl*sizeof(char));
  sprintf(fullfn,"%s/%s",path,fn);

  if ((f=fopen(fullfn,"r"))==NULL) {
    printf("Could not open %s\n",fullfn);
    exit(1);
  }


  while (!feof(f)) {
    fgets(line,1000,f);

    sscanf(line,"%d",&p1);
    sscanf(&line[8],"%d",&p2);
    sscanf(&line[17],"%s",s);
    v=atof(s);
    REF[p1][p2]=v;

  }


  if (REF[9][9]<0) {
    for (i=0;i<AAN;i++) for (j=0;j<AAN;j++)
      REF[i][j]*=-1;
  }


  fclose(f);
  
}



void read_mat(char *path, char *fn, double **MAT, double *matave)
{
  FILE *f;
  char ln[ML];
  int numargs;
  char *args[AAN+2], AAL[AAN],a1,a2;
  int i,j,k ,p, q;
  double val;
  int sl;
  char *fullfn;

  sl=strlen(path)+strlen(fn)+2;
  fullfn=malloc(sl*sizeof(char));
  sprintf(fullfn,"%s/%s",path,fn);

  if ((f=fopen(fullfn,"r"))==NULL) {
    printf("Could not open %s\n",fullfn);
    exit(1);
  }

  
  fgets(ln,ML,f);
  sprintf(AAL,"%s",ln);
  i=0;
  while (!feof(f))   {
    fgets(ln,ML,f);
    k=0;j=0;
    if (feof(f)) break;
    
    
    if (ln[0] == '\n') continue;
    if (ln[0] == '#') continue;
    numargs = getargs(ln,args,AAN+2);
    

    
    for (j=0;j<numargs;j++)  {          
      val=atof(args[j]);
      a1=AAL[i];
      a2=AAL[j];
      p=strchr(AA,a1)-AA;
      q=strchr(AA,a2)-AA;
      
      MAT[p][q]=val;
      
    }
    i++; 	   
    
  } 

  *matave=0;
  for (i=0;i<AAN;i++) for (j=0;j<AAN;j++) *matave+=MAT[i][j];
  *matave/=(AAN*AAN);
    
  
  fclose(f);


}


int getargs(char *line,char *args[],int max)
{
  char *inptr;
  int i;
  
  inptr=line;
  for (i=0;i<max;i++)  {
    if ((args[i]=strtok(inptr," \t\n"))==NULL)
      break;
    inptr=NULL;
    
  }
  
  return(i);
	
}

void *my_malloc(size_t size)
{
 
  void    *new_mem;
 
  if (size == 0)
    return NULL;
  new_mem = malloc(size);
  if (new_mem == NULL) {
    fprintf(stderr, "can't allocate enough memory: %d bytes\n", size);
  }
  return new_mem;
}
 
 

double **DMatrix(int n_rows, int n_cols)
{
  double **matrix;
  int   i,j;
   
  matrix = (double **) my_malloc(n_rows*sizeof(double *));
  matrix[0] = (double *) my_malloc(n_rows*n_cols*sizeof(double));
   
  for (i = 1; i < n_rows; i++)
    matrix[i] = matrix[i-1] + n_cols;
  for (i=0;i<n_rows;i++) for (j=0;j<n_cols;j++) matrix[i][j]=0;
   
  return matrix;
                                                             
}

void Get_Seq(char *seq, SEQ_STR *SEQ)
{
  if ((seq==NULL)||(strlen(seq)==0)) {
    printf("No sequence provided, you bastard\n"),exit(1);
  }

  SEQ->seq=calloc(MSL,sizeof(char));
  strcpy(SEQ->seq,seq);
  SEQ->le=strlen(SEQ->seq);
  
}
