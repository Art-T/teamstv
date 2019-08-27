package com.teamstv.telegrambot.providers;

import com.teamstv.telegrambot.services.IdGenerator;
import java.util.concurrent.atomic.AtomicInteger;
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
    if (generator.get() == Integer.MAX_VALUE) {
      generator.set(0);
    }
    return generator.incrementAndGet();
  }
}
