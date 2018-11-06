package com.codenotfound.batch.job;

import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class FileDeletingTasklet implements Tasklet {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileDeletingTasklet.class);

  private Resource directory;

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
    try {
      FileUtils.cleanDirectory(directory.getFile());
    } catch (IOException e) {
      LOGGER.error("error deleting files", e);
      throw new UnexpectedJobExecutionException("unable to delete files");
    }

    return RepeatStatus.FINISHED;
  }

  public void setDirectory(Resource directory) {
    this.directory = directory;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(directory, "Directory must be set");
  }
}
