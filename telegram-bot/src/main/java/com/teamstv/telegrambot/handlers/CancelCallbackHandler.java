package com.teamstv.telegrambot.handlers;

import static com.teamstv.telegrambot.BotReplies.CLEAN_UP;

import com.teamstv.telegrambot.handlers.messages.CallbackResponse;
import com.teamstv.telegrambot.handlers.messages.Response;
import com.teamstv.telegrambot.model.ButtonNameEnum;
import com.teamstv.telegrambot.model.Photo;
import com.teamstv.telegrambot.services.TransferService;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;

/**
 * Class for handling callbacks from inline keyboard. This one will cancel any progression on data
 * loading
 *
 * @author talipa
 */
@Service
public class CancelCallbackHandler extends CallbackHandler {

  public CancelCallbackHandler(
      TransferService<Integer, Photo> transferService) {
    super(transferService);
  }

  @Override
  Optional<Photo> operateOnContent(BotApiObject content) {
    getPhotoModel(ButtonNameEnum.CANCEL).ifPresent(
        model -> {
          int transferId = getTransferID(ButtonNameEnum.CANCEL);
          transferService.delete(transferId);
          log.info("Transfer service cleaned for transferId = {}", transferId);
          try {
            model.delete();
          } catch (IOException e) {
            log.error("Error while deleting files for model = {} ", model.getFileId(), e);
          }
        }
    );
    return Optional.empty();
  }

  @Override
  Response getResponse() {
    return new CallbackResponse(CLEAN_UP.getResponse(), update);
  }
}
