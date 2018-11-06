package com.codenotfound.batch.job;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;

public class FileDeletingTasklet implements Tasklet {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileDeletingTasklet.class);

  private Resource directory;

  public FileDeletingTasklet(Resource directory) {
    this.directory = directory;
  }

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) {
    try (Stream<Path> walk = Files.walk(Paths.get(directory.getFile().getPath()))) {
      walk.filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);
    } catch (IOException e) {
      LOGGER.error("error deleting files", e);
      throw new UnexpectedJobExecutionException("unable to delete files");
    }

    return RepeatStatus.FINISHED;
  }
}
