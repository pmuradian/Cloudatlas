package pl.edu.mimuw.cloudatlas.modules.timer;

import java.util.*;
import java.util.Timer;
import java.util.concurrent.Callable;

public class TimerModule {

//    private static Queue<TimerModule> MessageQueue;
    private ArrayList messages = new ArrayList<TimerMessage>();
    private Sleeper sleeper = null;

    // Private Sleeper class for callback invocation
    private class Sleeper {
        private Timer timer = new Timer();
        private String headMessageID = null;
        private TimerModule module = null;

        Sleeper(TimerModule module) {
            this.module = module;
        }

        void wait(Long duration) {

        }

        void notify(TimerMessage message) {
            if (this.headMessageID.equals(message.requestID)) {
                return;
            }

            this.headMessageID = message.requestID;
            timer.cancel(); // Stop previous timer
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        message.performCallback();
                        module.removeMessage(message.requestID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 0);
        }
    }

    TimerModule() {
        this.sleeper = new Sleeper(this);
    }

    public static void main(String[] args) {
        TimerModule timer = new TimerModule();
        Callable a = new Callable() {
            @Override
            public Object call() throws Exception {
                System.out.println("completion fired");
                return null;
            }
        };
        TimerMessage message = new TimerMessage("1", 3l, new Date(), a);
        TimerMessage message1 = new TimerMessage("2", 5l, new Date(), a);
    }

    public void addMessage(TimerMessage message) {
        this.messages.add(message);
        Collections.sort(messages);
        this.sleeper.notify((TimerMessage) messages.get(0));
    }

    public void removeMessage(String messageID) {
        for (Iterator<TimerMessage> iterator = messages.iterator(); iterator.hasNext(); ) {
            if (iterator.next().requestID == messageID)
                iterator.remove();
        }
        this.sleeper.notify((TimerMessage) messages.get(0));
    }
}
