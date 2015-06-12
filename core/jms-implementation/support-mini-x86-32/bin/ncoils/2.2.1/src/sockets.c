#include "ncoilsServer.h"
#include <stdio.h>      /* for printf() and fprintf() */ 
#include <sys/socket.h> /* for recv() and send() */
#include <unistd.h>     /* for close() */
#include <arpa/inet.h>  /* for sockaddr_in and inet_ntoa() */
#include <netinet/in.h>
#include <string.h>     /* for memset() */
#include <stdlib.h>

#define RCVBUFSIZE 1024   /* Size of receive buffer */
#define MAXPENDING 5    /* Maximum outstanding connection requests */


void HandleTCPClient(void *threadArgs)
{
    char buff[RCVBUFSIZE];        /* Buffer for echo string */
    int recvMsgSize;                    /* Size of received message */
    int resSize;
    float *res;
    int clntSocket;  
    FILE *lfp;
    FILE *efp;
    char *seq,*title,*ident;
    int readingHead = 0;
    int i, seqlen, j; 
     
    /* Extract socket file descriptor from argument */
    clntSocket = ((struct ThreadArgs *) threadArgs) -> clntSock;
    lfp      = ((struct ThreadArgs *) threadArgs) -> logfp;
    efp      = ((struct ThreadArgs *) threadArgs) -> errfp;

    /* Receive message from client */
    if ((recvMsgSize = recv(clntSocket, buff, RCVBUFSIZE, 0)) < 0)
        LogDie(efp, "recv() failed");


    /* Now we have the sequence in the echoBuffer*/


     
    ident = (char*) malloc(100*sizeof(char));
    title = (char*) malloc(100000*sizeof(char));
    seq   = (char*) malloc(100000*sizeof(char));
    seqlen = 0;
    seq[0]='\0';
    
    /* Send received string and receive again until end of transmission */
    while (recvMsgSize > 0)      /* zero indicates end of transmission */
    {
      for(i=0; i<recvMsgSize; ++i){ 
        if(buff[i]=='\n' || buff[i]=='\r'){
           buff[i]='\0';
           readingHead = 0;
        }
        
        if(buff[0]=='>' || (readingHead == 1) ) {
            readingHead = 1;
            /* Try and read the identifier */
            if(buff[0] == '>'){
              i=1;
              while((buff[i]!=' ') && (buff[i]!='\0') && (buff[i]!='\n') && (buff[i]!='\r')) { 
                 ident[i-1]=buff[i]; 
                i++; 
              }
              ident[i-1]='\0';
            }
            j=0;
            
            /* Is there anything beyond the accession/identifier.....*/
            if(buff[i]!='\n'){
              i++;
              while(buff[i]!='\n' && buff[i]!='\0' && buff[i]!='\r') {
                title[j]=buff[i]; i++;
                j++;
              }
            }
            title[j]='\0';
            if(buff[0] == '>') buff[0] = '*';
            if(buff[i]=='\n') readingHead=0;
        } else {
           readingHead = 0;
           if(buff[i]!='\n' && buff[i]!='\0' && buff[i]!='\r'){
            strncpy(&seq[seqlen], &buff[i], 1);
            seq[seqlen + 1] = '\0';
            seqlen=strlen(seq);
           }
        }
      }
      if(recvMsgSize != RCVBUFSIZE) break;  

      /* See if there is more data to receive */
      if ((recvMsgSize = recv(clntSocket, buff, RCVBUFSIZE, 0)) < 0)
         LogDie(efp, "recv() failed");
    }
    
    LogMessage( lfp, "Query: %s\n", seq);
   
  
    seqlen=strlen(seq);
    resSize = seqlen*sizeof(float);
	  res = (float *) malloc(resSize);
    predict(seq, &res, 3); // 3 - no output from coils

   
    /* Echo message back to client */
    //fprintf(stderr, "Results size is  %d", resSize);
    //Try with just res;
    if (write(clntSocket, res, resSize) < 0)
      LogDie(efp, "send() failed");
    
    free(res);
    free(seq);
    free(ident);
    free(title);
    close(clntSocket);    /* Close client socket */
}

int AcceptTCPConnection(int servSock, FILE *lfp, FILE *efp)
{
    int clntSock;                    /* Socket descriptor for client */
    struct sockaddr_in echoClntAddr; /* Client address */
    unsigned int clntLen;            /* Length of client address data structure */

    /* Set the size of the in-out parameter */
    clntLen = sizeof(echoClntAddr);
    
    /* Wait for a client to connect */
    if ((clntSock = accept(servSock, (struct sockaddr *) &echoClntAddr, &clntLen)) < 0)
        LogDie(efp, "accept() failed");
    
    /* clntSock is connected to a client! */
    LogMessage(lfp, "Handling client from %s\n", inet_ntoa(echoClntAddr.sin_addr));
    return clntSock;
}


int CreateTCPServerSocket(unsigned short port, FILE *efp)
{
    int sock;                        /* socket to create */
    struct sockaddr_in echoServAddr; /* Local address */

    /* Create socket for incoming connections */
    if ((sock = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0)
        LogDie(efp, "socket() failed");
      
    /* Construct local address structure */
    memset(&echoServAddr, 0, sizeof(echoServAddr));   /* Zero out structure */
    echoServAddr.sin_family = AF_INET;                /* Internet address family */
    echoServAddr.sin_addr.s_addr = htonl(INADDR_ANY); /* Any incoming interface */
    echoServAddr.sin_port = htons(port);              /* Local port */

    /* Bind to the local address */
    if (bind(sock, (struct sockaddr *) &echoServAddr, sizeof(echoServAddr)) < 0)
        LogDie(efp, "bind() failed");

    /* Mark the socket so it will listen for incoming connections */
    if (listen(sock, MAXPENDING) < 0)
        LogDie(efp, "listen() failed");

    return sock;
}
