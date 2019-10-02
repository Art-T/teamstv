package com.teamstv.telegrambot.handlers;

import static com.teamstv.telegrambot.BotReplies.THANKS_FOR_PHOTO;

import com.teamstv.telegrambot.handlers.messages.Response;
import com.teamstv.telegrambot.handlers.messages.ResponseFactory;
import com.teamstv.telegrambot.handlers.messages.ResponseTypes;
import com.teamstv.telegrambot.model.Keyboard;
import com.teamstv.telegrambot.model.Photo;
import com.teamstv.telegrambot.services.IdGenerator;
import com.teamstv.telegrambot.services.TransferService;
import java.util.Comparator;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

/**
 * Class does: 1. Handling photo messages. 2. Creates Inline keyboard.
 *
 * @author talipa
 */

@Service
public class PhotoMessageHandler extends Handler {

  private final Keyboard keyboard;
  private final IdGenerator<Integer> generator;

  public PhotoMessageHandler(
      Keyboard keyboard,
      TransferService<Integer, Photo> transferService,
      ResponseFactory factory,
      IdGenerator<Integer> generator) {
    super(transferService, factory);
    this.keyboard = keyboard;
    this.generator = generator;
  }

  @Override
  Optional<? extends BotApiObject> getContent() {
    return update.getMessage()
        .getPhoto()
        .stream()
        .max(Comparator.comparing(PhotoSize::getFileSize));
  }

  @Override
  Optional<Photo> operateOnContent(BotApiObject content) {
    PhotoSize photo = (PhotoSize) content;
    Photo model = Photo.getPhotoModel(photo, photo.getFileId());
    model.setLoaded(false);
    log.info("PhotoSize object from user {} received", getUser());
    return Optional.of(model);
  }

  @Override
  void createKeyboard(Photo model, PartialBotApiMethod msg) {
    int id = generator.getUniq();
    keyboard.keyboard(model, id).ifPresent(((SendMessage) msg)::setReplyMarkup);
    transferService.set(id, model);
  }

  @Override
  Response getResponse() {
    return factory.getResponse(THANKS_FOR_PHOTO.getResponse(), update, ResponseTypes.TEXT);
  }
}
