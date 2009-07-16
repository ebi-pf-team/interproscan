package com.activeeon.sandbox.spring;

import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * TODO: Add description
 *
 * @author  Emil Salageanu
 * @version $Id$
 * @since   2.0
 */
public class StepExecutable extends JavaExecutable {

	public static final String JOB_EXECUTION_ID  = "jobExecutionId";
	public static final String STEP_EXECUTION_ID = "stepExecutionId";
	public static final String STEP_NAME         = "stepName";
	public static final String JOB_CONFIG_FILE   = "JobConfigFile";

	private Long jobExecutionId;
	private Long stepExecutionId;
	private String stepName;
	private String configFile;

    @Override
    public void init(Map<String, String> args) throws Exception {
     	jobExecutionId = Long.parseLong( args.get(JOB_EXECUTION_ID));
    	stepExecutionId = Long.parseLong(args.get(STEP_EXECUTION_ID));
    	stepName = args.get(STEP_NAME);
    	configFile=args.get(JOB_CONFIG_FILE);
    }

	@Override
	public Serializable execute(TaskResult... arg0) throws Throwable {
		System.out.println("TestStep.execute() ->  starting execution for step "+stepName + " and stepExecutionID "+STEP_EXECUTION_ID);
		printClassPath();

		System.out.println("StepExecutable ClassLoader:"+this.getClass().getClassLoader());
		System.out.println("System ClassLoader:"+ClassLoader.getSystemClassLoader());

		ApplicationContext context = new ClassPathXmlApplicationContext(configFile,this.getClass());

		System.out.println("ApplicationContext ClassLoader:"+context.getClass().getClassLoader());

		BeanFactory factory = context;
		Step step = (Step) factory.getBean(stepName);

        // TODO: Pass StepExecution to remote node without relying on access to job repository [http://mail.ow2.org/wws/arc/proactive/2009-05/msg00050.html]
        JobExplorer je = (JobExplorer)factory.getBean("jobExplorer");
		StepExecution stepExecution = (StepExecution) je.getStepExecution(jobExecutionId, stepExecutionId);

		try {
			step.execute(stepExecution);
		} catch (JobInterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			stepExecution.addFailureException(e);
		}
		return stepExecution;
	}

	private void printClassPath()
	{
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

	//	ClassLoader classLoader = StepExecutable.class.getClassLoader();
        System.out.println("Class Loader: "+classLoader);
        //Get the URLs

        URL[] urls = ((URLClassLoader)classLoader).getURLs();

        for(int i=0; i< urls.length; i++)
        {
            System.out.println(urls[i].getFile());
        }
	}

}