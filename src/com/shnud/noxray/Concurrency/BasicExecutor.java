package com.shnud.noxray.Concurrency;

import com.shnud.noxray.NoXray;
import org.bukkit.Bukkit;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 * Created by Andrew on 07/01/2014.
 */
public class BasicExecutor extends Thread {

    private final Object _monitor = new Object();
    private final ConcurrentLinkedQueue<Runnable> _tasks = new ConcurrentLinkedQueue<Runnable>();
    public static BasicExecutor currentThread() {
        return (BasicExecutor) Thread.currentThread();
    }

    public void execute(Runnable task) {
        _tasks.add(task);
        synchronized (_monitor) {
            _monitor.notify();
        }
    }

    public void run() {
        synchronized (_monitor) {
            Thread.currentThread().setName("No X-ray Executor Thread");

            try {
                while(true) {
                    while(_tasks.isEmpty()) {
                        _monitor.wait();
                    }

                    _tasks.remove().run();
                }
            } catch (InterruptedException e) {
                NoXray.getInstance().getLogger().log(Level.WARNING, "Executor was interrupted");
            }
        }
    }

    /**
     * Sneakily uses the Bukkit scheduler to schedule some execution on this thread
     * @param task the task to be scheduled
     * @param ticksInFuture how far in the future we should try to execute it (may be delayed if under load)
     */
    public void schedule(final Runnable task, final int ticksInFuture) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(NoXray.getInstance(), new Runnable() {
            @Override
            public void run() {
                execute(task);
            }
        }, ticksInFuture);
    }
}
