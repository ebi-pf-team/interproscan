#include "ncoilsServer.h"  /* TCP echo server includes */
#include <pthread.h>        /* for POSIX threads */
#include <sys/time.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>        /* for strncmp() */


/* 
=================
* Ncoils Daemon *
=================
This is a relatively simpe bit of code that wraps up the ncoils program written
by Rob Russell, which identifies coiled-coils in protein sequences. The process
forks early, with the parent process exiting and the child process continuing
'forever'.  The daemon listens on the specified port, and uses a separate 
thread to process each client connection.

Each thread reads the sequence form the socket and predicts the coiled-coils and
returns the results to the client connection.  The results are in a binary
format, which is an array of floats.  The size of the array will be the length
of the read sequences.



*/

void usage(char *);

int main(int argc, char *argv[])
{
    int servSock;                    /* Socket descriptor for server */
    unsigned short echoServPort;     /* Server port */
    pid_t processID;                 /* Process ID */
    FILE *efp;
    FILE *afp;                 /* Error and access files */
    FILE *seq_in;
    char *execname;
    char *pidpath;    
    char *logpath;
    int summarise_coords = 0, d_flag = 0, c;

    while ((c = getopt (argc, argv, "m:hdc")) != -1)
       switch (c) {
            case 'h':
                usage(argv[0]);
                exit(0);
                break;
            case 'm':
                // i5 comptability - matrix option no longer needed. added to prevent warnings
                break;
            case 'c':
                summarise_coords = 1;  // i5 output - list of start/end pairs
                break;
            case 'd':
                d_flag = 1; // daemon mode
                break;
        }

    if (! d_flag) {
        process_seq_stdin(summarise_coords); // run standalone - either printing a list of p values of the list of start/end for i5
        exit(0);
    } else { // deamonise
        if (argc - optind != 3) // Test for correct number of arguments
                                // optind is the number of real (excluding $0/-xxx) arguments, or the index into the first real argument
        {
            usage(argv[0]);
            exit(1);
        }
    }

    execname = "ncoilsd";
    pidpath = argv[optind + 1];
    logpath = argv[optind + 2];

    /* Open the logs */ 
    OpenLog(&efp, ERROR, logpath, execname);
    OpenLog(&afp, LOG, logpath, execname);
    
    echoServPort = atoi(argv[optind]);  /* First arg:  local port */
    LogMessage(afp, "Trying to listening on port %d\n", echoServPort); 
    servSock = CreateTCPServerSocket(echoServPort, efp);
    if(servSock == 0){
      LogDie(efp, "Failed to open sockect connection on port %d\n", echoServPort);
    }else{
      LogMessage(afp, "Successfully listening on port %d\n", echoServPort); 
    }

    processID = fork();

    if (processID < 0){
       LogMessage(efp, "fork() failed.\n");
    } else if (processID == 0) { /* If this is the child process */
       LogMessage(afp, "Forked with processID %d\n", processID);
       WritePidFile(pidpath, execname );
       ProcessMain(servSock, afp, efp);
    }

    LogMessage(afp, "Exiting parent process.\n");
    exit(0);  /* The child will carry on, while the parent exists out gracefully */
}



void ProcessMain (int servSock, FILE *lfp, FILE *efp) {
   int clntSock;                  /* Socket descriptor for client connection */
   pthread_t threadID;              /* Thread ID from pthread_create() */
   struct ThreadArgs *threadArgs;   /* Pointer to argument structure for thread */

   for (;;) /* run forever */
    {
        clntSock = AcceptTCPConnection(servSock, lfp, efp);
        
        /* Create separate memory for client argument */
        if ((threadArgs = (struct ThreadArgs *) malloc(sizeof(struct ThreadArgs))) == NULL){
            LogDie(efp, "malloc() failed for ThreadArgs!");
        }

        /* populate the newly generated struc */
        threadArgs->clntSock = clntSock;
        threadArgs->logfp = lfp;
        threadArgs->errfp = efp; 
       
        /* Create client thread and process the socket connection in the ThreadMain method*/
        if (pthread_create(&threadID, NULL, ThreadMain, (void *) threadArgs) != 0){
            LogDie(efp, "pthread_create() failed");
        }
        //ThreadMain( (void *) threadArgs);
    }
    /* NOT REACHED */
}

void *ThreadMain(void *threadArgs)
{
    /* Guarantees that thread resources are deallocated upon return */
//fprintf(stderr, "threadmain\n");
    pthread_detach(pthread_self()); 
    HandleTCPClient( threadArgs );
    free(threadArgs);              /* Deallocate memory for argument */
    return (NULL);
}

void usage(char *bin)
{
    fprintf(stderr, "noils - (C) Rob Russell\n");
    fprintf(stderr, "Modified by Robert Finn (EBI) to:\n");
    fprintf(stderr, "\tremove the dependency on an external matrix file\n");
    fprintf(stderr, "\tallow running in daemon mode\n");
    fprintf(stderr, "Usage %s:\n", bin);
    fprintf(stderr, "\t-d <SERVER PORT> <PID PATH> <LOG PATH> (server mode)\n");
    fprintf(stderr, "\t[-c] < <INPUT_FASTA> (standalone mode, -c for i5 compatability)\n");
}
