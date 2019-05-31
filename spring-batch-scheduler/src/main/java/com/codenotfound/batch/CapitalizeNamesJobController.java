package com.codenotfound.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/toggle-batch-job")
public class CapitalizeNamesJobController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CapitalizeNamesJobScheduler.class);

  @Autowired
  CapitalizeNamesJobScheduler capitalizeNamesJobScheduler;

  @GetMapping
  public String toggleBatchJob() {
    boolean toggleEnabled =
        !capitalizeNamesJobScheduler.isEnabled();
    capitalizeNamesJobScheduler.setEnabled(toggleEnabled);

    String result = "isEnabled=" + toggleEnabled;
    LOGGER.info(result);

    return result;
  }
}
