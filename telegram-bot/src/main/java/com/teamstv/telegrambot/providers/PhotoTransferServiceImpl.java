package com.teamstv.telegrambot.providers;

import com.teamstv.telegrambot.BotProperties;
import com.teamstv.telegrambot.model.Photo;
import com.teamstv.telegrambot.services.TransferService;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Simple implementation of transfer service based on ConcurrentHashMap
 *
 * @author talipa
 */

@Service
public class PhotoTransferServiceImpl implements TransferService<Integer, Photo> {

  private final int capacity;
  private final Map<Integer, Photo> photoSizeMap;
  private final Map<String, Queue<Integer>> userMap;
  private final Queue<Photo> photoQueue;
  private Logger log = LoggerFactory.getLogger(TransferService.class);

  public PhotoTransferServiceImpl(BotProperties properties) {
    capacity = properties.getTransferServiceCapacity();
    photoSizeMap = new ConcurrentHashMap<>(2 * capacity);
    userMap = new ConcurrentHashMap<>(2 * capacity);
    photoQueue = new ArrayBlockingQueue<>(capacity);
  }

  @Override
  public Optional<Photo> get(Integer id) {
    return Optional.ofNullable(photoSizeMap.get(id));
  }

  @Override
  public void set(Integer id, Photo photo) {
    photo.setTransferId(id);
    addModelToQueue(photo);
    photoSizeMap.put(id, photo);
  }

  @Override
  public void delete(Integer id) {
    Photo model = photoSizeMap.remove(id);
    photoQueue.remove(model);
  }

  @Override
  public Optional<Photo> get(String user) {
    Queue<Integer> actions = userMap.get(user);
    if (actions != null && !actions.isEmpty()) {
      int id = actions.peek();
      return Optional.of(photoSizeMap.get(id));
    }
    return Optional.empty();
  }

  @Override
  public void set(String user, Integer id) {
    Queue<Integer> actions = userMap.get(user);
    if (userMap.get(user) == null) {
      actions = new ArrayBlockingQueue<>(capacity);
      userMap.put(user, actions);
    }
    actions.offer(id);
  }

  @Override
  public void delete(String user) {
    Queue<Integer> actions = userMap.get(user);
    if (actions != null && !actions.isEmpty()) {
      actions.poll();
      return;
    }
    userMap.remove(user);
  }

  @Override
  public Collection<Photo> getAll() {
    return photoSizeMap.values();
  }

  @Override
  public Optional<Photo> getByFileID(String fileID) {
    return photoSizeMap.values().parallelStream().filter(
        p -> p.getFileId().equals(fileID)
    ).findFirst();
  }

  private void addModelToQueue(Photo photo) {
    Photo model = null;
    if (photoQueue.size() == capacity) {
      model = photoQueue.poll();
    }
    if (model != null) {
      photoSizeMap.remove(model.getTransferId());
      try {
        model.delete();
      } catch (IOException e) {
        log.error("Error while deleting files for model = {} ", model.getFileId(), e);
      }
    }
    photoQueue.offer(photo);
  }

}
