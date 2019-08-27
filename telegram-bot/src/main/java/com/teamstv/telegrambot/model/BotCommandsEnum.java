package com.teamstv.telegrambot.model;

/**
 * Class contains list of available bot commands
 *
 * @author talipa
 */

public enum BotCommandsEnum {

  LIST("/list");

  private final String name;

  BotCommandsEnum(String name) {
    this.name = name;
  }


  public String getName() {
    return name;
  }
}
