package com.teamstv.telegrambot.services;

/**
 * Service provides uniq ID values
 *
 * @param <T> type of id value
 * @author talipa
 */

public interface IdGenerator<T> {

  T getUniq();
}
