package com.teamstv.telegrambot.services;

import java.util.Collection;
import java.util.Optional;

/**
 * Service used to transfer data between handlers
 *
 * @param <T> id value, used to get necessary transfer object
 * @param <E> transfer object
 * @author talipa
 */
public interface TransferService<T extends Number, E> {

  Optional<E> get(T id);

  void set(T id, E e);

  void delete(T id);

  Optional<E> get(String user);

  void set(String user, T id);

  void delete(String user);

  Collection<E> getAll();

  Optional<E> getByFileID(String fileID);

}
