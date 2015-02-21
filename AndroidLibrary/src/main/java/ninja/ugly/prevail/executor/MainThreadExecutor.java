package ninja.ugly.prevail.executor;


import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

public final class MainThreadExecutor implements Executor {
  private final Handler handler = new Handler(Looper.getMainLooper());

  @Override
  public void execute(Runnable r) {
    handler.post(r);
  }
}

