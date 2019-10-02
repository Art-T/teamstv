package com.teamstv.telegrambot.handlers.messages;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

public class CallbackResponse extends Response<String> {

  private final EditMessageText editMessageText;

  CallbackResponse(String msg, Update update) {
    super(msg, update);
    editMessageText = new EditMessageText();
  }

  @Override
  int getMessageId() {
    return update.getCallbackQuery().getMessage().getMessageId();
  }

  @Override
  long getChatId() {
    return update.getCallbackQuery().getMessage().getChatId();
  }

  @Override
  Response setChatId(long id) {
    editMessageText.setChatId(id);
    return this;
  }

  @Override
  Response setMessageId(int id) {
    editMessageText.setMessageId(id);
    return this;
  }

  @Override
  BotApiMethod setText(String msg) {
    editMessageText.setText(msg);
    return editMessageText;
  }
}
