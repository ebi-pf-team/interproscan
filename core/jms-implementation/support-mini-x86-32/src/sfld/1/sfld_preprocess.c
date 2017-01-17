#include "sfld_preprocess.h"

// Pre-process SFLD alignment/HMM and produce annotation of active sites
// sfld_preprocess -a SFLD.sto -m SFLD.hmm -s SFLD.annot
//
// Copyright (c) EMBL-EBI 2016.

int main(int argc, char **argv)
{
    struct feature *features = NULL;
    char *align_fn, *hmmer_path, *hmm_fn, *features_fn;
    int i;
    int hmm_build = 0, no_search = 0;

    features_fn = hmmer_path = hmm_fn = align_fn = NULL;

    get_options_pre(argc, argv, &hmm_build, &no_search, &hmmer_path, &align_fn, &hmm_fn, &features_fn);

    if (! align_fn || ! hmm_fn || ! features_fn) {
        show_help(argv[0]);
        return 0;
    }

    if (! hmmer_path) {
        fprintf(stderr, "HMMER path not defined - set $HMMER_PATH or use -h\n");
        return 0;
    }

    // hmmbuild, if not done so already
    if (hmm_build || ! file_exists_non_empty(hmm_fn))
        build_hmm(hmmer_path, align_fn, hmm_fn);

    // store family site features
    store_site_data(align_fn, features_fn);

    free(align_fn);
    free(hmm_fn);
    free(features_fn);
    if (hmmer_path)
        free(hmmer_path);
}


void build_hmm(char *path, char *sto, char *hmm)
{
    char *cmd;
    int ret;

    cmd = (char *)malloc(2048); // FIXME
    sprintf(cmd, "%s/hmmbuild --hand -o /dev/null %s %s", path, hmm, sto);
    if (ret = system(cmd)) {
        fprintf(stderr, "Error running %s\n", cmd);
        free(cmd);
        exit (1);
    }
    free(cmd);
}

void run_search(char *path, char *tblout, char *alignout, char *hmm, char *db)
{
    char *cmd;
    int ret;
    
    cmd = (char *)malloc(2048); // FIXME
    sprintf(cmd, "%s/hmmsearch --cut_ga -o /dev/null --domtblout %s -A %s %s %s", path, tblout, alignout, hmm, db);
    if (ret = system(cmd)) {
        fprintf(stderr, "Error running %s\n", cmd);
        free(cmd);
        exit (1);
    }
    free(cmd);
}

void get_options_pre(int argc, char **argv, int *hmmbuild, int *nosearch, char **hmmer_path, char **alignments, char **hmm, char **sites)
{
    static struct option long_options[] =
    {
        {"help",         no_argument,       0,  'h' },
        {"hmmbuild",     no_argument,       0,  'b' }, // force building of hmm even if it exists
        {"nosearch",     no_argument,       0,  'S' }, // don't run search if output files exist
        {"hmmerpath",    required_argument, 0,  'p' }, // path to hmm* binaries (overrides $HMMER_PATH)
        {"alignments",   required_argument, 0,  'a' }, // SFLD alignments (prefixed with $SFLD_OUTPUT if set)
        {"hmm",          required_argument, 0,  'm' }, // SFLD alignments (prefixed with $SFLD_OUTPUT if set)
        {"sites",        required_argument, 0,  's' }, // SFLD alignments (prefixed with $SFLD_OUTPUT if set)
        {0,              0,                 0,   0  }
    };
    int long_index = 0;
    int opt;
    char *path;

    while ((opt = getopt_long(argc, argv,"p:a:s:m:bSh", 
                   long_options, &long_index )) != -1) {
        switch (opt) {
             case 'H' : *hmmbuild = 1;
                 break;
             case 'S' : *nosearch = 1;
                 break;
             case 'a' : if ((path = getenv("SFLD_LIB_DIR")) == NULL)
                            *alignments = strdup(optarg); 
                        else
                            path_concat(path, optarg, alignments);
                 break;
             case 'm' : if ((path = getenv("SFLD_LIB_DIR")) == NULL)
                            *hmm = strdup(optarg); 
                        else
                            path_concat(path, optarg, hmm);
                 break;
             case 's' : if ((path = getenv("SFLD_LIB_DIR")) == NULL)
                            *sites = strdup(optarg); 
                        else
                            path_concat(path, optarg, sites);
                 break;
             case 'p' : *hmmer_path = strdup(optarg);
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
    if (*hmmer_path == NULL && ((path = getenv("HMMER_PATH")) != NULL))
        *hmmer_path = strdup(path);
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
   printf("\t--hmmbuild    | -H         force building of hmm even if it exists\n");
   printf("\t--nosearch    | -S         don't run search if output files exist\n");
   printf("\t--hmmerpath   | -p PATH    path to hmm* binaries (overrides $HMMER_PATH)\n");
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
    fprintf(fp, "# Format version: 1.0\n");
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
