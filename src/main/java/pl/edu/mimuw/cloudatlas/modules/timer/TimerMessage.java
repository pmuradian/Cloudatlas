package pl.edu.mimuw.cloudatlas.modules.timer;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class TimerMessage implements Comparable {
    public String requestID;
    private Long delay;
    private Date delayDate;
    private Callable function;
    private Long firingTime;

    public Long getFiringTime() {
        return this.delay + this.delayDate.getTime();
    }

    TimerMessage(String requestID, Long delay, Date delayDate, Callable function) {
        this.requestID = requestID;
        this.delay = delay;
        this.delayDate = delayDate;
        this.function = function;
    }

    @Override
    public int compareTo(Object o) {
        if (this.getFiringTime() > ((TimerMessage) o).getFiringTime()) {
            return 1;
        }
        return 0;
    }

    public void performCallback() {
        try {
            this.function.call();
        } catch (Exception e) {
            System.out.println("Error while invoking callback");
            e.printStackTrace();
        }
    }
}
