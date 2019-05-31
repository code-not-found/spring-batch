package com.codenotfound.batch;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class CapitalizeNamesJobScheduler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CapitalizeNamesJobScheduler.class);

  @Autowired
  private JobLauncher jobLauncher;

  @Autowired
  private Job capitalizeNamesJob;

  private boolean enabled = false;

  @Scheduled(cron = "0/10 * * * * ?")
  public void runBatchJob()
      throws JobExecutionAlreadyRunningException,
      JobRestartException, JobInstanceAlreadyCompleteException,
      JobParametersInvalidException {
    LOGGER.info("start runBatchJob");

    if (enabled) {
      jobLauncher.run(capitalizeNamesJob, new JobParametersBuilder()
          .addDate("date", new Date()).toJobParameters());
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
