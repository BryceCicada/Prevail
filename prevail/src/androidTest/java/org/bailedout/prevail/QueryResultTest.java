package org.bailedout.prevail;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class QueryResultTest {
  @Test
  public void test_isClosed_SHOULD_returnFalse_IF_justConstructed() {
    QueryResult<Object> r = new QueryResult<Object>();
    assertThat(r.isClosed(), is(false));
  }
}
