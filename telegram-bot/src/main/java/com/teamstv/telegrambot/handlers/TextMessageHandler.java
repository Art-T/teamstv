package com.teamstv.telegrambot.handlers;

import static com.teamstv.telegrambot.BotReplies.TEXT_NOT_SUPPORTED;
import static com.teamstv.telegrambot.BotReplies.THANKS_FOR_CAPTION;

import com.teamstv.telegrambot.BotProperties;
import com.teamstv.telegrambot.handlers.messages.Response;
import com.teamstv.telegrambot.handlers.messages.ResponseFactory;
import com.teamstv.telegrambot.handlers.messages.ResponseTypes;
import com.teamstv.telegrambot.model.Keyboard;
import com.teamstv.telegrambot.model.Photo;
import com.teamstv.telegrambot.services.TransferService;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Class for handling text messages(not commands)
 *
 * @author talipa
 */

@Service
public class TextMessageHandler extends Handler {


  private final Keyboard keyboard;
  private final BotProperties properties;

  public TextMessageHandler(
      TransferService<Integer, Photo> transferService,
      ResponseFactory factory,
      Keyboard keyboard, BotProperties properties) {
    super(transferService, factory);
    this.keyboard = keyboard;
    this.properties = properties;
  }

  @Override
  Optional<? extends BotApiObject> getContent() {
    return Optional.ofNullable(update.getMessage());
  }

  @Override
  Optional<Photo> operateOnContent(BotApiObject content) {
    Optional<Photo> optModel = transferService.get(getUser());
    optModel.ifPresent(
        model -> {
          String caption = ((Message) content).getText();
          log.info("Caption: {}. For photo: {} provided.", caption, model.getFileId());
          model.setCaption(caption);
          Path captionPath = Paths.get(properties.getPath(), model.getFileId() + ".txt");
          model.setCaptionLocalPath(captionPath.toString());
          saveCaption(captionPath, caption);
        }
    );
    return optModel;
  }

  @Override
  void createKeyboard(Photo model, PartialBotApiMethod msg) {
    keyboard.keyboard(model, model.getTransferId())
        .ifPresent(((SendMessage) msg)::setReplyMarkup);
    transferService.delete(getUser());
  }

  @Override
  Response getResponse() {
    String msg;
    if (transferService.get(getUser()).isPresent()) {
      msg = THANKS_FOR_CAPTION.getResponse();
    } else {
      msg = TEXT_NOT_SUPPORTED.getResponse();
    }
    return factory.getResponse(msg, update, ResponseTypes.TEXT);
  }

  private void saveCaption(Path path, String msg) {
    try (BufferedWriter wr = Files.newBufferedWriter(path)) {
      wr.write(msg);
      wr.newLine();
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }
}
