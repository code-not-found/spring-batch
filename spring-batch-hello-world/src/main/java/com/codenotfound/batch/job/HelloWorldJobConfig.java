package com.codenotfound.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import com.codenotfound.model.Person;

@Configuration
@EnableBatchProcessing
public class HelloWorldJobConfig {

  @Autowired
  public JobBuilderFactory jobBuilders;

  @Autowired
  public StepBuilderFactory stepBuilders;

  @Bean
  public Job helloWorlJob() {
    return jobBuilders.get("helloWorldJob").start(helloWorldStep()).build();
  }

  @Bean
  public Step helloWorldStep() {
    return stepBuilders.get("helloWorldStep").<Person, String>chunk(10).reader(reader())
        .processor(processor()).writer(writer()).build();
  }

  @Bean
  public FlatFileItemReader<Person> reader() {
    return new FlatFileItemReaderBuilder<Person>().name("personItemReader")
        .resource(new ClassPathResource("persons.csv")).delimited()
        .names(new String[] {"firstName", "lastName"}).targetType(Person.class).build();
  }

  @Bean
  public PersonItemProcessor processor() {
    return new PersonItemProcessor();
  }

  @Bean
  public FlatFileItemWriter<String> writer() {
    return new FlatFileItemWriterBuilder<String>().name("greetingItemWriter")
        .resource(new FileSystemResource("target/test-outputs/greetings.txt"))
        .lineAggregator(new PassThroughLineAggregator<>()).build();
  }
}
