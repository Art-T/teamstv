package com.emc.teamstv.telegrambot.handlers;

import static com.emc.teamstv.telegrambot.BotReplies.LOAD_COMPLETED;

import com.emc.teamstv.telegrambot.BotProperties;
import com.emc.teamstv.telegrambot.handlers.messages.CallbackResponse;
import com.emc.teamstv.telegrambot.handlers.messages.Response;
import com.emc.teamstv.telegrambot.model.ButtonNameEnum;
import com.emc.teamstv.telegrambot.model.Keyboard;
import com.emc.teamstv.telegrambot.model.Photo;
import com.emc.teamstv.telegrambot.services.TransferService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Class for handling callbacks from inline keyboard. This one will load data into our server
 *
 * @author talipa
 */
@Service
public class DownloadCallbackHandler extends CallbackHandler {

  private final Keyboard keyboard;
  private final BotProperties properties;

  public DownloadCallbackHandler(
      TransferService<Integer, Photo> transferService,
      Keyboard keyboard,
      BotProperties properties) {
    super(transferService);
    this.keyboard = keyboard;
    this.properties = properties;

  }

  @Override
  Optional<Photo> operateOnContent(BotApiObject content) {
    Optional<Photo> optionalModel = getPhotoModel(ButtonNameEnum.DOWNLOAD);
    optionalModel.ifPresent(
        model -> {
          String fileId = model.getPhotoSize().getFileId();
          try {
            log.info("Found entry for fileId ={}", fileId);
            saveFile(model);
          } catch (TelegramApiException e) {
            log.error("Error while downloading file = " + fileId, e);
          }
        }
    );
    return optionalModel;
  }

  @Override
  void createKeyboard(Photo model, PartialBotApiMethod msg) {
    keyboard.keyboard(model, getTransferID(ButtonNameEnum.DOWNLOAD))
        .ifPresent(((EditMessageText) msg)::setReplyMarkup);
  }

  @Override
  Response getResponse() {
    return new CallbackResponse(LOAD_COMPLETED.getResponse(), update);
  }

  private void saveFile(Photo model) throws TelegramApiException {
    PhotoSize p = model.getPhotoSize();
    Path file = sender.downloadFile(getPath(p)).toPath();
    try (InputStream ios = Files.newInputStream(file)) {
      Path fileI = Paths.get(properties.getPath(), p.getFileId() + ".jpg");
      Files.copy(ios, fileI, StandardCopyOption.REPLACE_EXISTING);
      log.info("File {} loaded", fileI);
      model.setLoaded(true);
      model.setPhotoLocalPath(fileI.toString());
    } catch (IOException e) {
      log.error("Error while moving downloaded file = " + p.getFileId(), e);
    }
  }

  private String getPath(PhotoSize p) throws TelegramApiException {
    String path;
    if (p.hasFilePath()) {
      path = p.getFilePath();
    } else {
      GetFile getFile = new GetFile();
      getFile.setFileId(p.getFileId());
      File file = sender.execute(getFile);
      path = file.getFilePath();
    }
    return path;
  }
}
