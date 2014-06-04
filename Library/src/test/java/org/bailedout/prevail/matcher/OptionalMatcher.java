package org.bailedout.prevail.matcher;

import com.google.common.base.Optional;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class OptionalMatcher {
  public static <T> Matcher<Optional<T>> hasValue(final T value) {
    return new TypeSafeMatcher<Optional<T>>() {

      public Optional<T> mOptional;

      @Override
      protected boolean matchesSafely(final Optional<T> optional) {
        mOptional = optional;
        return optional.isPresent() && optional.get().equals(value);
      }

      @Override
      public void describeTo(final Description description) {
        description.appendText(mOptional.toString());
      }

      @Override
      public void describeMismatchSafely(final Optional<T> optional,final Description description) {
        description.appendText(optional.toString());
      }
    };
  }
}
