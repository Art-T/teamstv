package com.emc.teamstv.telegrambot.handlers;

import com.emc.teamstv.telegrambot.model.ButtonNameEnum;
import com.emc.teamstv.telegrambot.model.Photo;
import com.emc.teamstv.telegrambot.services.TransferService;
import java.util.Optional;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public abstract class CallbackHandler extends Handler {

  protected CallbackHandler(
      TransferService<Integer, Photo> transferService) {
    super(transferService);
  }

  @Override
  final Optional<? extends BotApiObject> getContent() {
    return Optional.ofNullable(update.getCallbackQuery());
  }

  final int getTransferID(ButtonNameEnum nameEnum) {
    return getContent().map(
        c -> ((CallbackQuery) c).getData().replace(nameEnum.getData(), "")
    ).map(Integer::valueOf).orElse(Integer.MAX_VALUE);
  }

  final Optional<Photo> getPhotoModel(ButtonNameEnum nameEnum) {
    return transferService.get(getTransferID(nameEnum));
  }

}
