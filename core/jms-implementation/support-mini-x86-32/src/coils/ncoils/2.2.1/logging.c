#include <stdio.h>
#include <stdlib.h> /* for exit() */
#include <sys/time.h>
#include <stdarg.h>
#include <string.h>
#include <sys/file.h>   /* for file locking */
#include <time.h>

/* Structure of arguments to pass to client thread */

void LogMessage ( FILE *fp, char *format, ... )
{
  
  /* Workout which host we are one */
  char hostname[1024];
  hostname[1023] = '\0';
  gethostname(hostname, 1023);
  
  
  /* Workout the time */
  char theTime[30];
  struct timeval tv;
  time_t curtime;
  gettimeofday(&tv, NULL); 
  curtime=tv.tv_sec;

  

  /* This allows the formatting of time */
  strftime(theTime,30,"%Y-%m-%d %T", localtime(&curtime));
  
  /* Now print this time, hostname and message to the file */ 
  flock(fileno(fp), LOCK_EX); /* lock the log file */

  /* print out the  timestamp and host */
  fprintf(fp, "%s %s:",theTime, hostname);
  va_list args;
  va_start (args, format);
  vfprintf (fp, format, args);
  va_end (args);
  fflush(fp);

  flock(fileno(fp), LOCK_UN); /* unlock log file */
}


void LogDie ( FILE *fp, char *format, ...){
  va_list args;
  LogMessage(fp, format, args);
  exit(1);
}


void OpenLog(FILE **fp, int type, char *path, char *name){
    char logfile[1000];
    //fprintf(stderr, "Path: %s\n", path);
    //Change to a macro?
    if(type == 1){
      strcpy(logfile, path);
      strcat(logfile, "/");
      strcat(logfile, name);
      strcat(logfile, ".error.log");
      //fprintf(stderr, "Error log: %s\n", logfile);
      *fp = fopen(logfile, "a");
      if(fp == NULL){
        fprintf(stderr, "Failied to open error log, %s!\n", logfile);
        exit(1);
      }else{
        LogMessage(*fp, "Opened error log.\n");
      }
    }else{
      strcpy(logfile, path);
      strcat(logfile, "/");
      strcat(logfile, name);
      strcat(logfile, ".access.log");
      //fprintf(stderr, "Error log: %s\n", logfile);
      *fp = fopen(logfile, "a");
      if(fp == NULL){
        fprintf(stderr, "Failied to open error log, %s!\n", logfile);
        exit(1);
      }else{
        LogMessage(*fp, "Opened access log.\n");
      } 
    }
}

void WritePidFile (char *path, char *name){
  FILE *fp;
  char logfile[1000];
  pid_t pid;
  
  strcpy(logfile, path);
  strcat(logfile, "/");
  strcat(logfile, name);
  strcat(logfile, ".pid");
  
  fp = fopen(logfile, "w");
  if(fp == NULL){
    fprintf(stderr, "Failied to open pid file, %s!\n", logfile);
    exit(1);
  }else{
    /* get the process id */
    if ((pid = getpid()) < 0) {
      perror("Unable to get pid!");
    } else {
      fprintf(fp, "%d", pid);
      fclose(fp);
    }
  }
}


