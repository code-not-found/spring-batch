package com.codenotfound.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import com.codenotfound.model.Person;

@Configuration
@EnableBatchProcessing
public class ConvertNamesJobConfig {

  @Autowired
  public JobBuilderFactory jobBuilders;

  @Autowired
  public StepBuilderFactory stepBuilders;

  @Bean
  public Job convertNamesJob() {
    return jobBuilders.get("convertNamesJob").start(convertNamesStep()).build();
  }

  @Bean
  public Step convertNamesStep() {
    return stepBuilders.get("convertNamesStep").<Person, Person>chunk(10).reader(itemReader())
        .processor(itemProcessor()).writer(itemWriter()).build();
  }

  @Bean
  public FlatFileItemReader<Person> itemReader() {
    BeanWrapperFieldSetMapper<Person> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
    fieldSetMapper.setTargetType(Person.class);

    DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
    lineTokenizer.setDelimiter(",");
    lineTokenizer.setNames(new String[] {"firstName", "lastName"});

    DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
    lineMapper.setFieldSetMapper(fieldSetMapper);
    lineMapper.setLineTokenizer(lineTokenizer);

    FlatFileItemReader<Person> flatFileItemReader = new FlatFileItemReader<Person>();
    flatFileItemReader.setName("personItemReader");
    flatFileItemReader.setResource(new ClassPathResource("csv/persons.csv"));
    flatFileItemReader.setLineMapper(lineMapper);
    flatFileItemReader.setLinesToSkip(1);

    return flatFileItemReader;
  }

  @Bean
  public PersonNameProcessor itemProcessor() {
    return new PersonNameProcessor();
  }

  @Bean
  public FlatFileItemWriter<Person> itemWriter() {
    return new FlatFileItemWriterBuilder<Person>().name("personItemWriter")
        .resource(new FileSystemResource("target/test-outputs/persons.txt"))
        .lineAggregator(new PassThroughLineAggregator<>()).build();
  }
}
