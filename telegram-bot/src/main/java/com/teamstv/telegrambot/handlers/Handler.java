package com.teamstv.telegrambot.handlers;

import static com.teamstv.telegrambot.BotReplies.NULL_USER;

import com.teamstv.telegrambot.handlers.messages.Response;
import com.teamstv.telegrambot.handlers.messages.ResponseFactory;
import com.teamstv.telegrambot.handlers.messages.ResponseTypes;
import com.teamstv.telegrambot.model.Photo;
import com.teamstv.telegrambot.services.TransferService;
import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Handler interface with abstract method onUpdateReceived. Used to handle different types of
 * messages Default sendText method could be commonly used in implementations
 *
 * @author talipa
 */
public abstract class Handler {

  final TransferService<Integer, Photo> transferService;
  Update update;
  DefaultAbsSender sender;
  Logger log = LoggerFactory.getLogger(Handler.class);
  final ResponseFactory factory;

  protected Handler(
      TransferService<Integer, Photo> transferService,
      ResponseFactory factory) {
    this.transferService = transferService;
    this.factory = factory;
  }

  public void setUpdate(Update update) {
    this.update = update;
  }

  public void setSender(DefaultAbsSender sender) {
    this.sender = sender;
  }

  public final void onUpdateReceived() {
    if (getUser() == null) {
      nullUserAction();
      return;
    }
    Iterable<Optional<? extends BotApiObject>> listOptionalObject;
    if (this instanceof ListCommandHandler) {
      listOptionalObject = getListContent();
    } else {
      listOptionalObject = Collections.singleton(getContent());
    }
    template(listOptionalObject);
  }

  final String getUser() {
    if (update.hasMessage()) {
      return update.getMessage().getFrom().getUserName();
    }
    return update.getCallbackQuery().getFrom().getUserName();
  }

  void createKeyboard(Photo model, PartialBotApiMethod msg) {

  }

  abstract Optional<Photo> operateOnContent(BotApiObject content);

  abstract Optional<? extends BotApiObject> getContent();

  abstract Response getResponse();

  private void nullUserAction() {
    Response msg = factory.getResponse(NULL_USER.getResponse(), update, ResponseTypes.TEXT);
    sendText(createResponse(msg));
  }

  private Iterable<Optional<? extends BotApiObject>> getListContent() {
    return transferService.getAll()
        .stream()
        .map(Photo::getPhotoSize)
        .map(Optional::ofNullable)
        .collect(Collectors.toList());
  }

  private void template(Iterable<Optional<? extends BotApiObject>> listOptionalObject) {
    listOptionalObject.forEach(
        optionalObject -> optionalObject.ifPresent(
            c -> {
              Optional<Photo> optionalPhoto = operateOnContent(c);
              Response reply = getResponse();
              BotApiMethod msg = createResponse(reply);
              optionalPhoto.ifPresent(
                  p -> createKeyboard(p, msg)
              );
              sendText(msg);
            }
        )
    );
  }

  private void sendText(BotApiMethod msg) {
    try {
      sender.execute((BotApiMethod<? extends Serializable>) msg);
      log.info("Text message to user {} sent.", getUser());
    } catch (TelegramApiException e) {
      log.error("Error while sending message to user = " + getUser(), e);
    }
  }

  private BotApiMethod createResponse(Response reply) {
    return reply.getResponse();
  }
}
