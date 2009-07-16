/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.activeeon.sandbox.spring;

import java.io.File;
import java.util.*;

import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.StepExecutionSplitter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;


/**
 * TODO: Add description
 *
 * @author  Emil Salageanu
 * @version $Id$
 * @since   2.0
 */
public class ProActiveSchedulerPartitionHandler implements PartitionHandler, InitializingBean {

	private int gridSize = 1;

	private String proactiveSchedulerUrl;
	private String proactiveSchedulerUserName;
	private String proactiveSchedulerPassword;

    private boolean addClassPath = true;

    private Step step;

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(step, "A Step must be provided.");
	}

    public void setAddClassPath(boolean addClassPath) {
        this.addClassPath = addClassPath;
    }        

    /**
	 * Passed to the {@link StepExecutionSplitter} in the
	 * {@link #handle(StepExecutionSplitter, StepExecution)} method, instructing
	 * it how many {@link StepExecution} instances are required, ideally. The
	 * {@link StepExecutionSplitter} is allowed to ignore the grid size in the case of
	 * a restart, since the input data partitions must be preserved.
	 *
	 * @param gridSize the number of step executions that will be created
	 */
	public void setGridSize(int gridSize) {
		this.gridSize = gridSize;
	}


	/**
	 * Setter for the {@link Step} that will be used to execute the partitioned
	 * {@link StepExecution}. This is a regular Spring Batch step, with all the
	 * business logic required to complete an execution based on the input
	 * parameters in its {@link StepExecution} context.
	 *
	 * @param step the {@link Step} instance to use to execute business logic
	 */
	public void setStep(Step step) {
		this.step = step;
	}


	/**
	 * @see PartitionHandler#handle(StepExecutionSplitter, StepExecution)
	 */
	public Collection<StepExecution> handle(StepExecutionSplitter stepExecutionSplitter,
			StepExecution masterStepExecution) throws Exception {


		Collection<StepExecution> result = new ArrayList<StepExecution>();

		//A job to be scheduled on the ProactiveScheduler
		TaskFlowJob proactiveSchedulerJob = new TaskFlowJob();
		proactiveSchedulerJob.setName("Master Step: " + masterStepExecution.toString());

		//the step executions
		Set<StepExecution> stepExecutions =  stepExecutionSplitter.split(masterStepExecution, gridSize);

		for (StepExecution stepExecution : stepExecutions) {

            String jobExecutionId=	stepExecution.getJobExecution().getId().toString();
			String stepExecutionId=stepExecution.getId().toString();
			String stepName=step.getName();

            // TODO: Pass StepExecution to remote node without relying on access to job repository
            // TODO: Documentation wrong (Version 1.0 2009-04-27) -- says addArgument() can take Object as second arg, but in reality can only take String  [http://mail.ow2.org/wws/arc/proactive/2009-05/msg00050.html]
            // JavaTask aTask = new JavaTask();
            // aTask.addArgument("foo",new Boolean(true));
            // aTask.addArgument("bar",new Integer(12));
            // http://proactive.inria.fr/release-doc/scheduler/single_html/SchedulerManual.html#Create_java_task
            JavaTask jt = new JavaTask();
			jt.setName(stepExecutionId);
			jt.setExecutableClassName(StepExecutable.class.getName());
			jt.addArgument(StepExecutable.JOB_EXECUTION_ID, jobExecutionId);
			jt.addArgument(StepExecutable.STEP_EXECUTION_ID, stepExecutionId);
			jt.addArgument(StepExecutable.STEP_NAME, stepName);
			jt.addArgument(StepExecutable.JOB_CONFIG_FILE, "/launch-context.xml");
            proactiveSchedulerJob.addTask(jt);

		}//for all  StepExecution

        // AQ
        if (addClassPath)   {
            this.setJobClasPath(proactiveSchedulerJob);
        }

        SchedulerAuthenticationInterface auth = SchedulerConnection.join(proactiveSchedulerUrl);
		UserSchedulerInterface uischeduler =  auth.logAsUser(proactiveSchedulerUserName, proactiveSchedulerPassword);
		JobId id = uischeduler.submit(proactiveSchedulerJob);

		//blocking loop until we have the result:

		JobResult jr = null;
		while (jr==null)
		{
			Thread.sleep(3000);
			jr = uischeduler.getJobResult(id);

		}

		Map<String, TaskResult> jobResults = jr.getAllResults();
		//the jobresults map contains entries of type: <task_name, TaskResult>
		// and the task_name is the same as the corresponding step execution id
		//Therefore we iterate over the  stepExecutions list and get the result for each stepExecution
		//This will allow us to manage exceptions on the remote tasks

		for (StepExecution stepExecution : stepExecutions) {
			TaskResult taskResult = jobResults.get(stepExecution.getId().toString());
			try{
				StepExecution se = (StepExecution)taskResult.value();
				result.add(se);
			}
			catch (Throwable t)
			{
				//an exception has been thrown during the launch of the task on the remote node
				//we will mark the local step execution as FAILED, attach the exception to it and add it to the results

				ExitStatus exitStatus = ExitStatus.FAILED
						.addExitDescription("TaskExecutor rejected the task for this step.");
				stepExecution.setStatus(BatchStatus.FAILED);
				stepExecution.setExitStatus(exitStatus);
				stepExecution.addFailureException(t);
				result.add(stepExecution);
			}
		}
        // TODO: Persist StepExecutions in local repository (assume using RemoteJobRepository)
        return result;
	}

	public void setProactiveSchedulerUrl(String proactiveSchedulerUrl) {
		this.proactiveSchedulerUrl = proactiveSchedulerUrl;
	}

	public void setProactiveSchedulerUserName(String proactiveSchedulerUserName) {
		this.proactiveSchedulerUserName = proactiveSchedulerUserName;
	}

	public void setProactiveSchedulerPassword(String proactiveSchedulerPassword) {
		this.proactiveSchedulerPassword = proactiveSchedulerPassword;
	}

	protected void setJobClasPath(org.ow2.proactive.scheduler.common.job.Job job) {

        // TODO: Find better of building class path (wait for Emil email)
        String appClassPath = "";
		try {
			File appMainFolder = new File(this.getClass().getProtectionDomain()
					.getCodeSource().getLocation().toURI());
			appClassPath = appMainFolder.getAbsolutePath();
        } catch (java.net.URISyntaxException e1) {
			e1.printStackTrace();
		}

        JobEnvironment je = new JobEnvironment();
		try {
			je.setJobClasspath(new String[] { appClassPath });
			System.out.println("job classpath: "+appClassPath);
        } catch (java.io.IOException e) {
			e.printStackTrace();
		}
		job.setEnvironment(je);
	}


}
