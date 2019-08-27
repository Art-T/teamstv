package com.teamstv.telegrambot.handlers;

import static com.teamstv.telegrambot.BotReplies.REMOVE;
import static com.teamstv.telegrambot.model.BotCommandsEnum.LIST;

import com.teamstv.telegrambot.handlers.messages.Response;
import com.teamstv.telegrambot.handlers.messages.TextResponse;
import com.teamstv.telegrambot.model.Keyboard;
import com.teamstv.telegrambot.model.Photo;
import com.teamstv.telegrambot.services.TransferService;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class ListCommandHandler extends Handler {

  private final Keyboard keyboard;

  protected ListCommandHandler(
      TransferService<Integer, Photo> transferService,
      Keyboard keyboard) {
    super(transferService);
    this.keyboard = keyboard;
  }

  @Override
  Optional<Photo> operateOnContent(BotApiObject content) {
    PhotoSize photoSize = (PhotoSize) content;
    Optional<Photo> optionalPhoto = transferService.getByFileID(photoSize.getFileId());
    optionalPhoto.ifPresent(
        p -> {
          SendPhoto sendPhoto = new SendPhoto();
          sendPhoto.setChatId(update.getMessage().getChatId());
          sendPhoto.setPhoto(p.getFileId());
          sendPhoto.setCaption(p.getCaption());
          try {
            sender.execute(sendPhoto);
          } catch (TelegramApiException e) {
            log.error("Error while sending photo {}", p.getFileId(), e);
          }
        }
    );
    return optionalPhoto;
  }

  @Override
  Optional<? extends BotApiObject> getContent() {
    String msg = update.getMessage().getText();
    if (msg.equals(LIST.getName())) {
      return Optional.of(update.getMessage());
    }
    return Optional.empty();
  }

  @Override
  void createKeyboard(Photo model, PartialBotApiMethod msg) {
    model.setListed(true);
    keyboard.keyboard(model, model.getTransferId()).ifPresent(((SendMessage) msg)::setReplyMarkup);
    model.setListed(false);
  }

  @Override
  Response getResponse() {
    return new TextResponse(REMOVE.getResponse(), update);
  }
}
