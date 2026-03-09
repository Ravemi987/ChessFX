package fr.chessproject.chessfx.model.game;

public class ClockModel {

    private double remainingTime;
    private boolean ticking;


    public ClockModel(double initialTime) {
        this.remainingTime = initialTime;
    }

    public void update(double delta) {
        if (ticking) {
            remainingTime = Math.max(0, remainingTime - delta);
        }
    }

    public void reset(double initialTime) {
        this.remainingTime = initialTime;
    }

    public boolean isTimeout() {
        return remainingTime <= 0;
    }

    public void stop() {
        ticking = false;
    }

    public void start() {
        ticking = true;    
    }

    public double getRemainingTime() {
        return remainingTime;
    }
}