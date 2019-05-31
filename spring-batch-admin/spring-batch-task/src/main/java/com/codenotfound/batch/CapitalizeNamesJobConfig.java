package com.codenotfound.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import com.codenotfound.model.Person;

@Configuration
@EnableBatchProcessing
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
    BeanWrapperFieldSetMapper<Person> fieldSetMapper =
        new BeanWrapperFieldSetMapper<>();
    fieldSetMapper.setTargetType(Person.class);

    DelimitedLineTokenizer lineTokenizer =
        new DelimitedLineTokenizer();
    lineTokenizer.setDelimiter(",");
    lineTokenizer.setNames(new String[] {"firstName", "lastName"});

    DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
    lineMapper.setFieldSetMapper(fieldSetMapper);
    lineMapper.setLineTokenizer(lineTokenizer);

    FlatFileItemReader<Person> flatFileItemReader =
        new FlatFileItemReader<>();
    flatFileItemReader.setName("personItemReader");
    flatFileItemReader
        .setResource(new ClassPathResource("csv/persons.csv"));
    flatFileItemReader.setLineMapper(lineMapper);

    return flatFileItemReader;
  }

  @Bean
  public PersonItemProcessor itemProcessor() {
    return new PersonItemProcessor();
  }

  @Bean
  public FlatFileItemWriter<Person> itemWriter() {
    BeanWrapperFieldExtractor<Person> fieldExtractor =
        new BeanWrapperFieldExtractor<>();
    fieldExtractor.setNames(new String[] {"firstName", "lastName"});
    fieldExtractor.afterPropertiesSet();

    DelimitedLineAggregator<Person> lineAggregator =
        new DelimitedLineAggregator<>();
    lineAggregator.setDelimiter(",");
    lineAggregator.setFieldExtractor(fieldExtractor);

    FlatFileItemWriter<Person> flatFileItemWriter =
        new FlatFileItemWriter<>();
    flatFileItemWriter.setName("personItemWriter");
    flatFileItemWriter.setResource(
        new FileSystemResource("target/test-outputs/persons.txt"));
    flatFileItemWriter.setLineAggregator(lineAggregator);

    return flatFileItemWriter;
  }
}
