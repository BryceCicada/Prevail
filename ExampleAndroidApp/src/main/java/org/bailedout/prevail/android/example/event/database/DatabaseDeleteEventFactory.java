package org.bailedout.prevail.android.example.event.database;

import com.google.common.base.Optional;
import org.bailedout.prevail.event.DeleteEndEvent;

import static org.bailedout.prevail.event.factory.DeleteEventFactory.EmptyDeleteEventFactory;

public class DatabaseDeleteEventFactory extends EmptyDeleteEventFactory {
  @Override
  public Optional<DeleteEndEvent> endEvent(final Object key, final int numValuesDeleted) {
    return Optional.of(new DeleteEndEvent(key, numValuesDeleted));
  }
}
