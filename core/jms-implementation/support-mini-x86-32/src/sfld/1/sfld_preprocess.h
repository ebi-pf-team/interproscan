#ifndef __SFLD_PRE__
#define __SFLD_PRE__

#include <easel.h>
#include <esl_msafile.h>
#include <string.h>
#include <getopt.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <sys/types.h>
#include <sys/stat.h>

#include "sfld.h"

void build_hmm(char *, char *, char *);
void run_search(char *, char *, char *, char *, char *);
int file_exists_non_empty(char *fn);
void store_site_data(char *msa_fn, char *out_fn);
void get_options_pre(int argc, char **argv, int *hmmbuild, int *nosearch, char **hmmer_path, char **alignments, char **hmm, char **sites);
void show_help(char *);

#endif // __SFLD_PRE__
