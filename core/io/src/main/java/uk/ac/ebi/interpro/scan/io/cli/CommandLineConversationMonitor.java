package uk.ac.ebi.interpro.scan.io.cli;


import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A command line conversation is monitored to make sure interproscan does not have virtual memory spikes due to forking
 *
 * @author Gift Nuka
 *
 */

public class CommandLineConversationMonitor {

    private static volatile Logger LOGGER = Logger.getLogger(CommandLineConversationMonitor.class.getName());

    private static Long binaryRunDelay = 10l;

    static Long timeSinceLastBinaryRun = System.currentTimeMillis();

    static int binaryStepCount = 0;

    private static  boolean verboseLog = false;

    private static  int verboseLogLevel = 0;

    public static final Lock binaryRunLock = new ReentrantLock();

    public static String parentProcessName;

    private static boolean checkForkInProgress = true;

    static public Long getBinaryRunDelay() {
        return binaryRunDelay;
    }

    static public boolean isVerboseLog() {
        return verboseLog;
    }

    public void setVerboseLog(boolean verboseLog) {
        this.verboseLog = verboseLog;
    }

    public static int getVerboseLogLevel() {
        return verboseLogLevel;
    }

    public void setVerboseLogLevel(int verboseLogLevel) {
        CommandLineConversationMonitor.verboseLogLevel = verboseLogLevel;
    }

    public void setBinaryRunDelay(Long binaryRunDelay) {
        this.binaryRunDelay = binaryRunDelay;
    }

    public void setCheckForkInProgress(boolean checkForkInProgress) {
        CommandLineConversationMonitor.checkForkInProgress = checkForkInProgress;
    }

    public static void setParentProcessName(String parentProcessName) {
        if (CommandLineConversationMonitor.parentProcessName == null && parentProcessName != null ){
            CommandLineConversationMonitor.parentProcessName = parentProcessName.trim();
        }
    }



    /**
     * introduce a delay as specified by the binaryDelayRun value
     *
     */
    static public  void simpleBinaryRunDelay(String stepid){
        Long elapsedTime = System.currentTimeMillis() - timeSinceLastBinaryRun;
        binaryStepCount ++;
        LOGGER.debug("Binary Step # " + binaryStepCount + "(" + stepid +") elapsed time: " + elapsedTime);
        if(verboseLogLevel > 5)  {
            System.out.println(getTimeNow() + " Binary Step # " + binaryStepCount + "(" + stepid +") elapsed time: " + elapsedTime);
        }
        if(binaryRunDelay == 0){
            timeSinceLastBinaryRun = System.currentTimeMillis();
            return;
        }
        if(elapsedTime > binaryRunDelay){
            timeSinceLastBinaryRun = System.currentTimeMillis();
            return;
        }
        if(checkForkInProgress){
            long sleepInterval = binaryRunDelay / 3;
            if(sleepInterval < 10){
                sleepInterval = 10;            }
            long totalSleepTime = sleepInterval;
            if(elapsedTime < sleepInterval){
                try {
                    Thread.sleep(sleepInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            while(isForkHappening()){
                LOGGER.debug("ForkHappening - Wait on binary Step No." + binaryStepCount  + " " + stepid);
                try {
                    Thread.sleep(sleepInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                if(binaryRunDelay  < totalSleepTime){         // try binaryRunDelay * 2
                    break;
                }
                totalSleepTime += sleepInterval;
            }
        } else{
            simpleWait(binaryRunDelay - elapsedTime);
        }
        timeSinceLastBinaryRun = System.currentTimeMillis();
    }


    static public void simpleWait(){
        try {
            Thread.sleep(binaryRunDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    static public void simpleWait(Long delay){
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     *
     * TODO: resolve circular dependency with Utilities class
     * @return
     */
    static public boolean isForkHappening(){
        String processes = null;
        try {
            processes = getCurrentProcesses();
            return getJavaCmdCount(processes) > 0;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return true;

    }


    static public int getJavaCmdCount(String psOutput){
        int javaCommandCount = 0;
        String[] cmds = psOutput.split("\n");
        for (String cmd : cmds) {
            if (cmd.contains("java")) {
                javaCommandCount++;
            }
        }
        return javaCommandCount;
    }


    static public  String getCurrentProcesses() throws IOException,InterruptedException {
        String PID = getPid();
        Vector<String> commands=new Vector<String>();
        commands.add("ps -o pid,vsize,rss,cmd --ppid " + PID);
        String output = runBashCommand(commands);
        if (output != null) {
            LOGGER.debug(" current cmds: \n" + output);
            LOGGER.debug( " current java cmd count: " + getJavaCmdCount(output));
        }
        return output;
    }

    /**
     * run bash commands
     *
     * @param commands
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static String runBashCommand(Vector<String> commands) throws IOException,InterruptedException{
        Vector<String> bashcommand =new Vector<String>();
        bashcommand.add("/bin/bash");
        bashcommand.add("-c");
        bashcommand.addAll(commands);
        ProcessBuilder pb=new ProcessBuilder(bashcommand);
//        System.out.println("Running " + commands.toString());
        Process pr=pb.start();
        pr.waitFor();
        if (pr.exitValue() == 0) {
            BufferedReader outReader = null;
            String output = "";
            try {
                outReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                String line;
                while ((line = outReader.readLine()) != null) {
                    output += line + "\n";
                }
            }
            finally {
                if (outReader != null) {
                    outReader.close();
                }
            }
            return output;
        } else {
            System.out.println("Error in running " + commands.toString());
        }
        return null;
    }

    /**
     * Gets a string representing the pid of this program - Java VM
     */
    public static String getPid() throws IOException,InterruptedException {

        Vector<String> commands=new Vector<String>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("echo $PPID");
        ProcessBuilder pb=new ProcessBuilder(commands);

        Process pr=pb.start();
        pr.waitFor();
        if (pr.exitValue() == 0) {
            BufferedReader outReader = null;
            String pid = null;
            try {
                outReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                pid = outReader.readLine().trim();
            }
            finally {
                if (outReader != null) {
                    outReader.close();
                }
            }
            return pid;
        } else {
            System.out.println("Error while getting PID");
            return "";
        }
    }

    /**
     * display time now
     * @return
     */
    public static String getTimeNow(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
        String currentDate = sdf.format(cal.getTime());
        return currentDate;
    }

}
