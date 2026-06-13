package fr.chessproject.chessfx.service;

import fr.chessproject.chessfx.model.game.ClockModel;

public class ClockService {

    private final ClockModel model;

    private Runnable timeoutCallback;

    private long lastUpdate;

    public ClockService(ClockModel model) {
        this.model = model;
    }

    public void setTimeoutCallback(Runnable timeoutCallback) {
        this.timeoutCallback = timeoutCallback;
    }

    public void reset(double initialTime) {
        model.reset(initialTime);
        lastUpdate = 0;
    }

    public void start() {
        model.start();
    }

    public void stop() {
        model.stop();
    }

    public void addIncrement(double increment) {
        model.addTime(increment);
    }

    public void update(long now) {

        if (!model.isTicking() || lastUpdate == 0) {
            lastUpdate = now;
            return;
        }

        double delta = (now - lastUpdate) / 1_000_000_000.0;

        model.update(delta);

        if (model.isTimeout()) {
            model.stop();
            if (timeoutCallback != null) {
                timeoutCallback.run();
            }
        }

        lastUpdate = now;
    }
}
