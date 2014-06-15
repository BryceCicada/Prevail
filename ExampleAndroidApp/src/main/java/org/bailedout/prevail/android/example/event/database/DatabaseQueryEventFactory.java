package org.bailedout.prevail.android.example.event.database;

import com.google.common.base.Optional;
import org.bailedout.prevail.event.QueryEndEvent;
import org.bailedout.prevail.type.Key;

import static org.bailedout.prevail.event.factory.QueryEventFactory.EmptyQueryEventFactory;

public class DatabaseQueryEventFactory extends EmptyQueryEventFactory {
  @Override
  public Optional<QueryEndEvent> endEvent(final Key key, final Iterable value) {
    return Optional.of(new QueryEndEvent(key, value));
  }
}
