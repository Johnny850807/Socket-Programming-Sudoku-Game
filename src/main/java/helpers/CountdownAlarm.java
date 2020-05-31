package helpers;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author johnny850807@gmail.com (Waterball))
 */
public class CountdownAlarm {
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private ScheduledFuture<?> currentScheduledFuture;
    private long time;
    private AtomicBoolean timeExpires = new AtomicBoolean();

    public CountdownAlarm(long time) {
        this.time = time;
    }

    public synchronized Countdown countdown(Runnable countdownListener) {
        timeExpires.set(false);
        if (!currentScheduledFuture.isDone()) {
            stopCountdown();
        }
        currentScheduledFuture = scheduler.schedule(() -> {
                    timeExpires.set(true);
                    countdownListener.run();
                }, time, TimeUnit.MILLISECONDS);

        return new Countdown() {
            @Override
            public void await() {
                try {
                    currentScheduledFuture.get();  // block until time's up
                } catch (InterruptedException | ExecutionException ignored) {
                    // the countdown is interrupted
                }
            }
        };
    }

    public boolean timeExpires() {
        return timeExpires.get();
    }

    public synchronized void stopCountdown() {
        timeExpires.set(false);
        if (currentScheduledFuture != null) {
            currentScheduledFuture.cancel(true);
        }
    }
}
