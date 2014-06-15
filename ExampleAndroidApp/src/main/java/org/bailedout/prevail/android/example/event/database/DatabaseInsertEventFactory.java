package org.bailedout.prevail.android.example.event.database;

import com.google.common.base.Optional;
import org.bailedout.prevail.event.InsertEndEvent;
import org.bailedout.prevail.type.Key;
import org.bailedout.prevail.type.Value;

import static org.bailedout.prevail.event.factory.InsertEventFactory.EmptyInsertEventFactory;

public class DatabaseInsertEventFactory extends EmptyInsertEventFactory {
  @Override
  public Optional<InsertEndEvent> endEvent(final Key key, final Value value) {
    return Optional.of(new InsertEndEvent(key, value));
  }
}
