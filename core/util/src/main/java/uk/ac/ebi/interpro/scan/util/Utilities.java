package uk.ac.ebi.interpro.scan.util;

import org.apache.log4j.Logger;
//import uk.ac.ebi.interpro.scan.io.OsUtils;
//import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversation;
//import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversationImpl;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

/**
 * @author Gift Nuka
 *
 */
public class Utilities {

    private static volatile Logger LOGGER = Logger.getLogger(Utilities.class.getName());

    public static boolean verboseLog = false;

    public static int verboseLogLevel = 0;

    public static String mode = "standalone";

    public static Boolean lookupMatchVersionProblemMessageDisplayed = false;

    public static int sequenceCount = 0;

    public static int logBase = 10;

    public static String createUniqueJobName(int jobNameLength) {
        StringBuffer sb = new StringBuffer();
        for (int x = 0; x < jobNameLength; x++) {
            sb.append((char) ((int) (Math.random() * 26) + 97));
        }
        return sb.toString();
    }

    /**
     * sleep for the specified milliseconds
     * @param sleepTime
     */
    public static void sleep(int sleepTime) {
        try {
            Thread.sleep(1 * sleepTime);                 //1000 milliseconds is one second.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public static double logOfBase(int base, int num) {
        return Math.log(num) / Math.log(base);
    }

    public static Long getWaitTimeFactor(int matchCount){
        double logv = logOfBase(logBase,matchCount);
        long waitTimeFactor = 1;
        if (Math.round(logv) > 1){
            waitTimeFactor = Math.round(logv);
        }
        return waitTimeFactor ;
    }

    public static Long getWaitTimeFactorLogE(int matchCount){
        double logv = Math.log(matchCount);
        long waitTimeFactor = 1;
        if (Math.round(logv) > 1){
            waitTimeFactor = Math.round(logv);
        }
        return waitTimeFactor ;
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

    public static void setMode(String mode) {
        Utilities.mode = mode;
    }

    public static String getMode(){
        return mode;
    }

    public static void setSequenceCount(int sequenceCount){
        sequenceCount = sequenceCount;
    }

    public static int getSequenceCount(){
        return sequenceCount;
    }

    /**
     * return true if running in single seq mode
     * @return
     */
    public static boolean isRunningInSingleSeqMode(){
        if (mode.equals("singleseq")) {
           return true;
        }
        return false;
    }

    /**
     * Lock a given file
     * @param filename
     * @return
     */
    public static boolean lock(File filename){
        boolean fileLockSucceeded = false;
        File lockFile =  new File(filename+".filelock");
        try {
            while(!fileLockSucceeded){
                if(!lockFile.exists()){
                    final String PID = getPid();
                    BufferedWriter out = new BufferedWriter(new FileWriter(lockFile));
                    out.write(PID);
                    out.close();
                    fileLockSucceeded = true;
                }else{
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return fileLockSucceeded;
    }

    public static boolean tryLock(File filename){
        boolean fileLockSucceeded = false;
        File lockFile =  new File(filename+".filelock");
        try {
            while(!fileLockSucceeded){
                if(!lockFile.exists()){
                    final String PID = getPid();
                    BufferedWriter out = new BufferedWriter(new FileWriter(lockFile));
                    out.write(PID);
                    out.close();
                    fileLockSucceeded = true;
                }else{
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return fileLockSucceeded;
    }

    /**
     * release the lock
     * @param filename
     * @return
     */
    public static boolean releaseLock(File filename){
        File lockFile =  new File(filename+".filelock");
        if(!lockFile.exists()){
            try {
                final String PID = getPid();
                BufferedReader in = null;
                in = new BufferedReader(new InputStreamReader(new FileInputStream(lockFile)));
                String line = null;
                while ((line = in.readLine()) != null) {
                    line += line;
                }
                if(line.contains(PID.trim())) {
                    //delete lock file
                    lockFile.delete();
                    return true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }else{
            //lock file exists, cannot execute the logic that we wanted
            return false;
        }
        return false;
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
            BufferedReader outReader=new BufferedReader(new InputStreamReader(pr.getInputStream()));
            return outReader.readLine().trim();
        } else {
            System.out.println("Error while getting PID");
            return "";
        }
    }

    /**
     * Gets a string representing the pid of this program - Java VM
     */
    public static String getSwapMemoryDetails(String PID) throws IOException,InterruptedException {

        String [] memoryStats;
        Vector<String> commands=new Vector<String>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("'ps");
        commands.add("-p ");
        commands.add(PID);
        commands.add("-o");
        commands.add("vsz");
        commands.add("--no-header'");
//        commands.add("echo ps -p "+ PID + " -o vsz,rss | sed 1d");
        ProcessBuilder pb = new ProcessBuilder(commands);
//        System.out.println(" Command is : " + pb.command());

        Process pr = pb.start();
        pr.waitFor();
        if (pr.exitValue()==0) {
            BufferedReader outReader=new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String output = outReader.readLine().trim();

            System.out.println(" String is : " + output);
            memoryStats = output.split(" ");
            int virtualMemory = Integer.parseInt(memoryStats[0]);
            int residentMemory = 1; //Integer.parseInt(memoryStats[1]);
            double percentMemory = 1; //Double.parseDouble(memoryStats[2]);
            int swapEstimate =  virtualMemory -  residentMemory;
            return "ps output (MB) - VSZ: " + virtualMemory / 1024 + " RSS: " + residentMemory / 1024 + " SWAP?: " +  swapEstimate / 1024 +" %MEM: " + percentMemory;
        } else {
            System.out.println("Error...");
            return "";
        }
    }
    /**
     * Gets a string representing the pid of this program - Java VM
     */
    public static String getSwapMemoryDetailsCLC(String PID) throws IOException,InterruptedException {

        String [] memoryStats;
        Vector<String> commands=new Vector<String>();
//        final CommandLineConversation clc = new CommandLineConversationImpl();
//        String output = "";
//        String command ="ps -p "+ PID + " -v --no-header";
//        ProcessBuilder pb = new ProcessBuilder(command.split(" "));
//        Process pr = pb.start();
//        pr.waitFor();
//        if (pr.exitValue()==0) {
//            BufferedReader outReader=new BufferedReader(new InputStreamReader(pr.getInputStream()));
//            output = outReader.readLine().trim();
//            memoryStats = output.trim().split("\\s+");
//            int virtualMemory = Integer.parseInt(memoryStats[6]);
//            int residentMemory = Integer.parseInt(memoryStats[7]);
//            double percentMemory = Double.parseDouble(memoryStats[8]);
//            int swapEstimate = virtualMemory - residentMemory;
//
//            if ((virtualMemory / (1024 * 1024)) > 12 )  {
//                System.out.println(" MemoryError : Virtual memory larger than expected" );
//            }
//            return "ps output (MB) - VSZ: " + virtualMemory / 1024 + " RSS: " + residentMemory / 1024 + " SWAP?: " + swapEstimate / 1024 + " %MEM: " + percentMemory;
//        }

        return "";
    }

    /**
     * run the 'free -m' command to get system swap and memory usage
     */
    public static String runFreeCmd() throws IOException,InterruptedException {
        Vector<String> commands=new Vector<String>();
        commands.add("free -m");
        String output = runBashCommand(commands);
        if (output != null) {
            System.out.println("free -m output: \n" + output);
        }
        return "";
    }

    /**
     * run the vmstat command to get more swap and memeory usage
     */
    public static String runVmstatCmd() throws IOException,InterruptedException {
        Vector<String> commands=new Vector<String>();
        commands.add("vmstat -t -S m");
        String output = runBashCommand(commands);
        if (output != null) {
            System.out.println("vmstat -t -S m output: \n" + output);
        }
        return "";
    }

    /**
     * get vm stats from /proc
     */
    public static String getProcStatus() throws IOException,InterruptedException {
        String PID = getPid();
        Vector<String> commands=new Vector<String>();
        commands.add("cat /proc/" + PID + "/status");
        String output = runBashCommand(commands);
        if (output != null) {
            System.err.println(getTimeNow() + " process stats: ");
            System.err.println(output);
        }
        return "";
    }

    /**
     * get vm stats from /proc
     */
    public static String getProcSelfStatus() throws IOException,InterruptedException {
        Vector<String> commands=new Vector<String>();
        commands.add("cat /proc/self/status");
        String output = runBashCommand(commands);
        if (output != null) {
            System.out.println(output);
        }
        return "";
    }

    /**
     * get vm stats from /proc
     */
    public static String runBjobs(String runId) throws IOException,InterruptedException {
        Vector<String> commands =new Vector<String>();
        commands.add("bjobs -l -J " + runId + " | grep SWAP");
        String output = runBashCommand(commands);
        if (output != null) {
            System.out.println("bjobs -l -J " + runId + " | grep SWAP = " + output);
        }
        return "";
    }


    static public  String getCurrentProcess() throws IOException,InterruptedException {
        String PID = getPid();
        Vector<String> commands=new Vector<String>();
        commands.add("ps -o cmd= -p " + PID);
        String output = runBashCommand(commands);
        if (output != null) {
            LOGGER.debug(commands + " current comd: \n" + output);
        }
        return output;
    }


    static public  List<String> getChildProcesses() throws IOException,InterruptedException {
        String PID = getPid();
        Vector<String> commands=new Vector<String>();
        commands.add("ps -o pid= --ppid " + PID);
        String output = runBashCommand(commands);
        List<String> list = new ArrayList<String>();
        if (output != null) {
            String [] outputList =  output.split("\n");
            list = new ArrayList<String>(outputList.length);
            System.out.println(commands + " current processes: \n" + output);
            for (String pid : outputList) {
                list.add(pid.trim());
            }
        }
        return list;
    }

    static public List <String> getNewChildProcesses(List<String> oldPIDList, List<String> newPIDList){
        newPIDList.removeAll(oldPIDList);
        return newPIDList;
    }


    static public  String getProcessCmd(String PID) throws IOException,InterruptedException {
        Vector<String> commands=new Vector<String>();
        commands.add("ps -o cmd= -p " + PID);
        String output = runBashCommand(commands);
        if (output != null) {
            LOGGER.debug(commands + " comd name: \n" + output);
        }
        return output;
    }

    static public  String getCurrentProcesses() throws IOException,InterruptedException {
        String PID = getPid();
        Vector<String> commands=new Vector<String>();
        commands.add("ps -o pid,vsize,rss,command -p " + PID);
        String output = runBashCommand(commands);
        if (output != null) {
            LOGGER.debug(" current cmds: \n" + output);
            LOGGER.debug( " current java cmd count: " + getJavaCmdCount(output));
        }
        return output;
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
        return false;

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
            BufferedReader outReader=new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String output = "";
            String line = null;
            while ((line = outReader.readLine()) != null) {
                output += line + "\n";
            }
            return output;
        } else {
            System.out.println("Error in running " + commands.toString());
        }
        return null;
    }

    //verbose output using System out
    public static void verboseLog(String out){
        if(verboseLogLevel > 0){
            System.out.println(Utilities.getTimeNow() + " " + out);
        }
    }

    //verbose output using System out
    public static void verboseLog(int level, String out){
        if(verboseLogLevel >= level){
            System.out.println(Utilities.getTimeNow() + " " + out);
        }
    }

}
