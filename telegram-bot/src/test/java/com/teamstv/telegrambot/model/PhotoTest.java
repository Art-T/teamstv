package com.teamstv.telegrambot.model;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

public class PhotoTest {

  private String given =
      "{\"photoSize\":{},\"fileId\":\"test\",\"loaded\":false,\"localPath\":null,\"caption\":null,\"isLoaded\":false,\"transferId\":\"\"}";

  private Photo photo;
  private ObjectMapper mapper;

  @Before
  public void setUp() throws Exception {
    PhotoSize photoSize = new PhotoSize();
    photo = Photo.getPhotoModel(photoSize, "test");
    mapper = new ObjectMapper();
  }

  @Test
  public void shouldSerializePhotoToGivenString() throws JsonProcessingException {
    PhotoSize photoSize = new PhotoSize();
    photo = Photo.getPhotoModel(photoSize, "test");
    assertEquals(given, mapper.writeValueAsString(photo));
  }

  @Test
  public void shouldDeserializePhotoFromString() throws IOException {
    assertEquals(photo, mapper.readValue(given, Photo.class));
  }
}