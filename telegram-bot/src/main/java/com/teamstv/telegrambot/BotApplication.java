package com.teamstv.telegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

/**
 * SpringBoot main class.
 *
 * @author talipa
 */

@SpringBootApplication
public class BotApplication {

  public static void main(String[] args) {
    ApiContextInitializer.init();
    SpringApplication.run(BotApplication.class, args);
  }

}
