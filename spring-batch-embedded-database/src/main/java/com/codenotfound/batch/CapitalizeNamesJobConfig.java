package com.codenotfound.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import com.codenotfound.model.Person;

@Configuration
public class CapitalizeNamesJobConfig {

  @Bean
  public Job convertNamesJob(JobBuilderFactory jobBuilders,
      StepBuilderFactory stepBuilders) {
    return jobBuilders.get("capitalizeNamesJob")
        .start(convertNamesStep(stepBuilders)).build();
  }

  @Bean
  public Step convertNamesStep(StepBuilderFactory stepBuilders) {
    return stepBuilders.get("capitalizeNamesStep")
        .<Person, Person>chunk(10).reader(itemReader())
        .processor(itemProcessor()).writer(itemWriter()).build();
  }

  @Bean
  public FlatFileItemReader<Person> itemReader() {
    return new FlatFileItemReaderBuilder<Person>()
        .name("personItemReader")
        .resource(new ClassPathResource("csv/persons.csv"))
        .delimited().names(new String[] {"firstName", "lastName"})
        .targetType(Person.class).build();
  }

  @Bean
  public PersonItemProcessor itemProcessor() {
    return new PersonItemProcessor();
  }

  @Bean
  public FlatFileItemWriter<Person> itemWriter() {
    return new FlatFileItemWriterBuilder<Person>()
        .name("personItemWriter")
        .resource(new FileSystemResource(
            "target/test-outputs/persons.txt"))
        .delimited().delimiter(", ")
        .names(new String[] {"firstName", "lastName"}).build();
  }
}
