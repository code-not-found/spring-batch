package com.codenotfound.batch.job;

import java.io.File;
import java.io.IOException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class FileDeletingTasklet implements Tasklet {

  private Resource directory;

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws IOException {

    File dir = directory.getFile();
    Assert.state(dir.isDirectory(), "Not a directory");

    for (File file : dir.listFiles()) {
      boolean deleted = file.delete();
      if (!deleted) {
        throw new UnexpectedJobExecutionException("Could not delete file: " + file.getPath());
      }
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
