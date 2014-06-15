package org.bailedout.prevail.type;

public interface KeyFactory<K extends Key, V extends Value> {
  K createKey(V value);

  public static class HashCodeKeyFactory<V> implements KeyFactory<Simple<Integer>, Simple<V>> {
    @Override
    public Simple<Integer> createKey(final Simple<V> value) {
      return new Simple(value.hashCode());
    }
  }
}
