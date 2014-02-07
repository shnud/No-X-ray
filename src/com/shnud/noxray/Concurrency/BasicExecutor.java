package com.shnud.noxray.Concurrency;

import com.shnud.noxray.NoXray;
import org.bukkit.Bukkit;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Andrew on 07/01/2014.
 */
public class BasicExecutor extends Thread {

    // A lock used to ensure we can only interrupt the thread while it is not processing a task
    private final Object _cleanStopLock = new Object();
    private final LinkedBlockingQueue<Runnable> _tasks = new LinkedBlockingQueue<Runnable>();
    private final String _name;
    private Thread _thread = this;

    public BasicExecutor(String name) {
        _name = name;
    }

    public void execute(Runnable task) {
        _tasks.add(task);
    }

    public void run() {
        Thread.currentThread().setName(_name);

        try {
            while(!Thread.interrupted()) {
                Runnable task = _tasks.take();

                synchronized (_cleanStopLock) {
                    task.run();
                }
            }
        } catch (InterruptedException e) {

        }
    }

    public void cancel() {
        synchronized (_cleanStopLock) {
            _thread.interrupt();
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
