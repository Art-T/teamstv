package com.teamstv.telegrambot.providers;

import static org.junit.Assert.*;

import com.teamstv.telegrambot.BotProperties;
import com.teamstv.telegrambot.model.Photo;
import com.teamstv.telegrambot.services.IdGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PhotoTransferServiceImplTest {

  private String filePath = "test";
  private String dirPath = "events";
  private PhotoTransferServiceImpl transferService;

  @Before
  public void init() throws IOException, InterruptedException {
    BotProperties properties = new BotProperties();
    properties.setTransferServiceCapacity(128);
    properties.setPath(dirPath);
    IdGenerator<Integer> idGenerator = new IdGeneratorImpl();
    transferService = new PhotoTransferServiceImpl(properties, idGenerator);
    Files.createDirectory(Paths.get(dirPath));
    for (int i = 0; i < 128 ; i++) {
      Thread.sleep(10);
      Files.createFile(Paths.get(dirPath, filePath + i + ".jpg"));
    }

    transferService.init();
  }

  @Test
  public void initTest() {
    Optional<Photo> optionalPhoto = transferService.get(1);
    if(optionalPhoto.isPresent()) {
      Photo photo = optionalPhoto.get();
      assertEquals("test0", photo.getFileId());
      assertEquals("", photo.getCaption());
      assertTrue(photo.isLoaded());
    } else {
      throw new AssertionError();
    }
  }

  @Test
  public void getAllTest() {
    Collection<Photo> photos = transferService.getAll();
    Iterator<Photo> photoIterator = photos.iterator();
    int i = 0;
    while (photoIterator.hasNext()) {
      Photo photo = photoIterator.next();
      assertEquals("test"+ i, photo.getFileId());
      i++;
    }
  }

  @After
  public void tearDown() throws IOException {
    for (int i = 0; i < 128 ; i++) {
      Files.deleteIfExists(Paths.get(dirPath, filePath + i + ".jpg"));
    }
    Files.deleteIfExists(Paths.get(dirPath));
  }
}