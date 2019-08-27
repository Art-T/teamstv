package com.teamstv.telegrambot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Class which maps external props to POJO
 *
 * @author talipa
 */

@Component
@ConfigurationProperties(prefix = "bot.properties")
public class BotProperties {

  private String token;
  private String botName;
  private String path;
  private String downloadButton;
  private String captionButton;
  private String cancelButton;
  private String deleteButton;
  private int transferServiceCapacity;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getBotName() {
    return botName;
  }

  public void setBotName(String botName) {
    this.botName = botName;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getDownloadButton() {
    return downloadButton;
  }

  public void setDownloadButton(String downloadButton) {
    this.downloadButton = downloadButton;
  }

  public String getCaptionButton() {
    return captionButton;
  }

  public void setCaptionButton(String captionButton) {
    this.captionButton = captionButton;
  }

  public String getCancelButton() {
    return cancelButton;
  }

  public void setCancelButton(String cancelButton) {
    this.cancelButton = cancelButton;
  }

  public String getDeleteButton() {
    return deleteButton;
  }

  public void setDeleteButton(String deleteButton) {
    this.deleteButton = deleteButton;
  }

  public int getTransferServiceCapacity() {
    return transferServiceCapacity;
  }

  public void setTransferServiceCapacity(int transferServiceCapacity) {
    this.transferServiceCapacity = transferServiceCapacity;
  }
}
