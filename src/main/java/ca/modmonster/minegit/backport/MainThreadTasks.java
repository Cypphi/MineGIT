package ca.modmonster.minegit.backport;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainThreadTasks {
    public static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<>();

    public static void execute(Runnable runnable) {
        TASKS.add(runnable);
    }
}
