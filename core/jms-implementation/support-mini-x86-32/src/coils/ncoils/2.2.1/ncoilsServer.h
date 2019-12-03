#include <stdio.h>      /* for printf() and fprintf() */
#include <sys/socket.h> /* for socket(), bind(), and connect() */
#include <arpa/inet.h>  /* for sockaddr_in and inet_ntoa() */
#include <stdlib.h>     /* for atoi() and exit() */
#include <string.h>     /* for memset() */
#include <unistd.h>     /* for close() */

#define  ERROR 1
#define  LOG   0

/* Sockets stuff */
void HandleTCPClient(void *threadArgs);   /* TCP client handling function */
int CreateTCPServerSocket(unsigned short port, FILE *efp); /* Create TCP server socket */
int AcceptTCPConnection(int servSock, FILE *lfp, FILE *efp);  /* Accept TCP connection request */

/* Logging Stuff */
void LogMessage(FILE *fp, char *format, ...); /*Write the log message out to the file */
void LogDie(FILE *fp, char *format, ...); /*Write the log message out to the file */
void OpenLog(FILE **fp, int type, char *path, char *name);
void WritePidFile (char *path, char *name);

void *ThreadMain(void *threadArgs);            /* Main program of a thread */
void ProcessMain(int servSock, FILE *lfp, FILE *efp);         /* Main program of process */

/* Structure of arguments to pass to client thread */
struct ThreadArgs
{
    int clntSock;                      /* Socket descriptor for client */
    FILE * logfp;
    FILE * errfp;
};


