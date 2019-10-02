package com.teamstv.telegrambot.handlers.messages;

import static com.teamstv.telegrambot.handlers.messages.ResponseTypes.CALLBACK;
import static com.teamstv.telegrambot.handlers.messages.ResponseTypes.TEXT;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class ResponseFactory {

  public Response<String> getResponse(String msg, Update update, ResponseTypes type) {
    if(type == TEXT) {
      return new TextResponse(msg, update);
    } else if(type == CALLBACK) {
      return new CallbackResponse(msg, update);
    } else {
      throw new IllegalArgumentException("Not supported response type");
    }
  }

}
