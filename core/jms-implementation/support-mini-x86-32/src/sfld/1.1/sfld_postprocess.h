#ifndef __SFLD_POST__
#define __SFLD_POST__

#include <easel.h>
#include <esl_msafile.h>
#include <string.h>
#include <getopt.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>

#include "sfld.h"

struct family // one per SFLD accession
{
    char *name; // feature name
    int n_features; // from the GC line
    int n_sites; // from the GC line
    int *site_pos; // from the GC line
    char **site_desc;
    char **site_residue;
};

struct feature // one per SFLD feature
{
    char *name; // feature name
    char *states; // from the GC line
    char *match_states;
    int n_residues; // #(residues) (i.e. non ".") in the feature
};

struct seq_matches // one per feature per sequence
{
    int has_matched; // flag sequence matched
    char *residue_matches; // list of residues on the sequence at each match position
    int *residue_match_coords;
};

struct site_match
{
    char *matches;
    char *query_ac;
    char *model_ac;
    int n_match_lines;
    char **match_lines;
};

struct no_hit // store mismatches
{
    char *query_ac;
    char *model_ac;
};

struct hmmer_dom // used to read the lines from the dom table and store hits
{
    char query_ac[1001];
    char model_ac[1 + SFLD_NAME_LEN];
    float seq_evalue;
    float seq_score;
    float seq_bias;
    float dom_ievalue;
    float dom_cevalue;
    float dom_score;
    float dom_bias;
    int hmm_start;
    int hmm_end;
    int ali_start;
    int ali_end;
    int env_start;
    int env_end;
    float accuracy;
};

int get_start_from_nse(char *);
void read_family_data(ESL_MSA *, int *, struct feature **, int *);
void rf_to_array(ESL_MSA *, int *, int);
void identify_site_matches(char *aln_fn, struct family *families, int n_families, struct site_match **site_hits, int *n_site_hits, int *ali_present, struct no_hit **, int *);
void get_site_matches(struct family family, ESL_MSA *msa, struct site_match **site_hits, int *n_site_hits, struct no_hit **, int *);
void output_dom_sites_by_query(struct hmmer_dom *, int, struct site_match *, int);
void output_dom_sites_as_tab(struct hmmer_dom *, int, struct site_match *, int);
void read_domtblout(char *, struct hmmer_dom **, int *);
void family_ids_from_hmmer_out(char *, char ***, int *);
void get_options_post(int argc, char **argv, int *only_matches, char **hmmerout, char **output, char **dom_file, char **alignments, char **site_info, char **format);
void filter_no_hits(struct hmmer_dom *dom_hits, int n_dom_hits, struct no_hit *no_hits, int n_no_hits);
void strip_dom_se(struct hmmer_dom *dom_hits, int n_dom_hits);
int parse_hmmer_dom(struct hmmer_dom *, char *);
int cmp_site_query_model(const void *, const void *);
int cmp_dom_query_model(const void *, const void *);
int cmp_nohit_query_model(const void *, const void *);
int cmp_match_pair(char *query_1, char *model_1, char *query_2, char *model_2);
void retrieve_families_with_ali(char *fn, int **ali_present, int *nfam);
void read_site_data(char *fn, int *n_fam, struct family **families);
void output_dom_hit(struct hmmer_dom hit, int);
void output_site_hit(struct site_match hit, int);
void free_site_hits(int n, struct site_match *hits);
void free_no_hits(int n, struct no_hit *hits);
void clear_family_data(int n, struct family *fams);
void build_site_match_strings(struct family family, int *coords, char *residues, int *n_lines, char ***lines);

void show_help(char *);

#endif // __SFLD_POST__
