package com.teamstv.telegrambot;

/**
 * Enumeration with available replays
 *
 * @author talipa
 */
public enum BotReplies {
  TEXT_NOT_SUPPORTED(", наш бот на данный момент умеет работать только с фотоконтентом."),
  THANKS_FOR_PHOTO(" , спасибо за фото, выберите дальнейшее действие."),
  LOAD_COMPLETED("Фото успешно загружено на сервер"),
  CLEAN_UP("Загрузка отменена, спасибо"),
  NULL_USER("Пожалуйста создайте имя пользователя в своем аккаунте телеграм."),
  SEND_CAPTION("Пожалуйста отправьте описание для фото"),
  THANKS_FOR_CAPTION("Спасибо что предоставили описание к фото"),
  REMOVE("Для удаления фото нажмите кнопку");

  private final String response;

  BotReplies(String response) {
    this.response = response;
  }

  public String getResponse() {
    return response;
  }
}
