package com.codenotfound.batch.job;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import com.codenotfound.model.Person;

@Configuration
@EnableBatchProcessing
public class CapitalizeNamesJobConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(CapitalizeNamesJobConfig.class);

  @Autowired
  public JobBuilderFactory jobBuilders;

  @Autowired
  public StepBuilderFactory stepBuilders;

  @Bean
  public Job capitalizeNamesJob() {
    return jobBuilders.get("capitalizeNamesJob").start(capitalizeNamesStep())
        .next(deleteFilesStep()).build();
  }

  @Bean
  public Step capitalizeNamesStep() {
    return stepBuilders.get("capitalizeNamesStep").<Person, Person>chunk(10)
        .reader(multiItemReader()).processor(itemProcessor()).writer(itemWriter()).build();
  }

  @Bean
  public Step deleteFilesStep() {
    return stepBuilders.get("deleteFilesStep").tasklet(fileDeletingTasklet()).build();
  }

  @Bean
  public MultiResourceItemReader<Person> multiItemReader() {
    ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = null;
    try {
      resources = patternResolver.getResources("file:target/test-inputs/*.csv");
    } catch (IOException e) {
      LOGGER.error("error reading files", e);
    }

    return new MultiResourceItemReaderBuilder<Person>().name("multiPersonItemReader")
        .delegate(itemReader()).resources(resources).setStrict(true).build();
  }

  @Bean
  public FlatFileItemReader<Person> itemReader() {
    return new FlatFileItemReaderBuilder<Person>().name("personItemReader").delimited()
        .names(new String[] {"firstName", "lastName"}).targetType(Person.class).build();
  }

  @Bean
  public PersonItemProcessor itemProcessor() {
    return new PersonItemProcessor();
  }

  @Bean
  public FlatFileItemWriter<Person> itemWriter() {
    return new FlatFileItemWriterBuilder<Person>().name("personItemWriter")
        .resource(new FileSystemResource("target/test-outputs/persons.txt")).delimited()
        .delimiter(", ").names(new String[] {"firstName", "lastName"}).build();
  }

  @Bean
  public FileDeletingTasklet fileDeletingTasklet() {
    FileDeletingTasklet tasklet = new FileDeletingTasklet();
    tasklet.setDirectory(new FileSystemResource("target/test-inputs"));

    return tasklet;
  }
}
