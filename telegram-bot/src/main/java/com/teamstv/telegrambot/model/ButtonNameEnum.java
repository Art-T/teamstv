package com.teamstv.telegrambot.model;

/**
 * Enumeration represents available callback options
 *
 * @author talipa
 */
public enum ButtonNameEnum {

  DOWNLOAD("_download"),
  ADD_CAPTION("_caption"),
  CANCEL("_cancel");

  private final String data;

  ButtonNameEnum(String data) {
    this.data = data;
  }

  public String getData() {
    return data;
  }
}
