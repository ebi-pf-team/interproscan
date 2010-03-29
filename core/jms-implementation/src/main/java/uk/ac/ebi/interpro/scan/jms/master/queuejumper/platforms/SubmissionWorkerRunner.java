package uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;


/**
 * Creates a new worker by executing a command.
 */

public class SubmissionWorkerRunner implements WorkerRunner {




    private String submissionCommand;

    @Required
    public void setSubmissionCommand(String submissionCommand) {
        this.submissionCommand = submissionCommand;
    }

    @Override
    public void startupNewWorker() {
        try{
            String command=submissionCommand;
            command=command.replaceAll("%config%",System.getProperty("config"));
            
            System.out.println("Submitted Command: " + command);
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot run the worker", e);
        }
    }


//TODO: work out how to configure submission scripts.

    //    private String optionalPrefix;
//
//    private Resource submissionJobFile;
//
//    //Optional parameter so user can set shell to one of their own choosing.
//    //Default for both SGE and LSF is Bourne shell (/bin/sh).
//    //Default for PBS is to use the user's login shell on the execution host.
//    private String shellPath;
//
//    //This is a required parameter and currently can have the values 'sge', 'lsf' or 'pbs'.
//    private String submissionType;
//
//    private Map<String,String> submissionCommand = popSubmissionKey.newMap();
//
//    public void setOptionalPrefix(String optionalPrefix) {
//        this.optionalPrefix = optionalPrefix;
//    }
//
//    @Required
//    public void setSubmissionJobFile(Resource submissionJobFile) {
//        this.submissionJobFile = submissionJobFile;
//    }
//
//    public void setShellPath(String shellPath) {
//        this.shellPath = shellPath;
//    }
//
//    @Required
//    public void setSubmissionType(String submissionType) {
//        this.submissionType = submissionType;
//    }
//
//    @Override
//    public void startupNewWorker() {
//
//        StringBuffer commandBuf = new StringBuffer();
//        if (optionalPrefix != null){
//            commandBuf.append(optionalPrefix).append(' ');
//        }
//
//        commandBuf.append(submissionCommand.get(submissionType)).append(' ');
//
//        if (shellPath != null && shellPath.length() > 0) {
//            commandBuf.append(submissionCommand.get(submissionType+"_sh")).append(' ').append(shellPath).append(' ');
//        }
//
//        if (submissionCommand.containsKey(submissionType+"_exec")) {
//            commandBuf.append(submissionCommand.get(submissionType+"_exec")).append(' ');
//        }
//
//        try{
//            commandBuf.append(submissionJobFile.getFile().getAbsolutePath());
//
//            System.out.println("Submitted Command:\t" + commandBuf.toString());
//
//            Runtime.getRuntime().exec(commandBuf.toString());
//        } catch (IOException e) {
//            throw new IllegalStateException("Cannot run the worker", e);
//        }
//    }
//
//    static class popSubmissionKey{
//        private popSubmissionKey(){
//        }
//        private static Map<String, String> newMap(){
//            Map<String, String> m = new java.util.HashMap<String,String>();
//            m.put("sge", "qsub");
//            m.put("sge_sh", "-S");
//            m.put("pbs", "qsub");
//            m.put("pbs_sh", "-S");
//            m.put("lsf", "bsub");
//            m.put("lsf_sh", "-L");
//            m.put("lsf_exec", "<");
//            return Collections.unmodifiableMap(m);
//        }
//    }
}