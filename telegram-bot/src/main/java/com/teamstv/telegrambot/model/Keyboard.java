package com.teamstv.telegrambot.model;

import com.teamstv.telegrambot.BotProperties;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

/**
 * Class for inline keyboard.
 *
 * @author talipa
 */
@Component
public class Keyboard {

  private final BotProperties properties;

  public Keyboard(BotProperties properties) {
    this.properties = properties;
  }

  public Optional<InlineKeyboardMarkup> keyboard(Photo model, int id) {
    if (!model.isLoaded() && !model.hasCaption()) {
      return keyboard(getAllButtons(id));
    }
    if (!model.isLoaded() && model.hasCaption()) {
      return keyboard(getDownloadButtons(id));
    }
    if (!model.hasCaption()) {
      return keyboard(getCaptionButtons(id));
    }
    if (model.isListed()) {
      return keyboard(getDeleteButton(id));
    }
    return Optional.empty();
  }

  private Optional<InlineKeyboardMarkup> keyboard(List<InlineKeyboardButton> buttons) {
    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
    markupInline.setKeyboard(Collections.singletonList(buttons));
    return Optional.of(markupInline);
  }

  private List<InlineKeyboardButton> getAllButtons(int id) {
    return Arrays.asList(
        getButton(properties.getDownloadButton(), id + ButtonNameEnum.DOWNLOAD.getData()),
        getButton(properties.getCaptionButton(), id + ButtonNameEnum.ADD_CAPTION.getData()),
        getButton(properties.getCancelButton(), id + ButtonNameEnum.CANCEL.getData())
    );
  }

  private List<InlineKeyboardButton> getDownloadButtons(int id) {
    return Arrays.asList(
        getButton(properties.getDownloadButton(), id + ButtonNameEnum.DOWNLOAD.getData()),
        getButton(properties.getCancelButton(), id + ButtonNameEnum.CANCEL.getData())
    );
  }

  private List<InlineKeyboardButton> getCaptionButtons(int id) {
    return Arrays.asList(
        getButton(properties.getCaptionButton(), id + ButtonNameEnum.ADD_CAPTION.getData()),
        getButton(properties.getCancelButton(), id + ButtonNameEnum.CANCEL.getData())
    );
  }

  private List<InlineKeyboardButton> getDeleteButton(int id) {
    return Collections.singletonList(
        getButton(properties.getDeleteButton(), id + ButtonNameEnum.CANCEL.getData())
    );
  }

  private InlineKeyboardButton getButton(String text, String data) {
    return new InlineKeyboardButton()
        .setText(text)
        .setCallbackData(data);
  }
}
