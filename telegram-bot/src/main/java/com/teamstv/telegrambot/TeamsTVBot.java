package com.teamstv.telegrambot;

import com.teamstv.telegrambot.handlers.HandlerFactory;
import java.util.List;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


/**
 * Class represents telegram bot API which delegates methods to different handlers
 *
 * @author talipa
 */

@Service
public class TeamsTVBot extends TelegramLongPollingBot {

  private final BotProperties botProperties;
  private final HandlerFactory factory;

  public TeamsTVBot(BotProperties botProperties,
      HandlerFactory factory) {
    this.botProperties = botProperties;
    this.factory = factory;
  }

  public String getBotUsername() {
    return botProperties.getBotName();
  }

  public String getBotToken() {
    return botProperties.getToken();
  }

  @Override
  public synchronized void onUpdateReceived(Update update) {
    factory.getHandler(update).ifPresent(h -> {
      h.setUpdate(update);
      h.setSender(this);
      h.onUpdateReceived();
    });
  }

  @Override
  public void onUpdatesReceived(List<Update> updates) {
    updates.forEach(this::onUpdateReceived);
  }
}
