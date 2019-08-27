package com.teamstv.telegrambot.handlers.messages;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TextResponse extends Response<String> {

  private final SendMessage sendMessage;

  public TextResponse(String msg, Update update) {
    super(msg, update);
    sendMessage = new SendMessage();
  }

  @Override
  int getMessageId() {
    return update.getMessage().getMessageId();
  }

  @Override
  long getChatId() {
    return update.getMessage().getChatId();
  }

  @Override
  Response setChatId(long id) {
    sendMessage.setChatId(id);
    return this;
  }

  @Override
  Response setMessageId(int id) {
    sendMessage.setReplyToMessageId(id);
    return this;
  }

  @Override
  BotApiMethod setText(String msg) {
    sendMessage.setText(msg);
    return sendMessage;
  }
}
