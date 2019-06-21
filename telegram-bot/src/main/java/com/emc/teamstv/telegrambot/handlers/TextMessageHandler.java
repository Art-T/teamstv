package com.emc.teamstv.telegrambot.handlers;

import static com.emc.teamstv.telegrambot.BotReplies.TEXT_NOT_SUPPORTED;
import static com.emc.teamstv.telegrambot.BotReplies.THANKS_FOR_CAPTION;

import com.emc.teamstv.telegrambot.BotProperties;
import com.emc.teamstv.telegrambot.handlers.messages.Response;
import com.emc.teamstv.telegrambot.model.Keyboard;
import com.emc.teamstv.telegrambot.model.Photo;
import com.emc.teamstv.telegrambot.services.TransferService;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

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
      TransferService<String, Photo> transferService,
      Keyboard keyboard, BotProperties properties) {
    super(transferService);
    this.keyboard = keyboard;
    this.properties = properties;
  }

  @Override
  public void onUpdateReceived() {
    if (waitForCaptionMsg(update)) {
      return;
    }
    BotApiMethod msg = prepareResponse(TEXT_NOT_SUPPORTED);
    sendText(msg);
  }

  @Override
  Optional<? extends BotApiObject> getContent() {
    return Optional.empty();
  }

  @Override
  Optional<Photo> operateOnContent(BotApiObject content) {
    return Optional.empty();
  }

  @Override
  void createKeyboard(Photo model, BotApiMethod msg) {

  }

  @Override
  Response getResponse() {
    return null;
  }

  private boolean waitForCaptionMsg(Update update) {
    Optional<Photo> optModel = transferService.get(getUser());
    optModel.ifPresent(
        model -> {
          String caption = update.getMessage().getText();
          log.info("Caption: {}. For photo: {} provided.", caption, model.getFileId());
          model.setCaption(caption);
          Path captionPath = Paths.get(properties.getPath(), model.getFileId() + ".txt");
          model.setCaptionLocalPath(captionPath.toString());
          saveCaption(captionPath, caption);
          SendMessage msg = (SendMessage) prepareResponse(THANKS_FOR_CAPTION);
          keyboard.keyboard(model, model.getTransferId())
              .ifPresent(msg::setReplyMarkup);
          sendText(msg);
          transferService.delete(getUser());
          model.setTransferId("");
        }
    );
    return optModel.isPresent();
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
