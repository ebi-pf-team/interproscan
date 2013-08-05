package uk.ac.ebi.interpro.scan.jms.stats;

import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversation;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversationImpl;
import uk.ac.ebi.interpro.scan.io.cli.FileIsNotADirectoryException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author Gift Nuka
 *
 */
public class Utilities {


    public static String createUniqueJobName(int jobNameLength) {
        StringBuffer sb = new StringBuffer();
        for (int x = 0; x < jobNameLength; x++) {
            sb.append((char) ((int) (Math.random() * 26) + 97));
        }
        return sb.toString();
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
        final CommandLineConversation clc = new CommandLineConversationImpl();

//        commands.add("/bin/bash");
//        commands.add("-c");
//        commands.add("'ps -p "+ PID + " -o vsz,rss --no-header'");
        String output = "";
        String command ="ps -p "+ PID + " -v --no-header";
        ProcessBuilder pb = new ProcessBuilder(command.split(" "));
        Process pr = pb.start();
        pr.waitFor();
        if (pr.exitValue()==0) {
            BufferedReader outReader=new BufferedReader(new InputStreamReader(pr.getInputStream()));
            output = outReader.readLine().trim();

//            System.out.println(" Command is : " + pb.command());
            //int exitStatus = clc.runCommand(command);
            //String clcOutput = clc.getOutput();
            //String error = clc.getErrorMessage();
            memoryStats = output.trim().split("\\s+");
           // System.out.println(" Output is  " + memoryStats.length + " long : "+ output);
           // System.out.println(" Error is : " + error);
            int virtualMemory = Integer.parseInt(memoryStats[6]);
            int residentMemory = Integer.parseInt(memoryStats[7]);
            double percentMemory = Double.parseDouble(memoryStats[8]);
            int swapEstimate = virtualMemory - residentMemory;

            if ((virtualMemory / (1024 * 1024)) > 12 )  {
                System.out.println(" MemoryError : Virtual memory larger than expected" );
            }
            return "ps output (MB) - VSZ: " + virtualMemory / 1024 + " RSS: " + residentMemory / 1024 + " SWAP?: " + swapEstimate / 1024 + " %MEM: " + percentMemory;
        }

        return "";
    }

    /**
     * run the 'free -m' command to get system swap and memory usage
     */
    public static String runFreeCmd() throws IOException,InterruptedException {

        Vector<String> commands=new Vector<String>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("free -m");
        ProcessBuilder pb=new ProcessBuilder(commands);

        Process pr=pb.start();
        pr.waitFor();
        if (pr.exitValue() == 0) {
            BufferedReader outReader=new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String output = "";
            String line = null;
            while ((line = outReader.readLine()) != null) {
                output += line + "\n";
            }
            System.out.println("free -m output: \n" + output);
        } else {
            System.out.println("Error in running 'free -m'");

        }
        return "";
    }
    /**
     * run the vmstat command to get more swap and memeory usage
     */
    public static String runVmstatCmd() throws IOException,InterruptedException {

        Vector<String> commands=new Vector<String>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("vmstat -t -S m");
        ProcessBuilder pb=new ProcessBuilder(commands);

        Process pr=pb.start();
        pr.waitFor();
        if (pr.exitValue() == 0) {
            BufferedReader outReader=new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String output = "";
            String line = null;
            while ((line = outReader.readLine()) != null) {
                output += line + "\n";
            }
            System.out.println("vmstat -t -S m output: \n" + output);
        } else {
            System.out.println("Error in running 'free -m'");

        }
        return "";
    }

    /**
     * get vm stats from /proc
     */
    public static String getProcStatus() throws IOException,InterruptedException {

        String PID = getPid();
        Vector<String> commands=new Vector<String>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("cat /proc/" + PID + "/status");
        ProcessBuilder pb=new ProcessBuilder(commands);

        Process pr=pb.start();
        pr.waitFor();
        if (pr.exitValue() == 0) {
            BufferedReader outReader=new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String output = "";
            String line = null;
            while ((line = outReader.readLine()) != null) {
                output += line + "\n";
            }
            System.out.println(output);
        } else {
            System.out.println("Error in running 'cat /proc/" + PID + "/status'");

        }
        return "";
    }

    /**
     * get vm stats from /proc
     */
    public static String getProcSelfStatus() throws IOException,InterruptedException {

        Vector<String> commands=new Vector<String>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("cat /proc/self/status");
        ProcessBuilder pb=new ProcessBuilder(commands);

        Process pr=pb.start();
        pr.waitFor();
        if (pr.exitValue() == 0) {
            BufferedReader outReader=new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String output = "";
            String line = null;
            while ((line = outReader.readLine()) != null) {
                output += line + "\n";
            }
            System.out.println(output);
        } else {
            System.out.println("Error in running 'cat /proc/self/status'");

        }
        return "";
    }

    /**
     * get vm stats from /proc
     */
    public static String runBjobs(String runId) throws IOException,InterruptedException {

        Vector<String> commands=new Vector<String>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("bjobs -l -J " + runId + " | grep SWAP");
        ProcessBuilder pb=new ProcessBuilder(commands);
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
            System.out.println("bjobs -l -J " + runId + " | grep SWAP = " + output);
        } else {
            System.out.println("Error in running " + commands.toString());

        }
        return "";
    }
}
