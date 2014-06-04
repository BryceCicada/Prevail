package org.bailedout.prevail.chunk;

import org.bailedout.prevail.type.Value;

import java.io.Closeable;

public interface QueryResult<V extends Value> extends Iterable<V>, Closeable {

}
