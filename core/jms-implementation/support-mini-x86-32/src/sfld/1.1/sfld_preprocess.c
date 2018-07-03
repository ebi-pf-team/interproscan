#include "sfld_preprocess.h"

// Pre-process SFLD alignment/HMM and produce annotation of active sites
// sfld_preprocess -a SFLD.sto -s SFLD.annot
//
// Copyright (c) EMBL-EBI 2016.

int main(int argc, char **argv)
{
    struct feature *features = NULL;
    char *align_fn, *features_fn;
    int i;

    features_fn = align_fn = NULL;

    get_options_pre(argc, argv, &align_fn, &features_fn);

    if (! align_fn || ! features_fn) {
        show_help(argv[0]);
        return 0;
    }

    // store family site features
    store_site_data(align_fn, features_fn);

    free(align_fn);
    free(features_fn);
}


void get_options_pre(int argc, char **argv, char **alignments, char **sites)
{
    static struct option long_options[] =
    {
        {"help",         no_argument,       0,  'h' },
        {"nosearch",     no_argument,       0,  'S' }, // don't run search if output files exist
        {"alignments",   required_argument, 0,  'a' }, // SFLD alignments (prefixed with $SFLD_OUTPUT if set)
        {"sites",        required_argument, 0,  's' }, // SFLD alignments (prefixed with $SFLD_OUTPUT if set)
        {0,              0,                 0,   0  }
    };
    int long_index = 0;
    int opt;
    char *path;

    while ((opt = getopt_long(argc, argv,"a:s:h", 
                   long_options, &long_index )) != -1) {
        switch (opt) {
             case 'a' : if ((path = getenv("SFLD_LIB_DIR")) == NULL)
                            *alignments = strdup(optarg); 
                        else
                            path_concat(path, optarg, alignments);
                 break;
             case 's' : if ((path = getenv("SFLD_LIB_DIR")) == NULL)
                            *sites = strdup(optarg); 
                        else
                            path_concat(path, optarg, sites);
                 break;
             case 'h' : {
                        show_help(argv[0]);
                        exit(0);
             }
                 break;
             default: {
                        fprintf(stderr, "Unrecognised option '%c'\n", optopt);
                        show_help(argv[0]);
                        exit(1);
             }
        }
    }
}

int file_exists_non_empty(char *fn)
{
    struct stat buf;

    if (stat(fn, &buf) == -1) {
        return 0;
    }
    if (buf.st_size > 0)
        return 1;
    else
        return 0;
}


void path_concat(char *p, char *f, char **n)
{
    *n = (char *)malloc(strlen(p) + 2 + strlen(f));
    strcpy(*n, p);
    strcpy(*n + strlen(p), "/");
    strcpy(*n + strlen(p) + 1, f);
}


void show_help(char *progname)
{
   printf("Pre-process SFLD alignments/HMMs\n");
   printf("Usage %s: options:\n", progname);
   printf("\t--hmm         | -m FILE    HMM file (input)\n");
   printf("\t--sites       | -s FILE    sites file (output)\n");
   printf("\t--alignments  | -a FILE    alignments file (input)\n");
   printf("\t--hmm_dir     | -d DIR     SFLD HMM directory (overrides $SFLD_LIB_DIR)\n");
   printf("\n");
}


void store_site_data(char *msa_fn, char *out_fn)
{
    ESLX_MSAFILE *msaf = NULL;
    ESL_MSA *msa = NULL;
    FILE *fp;
    int count;
    int i, j, rv;
    char *gcline;
    char *p;
    time_t t;
    char timestr[21];
    struct tm *tm;

    if ((fp = fopen(out_fn, "w")) == NULL) {
        fprintf(stderr, "Unable to write site info to '%s'\n", out_fn);
    }

    if (eslx_msafile_Open(NULL, msa_fn, NULL, eslMSAFILE_STOCKHOLM, NULL, &msaf) != eslOK) {
        fprintf(stderr, "ESL: Error opening alignments file '%s'\n", msa_fn);
        exit (1);
    }

    fprintf(fp, "## MSA feature annotation file\n");
    fprintf(fp, "# Format version: %s\n", SFLD_ANNOT_VERSION);
    if ((p = rindex(msa_fn, '/')) == NULL)
        fprintf(fp, "# MSA file: %s\n", msa_fn);
    else
        fprintf(fp, "# MSA file: %s\n", ++p);
    time(&t);
    tm = localtime(&t);
    strftime(timestr, 21, "%Y-%m-%d %H:%M:%S", tm);
    fprintf(fp, "# Date %s\n", timestr);

    while ((rv = eslx_msafile_Read(msaf, &msa)) != eslEOF) {
        if (rv != eslOK) {
            fprintf(stderr, "Error reading from '%s'\n", msa_fn);
            exit(1);
        }
        fprintf(fp, "ACC %s %d\n", msa->acc, msa->ngc);
        for (i = 0; i < msa->ngc; i++) {
            count = 0;
            gcline = msa->gc[i];
            for (j = 0; j < strlen(gcline); j++) {
                if (gcline[j] != '.')
                    count++;
            }
            fprintf(fp, "FEAT %s %d", msa->gc_tag[i], count);
            for (j = 0; j < strlen(gcline); j++) {
                if (gcline[j] != '.')
                    fprintf(fp, " %c %d", gcline[j], j);
            }
            fprintf(fp, "\n");
        }
        esl_msa_Destroy(msa);
    }

    eslx_msafile_Close(msaf);
    fclose(fp);
}
