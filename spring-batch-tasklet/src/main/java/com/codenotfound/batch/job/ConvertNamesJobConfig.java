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
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
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
public class ConvertNamesJobConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConvertNamesJobConfig.class);

  @Autowired
  public JobBuilderFactory jobBuilders;

  @Autowired
  public StepBuilderFactory stepBuilders;

  @Bean
  public Job convertNamesJob() {
    return jobBuilders.get("convertNamesJob").start(convertNamesStep()).next(deleteFilesStep())
        .build();
  }

  @Bean
  public Step convertNamesStep() {
    return stepBuilders.get("convertNamesStep").<Person, Person>chunk(10).reader(reader())
        .processor(processor()).writer(writer()).build();
  }

  @Bean
  public Step deleteFilesStep() {
    return stepBuilders.get("deleteFilesStep").tasklet(fileDeletingTasklet()).build();
  }

  @Bean
  public FlatFileItemReader<Person> flatFileItemReader() {
    return new FlatFileItemReaderBuilder<Person>().name("flatFileItemReader").delimited()
        .names(new String[] {"firstName", "lastName"}).targetType(Person.class).build();
  }

  @Bean
  public MultiResourceItemReader<Person> reader() {
    ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = null;
    try {
      resources = patternResolver.getResources("file:target/test-input/*.csv");
    } catch (IOException e) {
      LOGGER.error("error reading files", e);
    }

    return new MultiResourceItemReaderBuilder<Person>().name("personItemReader")
        .delegate(flatFileItemReader()).resources(resources).setStrict(true).build();
  }

  @Bean
  public PersonItemProcessor processor() {
    return new PersonItemProcessor();
  }

  @Bean
  public FlatFileItemWriter<Person> writer() {
    BeanWrapperFieldExtractor<Person> fieldExtractor = new BeanWrapperFieldExtractor<>();
    fieldExtractor.setNames(new String[] {"firstName", "lastName"});
    fieldExtractor.afterPropertiesSet();

    DelimitedLineAggregator<Person> lineAggregator = new DelimitedLineAggregator<>();
    lineAggregator.setDelimiter(", ");
    lineAggregator.setFieldExtractor(fieldExtractor);

    return new FlatFileItemWriterBuilder<Person>().name("personItemWriter")
        .resource(new FileSystemResource("target/test-output/persons.txt"))
        .lineAggregator(lineAggregator).build();
  }

  @Bean
  public FileDeletingTasklet fileDeletingTasklet() {
    FileDeletingTasklet tasklet = new FileDeletingTasklet();
    tasklet.setDirectoryResource(new FileSystemResource("target/test-input"));

    return tasklet;
  }
}
