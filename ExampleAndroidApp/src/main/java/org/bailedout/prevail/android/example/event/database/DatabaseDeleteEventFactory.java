package org.bailedout.prevail.android.example.event.database;

import com.google.common.base.Optional;
import org.bailedout.prevail.event.DeleteEndEvent;
import org.bailedout.prevail.type.Key;

import static org.bailedout.prevail.event.factory.DeleteEventFactory.EmptyDeleteEventFactory;

public class DatabaseDeleteEventFactory extends EmptyDeleteEventFactory {
  @Override
  public Optional<DeleteEndEvent> endEvent(final Key key, final int numValuesDeleted) {
    return Optional.of(new DeleteEndEvent(key, numValuesDeleted));
  }
}
