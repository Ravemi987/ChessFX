package fr.chessproject.chessfx.controller;

import fr.chessproject.chessfx.model.game.ClockModel;

public class ClocksService {
    private final ClockModel clock;
    private Runnable timeoutCallback;

    private long lastUpdate = 0;

    public ClocksService(ClockModel clock) {
        this.clock = clock;
    }

    public void setTimeoutCallback(Runnable r) {
        this.timeoutCallback = r;
    }

    public void reset(double initialTime) {
        clock.reset(initialTime);
        lastUpdate = 0;
    }

    public void update(long now) {
        if (lastUpdate == 0) {
            lastUpdate = now;
            return;
        }

        double delta = (now - lastUpdate) / 1_000_000_000.0;

        clock.update(delta);

        if (clock.isTimeout()) {
            clock.stop();
            if (timeoutCallback != null) {
                timeoutCallback.run();
            }
        }

        lastUpdate = now;
    }
}
