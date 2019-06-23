package com.emc.teamstv.telegrambot.providers;

import com.emc.teamstv.telegrambot.services.IdGenerator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * Simple implementation of IdGenerator service based on AtomicLong
 *
 * @author talipa
 */

@Service
public class IdGeneratorImpl implements IdGenerator<Integer> {

  private static final AtomicInteger generator = new AtomicInteger(0);

  @Override
  public Integer getUniq() {
    if(generator.get() == Integer.MAX_VALUE) {
      generator.set(0);
    }
    return generator.incrementAndGet();
  }
}
