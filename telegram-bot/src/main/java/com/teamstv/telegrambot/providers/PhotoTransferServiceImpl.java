package com.teamstv.telegrambot.providers;

import com.teamstv.telegrambot.BotProperties;
import com.teamstv.telegrambot.model.Photo;
import com.teamstv.telegrambot.services.IdGenerator;
import com.teamstv.telegrambot.services.TransferService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

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
  private final BotProperties properties;
  private final IdGenerator<Integer> idGenerator;

  public PhotoTransferServiceImpl(BotProperties properties,
      IdGenerator<Integer> idGenerator) {
    capacity = properties.getTransferServiceCapacity();
    this.idGenerator = idGenerator;
    photoSizeMap = new MapDecorator(2 * capacity);
    userMap = new HashMap<>(2 * capacity);
    this.properties = properties;
  }

  @PostConstruct
  public void init() throws IOException {
    Path path = Paths.get(properties.getPath());
    try (Stream<Path> files = Files.walk(path, 1)) {
      files.sorted(getComparator())
          .map(Path::getFileName)
          .map(Path::toString)
          .filter(s -> s.endsWith(".jpg"))
          .map(s -> s.replace(".jpg", ""))
          .forEach(s -> {
                Photo photo = createPhotoFromFile(s, path);
                photoSizeMap.put(photo.getTransferId(), photo);
              }
          );
    }
  }

  private Photo createPhotoFromFile(String s, Path path) {
    PhotoSize photoSize = new PhotoSizeDecorator(s);
    Photo photo = Photo.getPhotoModel(photoSize, s);
    photo.setLoaded(true);
    photo.setPhotoLocalPath(Paths.get(path.toString(), s + ".jpg").toString());
    Path captionPath = Paths.get(path.toString(), s + ".txt");
    photo.setCaptionLocalPath(captionPath.toString());
    String caption = readCaption(captionPath);
    photo.setCaption(caption);
    photo.setTransferId(idGenerator.getUniq());
    return photo;
  }

  private String readCaption(Path path) {
    try {
      return new String(Files.readAllBytes(path));
    } catch (IOException e) {
      return "";
    }
  }

  private Comparator<Path> getComparator() {
    return (p1, p2) -> {
      try {
        return Files.getLastModifiedTime(p1).compareTo(Files.getLastModifiedTime(p2));
      } catch (IOException e) {
        return 0;
      }
    };
  }

  private static class PhotoSizeDecorator extends PhotoSize {

    private final String fileId;

    private PhotoSizeDecorator(String fileId) {
      this.fileId = fileId;
    }

    @Override
    public String getFileId() {
      return fileId;
    }
  }

  private static class MapDecorator extends LinkedHashMap<Integer, Photo> {

    private final int maxSize;

    private MapDecorator(int maxSize) {
      super(2 * maxSize);
      this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Entry<Integer, Photo> eldest) {
      return size() > maxSize;
    }

  }

  @Override
  public Optional<Photo> get(Integer id) {
    return Optional.ofNullable(photoSizeMap.get(id));
  }

  @Override
  public void set(Integer id, Photo photo) {
    photo.setTransferId(id);
    photoSizeMap.put(id, photo);
  }

  @Override
  public void delete(Integer id) {
    photoSizeMap.remove(id);
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
    return Collections.unmodifiableCollection(photoSizeMap.values());
  }

  @Override
  public Optional<Photo> getByFileID(String fileID) {
    return photoSizeMap.values().parallelStream().filter(
        p -> p.getFileId().equals(fileID)
    ).findFirst();
  }

}
