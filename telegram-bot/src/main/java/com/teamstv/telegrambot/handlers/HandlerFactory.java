package com.teamstv.telegrambot.handlers;

import com.teamstv.telegrambot.model.ButtonNameEnum;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Handler factory class returns Handler interface instance based on type of update Private methods
 * isText, isPhoto, etc used for simplify code
 *
 * @author talipa
 */

@Service
public class HandlerFactory {

  private final PhotoMessageHandler photoMessageHandler;
  private final TextMessageHandler textMessageHandler;
  private final DownloadCallbackHandler downloadCallbackHandler;
  private final CancelCallbackHandler cancelCallbackHandler;
  private final CaptionCallbackHandler captionCallbackHandler;
  private final ListCommandHandler listCommandHandler;


  public HandlerFactory(
      PhotoMessageHandler photoMessageHandler,
      TextMessageHandler textMessageHandler,
      DownloadCallbackHandler downloadCallbackHandler,
      CancelCallbackHandler cancelCallbackHandler,
      CaptionCallbackHandler captionCallbackHandler,
      ListCommandHandler listCommandHandler) {
    this.photoMessageHandler = photoMessageHandler;
    this.textMessageHandler = textMessageHandler;
    this.downloadCallbackHandler = downloadCallbackHandler;
    this.cancelCallbackHandler = cancelCallbackHandler;
    this.captionCallbackHandler = captionCallbackHandler;
    this.listCommandHandler = listCommandHandler;
  }

  public Optional<Handler> getHandler(Update update) {
    if (isPhoto(update)) {
      return Optional.of(photoMessageHandler);
    }
    if (isTextAndNotCommand(update)) {
      return Optional.of(textMessageHandler);
    }
    if (checkCallbackType(update, ButtonNameEnum.DOWNLOAD)) {
      return Optional.of(downloadCallbackHandler);
    }
    if (checkCallbackType(update, ButtonNameEnum.CANCEL)) {
      return Optional.of(cancelCallbackHandler);
    }
    if (checkCallbackType(update, ButtonNameEnum.ADD_CAPTION)) {
      return Optional.of(captionCallbackHandler);
    }
    if (isCommand(update)) {
      return Optional.of(listCommandHandler);
    }
    return Optional.empty();
  }

  private boolean isPhoto(Update update) {
    return update.hasMessage() && update.getMessage().hasPhoto();
  }

  private boolean isText(Update update) {
    return update.hasMessage() && update.getMessage().hasText();
  }

  private boolean isTextAndNotCommand(Update update) {
    return isText(update) && !isCommand(update);
  }

  private boolean isCommand(Update update) {
    return isText(update) && update.getMessage().getText().startsWith("/");
  }

  private boolean checkCallbackType(Update update, ButtonNameEnum nameEnum) {
    return update.hasCallbackQuery() && update.getCallbackQuery().getData()
        .endsWith(nameEnum.getData());
  }
}
