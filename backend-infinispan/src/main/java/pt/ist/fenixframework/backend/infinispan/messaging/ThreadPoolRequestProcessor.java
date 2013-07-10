package pt.ist.fenixframework.backend.infinispan.messaging;

import org.jgroups.blocks.Response;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.infinispan.InfinispanBackEnd;
import pt.ist.fenixframework.jmx.JmxUtil;
import pt.ist.fenixframework.jmx.MethodType;
import pt.ist.fenixframework.jmx.annotations.MBean;
import pt.ist.fenixframework.jmx.annotations.ManagedAttribute;
import pt.ist.fenixframework.jmx.annotations.ManagedOperation;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
@MBean(category = "messaging", description = "Messaging thread pool executor", objectName = "MessaginThreadPool")
public class ThreadPoolRequestProcessor {

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
    private final ThreadPoolExecutor executorService;
    private final Queue<RequestRunnable> pendingRequests;
    private final Object statLock = new Object();
    private final AtomicInteger taskExecutedCounter;
    private final AtomicLong lastReset;
    private final AtomicLong syncServiceTime;
    private final AtomicLong asyncServiceTime;

    public ThreadPoolRequestProcessor(int coreThreads, int maxThreads, int keepAliveTime, final String applicationName) {
        taskExecutedCounter = new AtomicInteger(0);
        lastReset = new AtomicLong(System.nanoTime());
        syncServiceTime = new AtomicLong(0);
        asyncServiceTime = new AtomicLong(0);
        JmxUtil.processInstance(this, applicationName, InfinispanBackEnd.BACKEND_NAME, null);
        this.pendingRequests = new LinkedBlockingDeque<RequestRunnable>();
        executorService = new ThreadPoolExecutor(coreThreads, maxThreads, keepAliveTime, TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, applicationName + "-request-processor-" + THREAD_COUNTER.getAndIncrement());
                    }
                },
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        if (r instanceof RequestRunnable) {
                            pendingRequests.add((RequestRunnable) r);
                            //to avoid some strange case in which the executor rejects the task and also
                            //removes all the threads.
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    RequestRunnable requestRunnable;
                                    while ((requestRunnable = pendingRequests.poll()) != null) {
                                        requestRunnable.execute();
                                    }
                                }
                            });
                        }
                    }
                }
        );
    }

    public final void execute(String data, Response response) {
        executorService.execute(new RequestRunnable(response, data));
    }

    @ManagedAttribute(description = "Returns the core thread pool size.")
    public final int getCoreThreads() {
        return executorService.getCorePoolSize();
    }

    @ManagedAttribute(description = "Sets the core thread pool size. The core thread pool size should be greater " +
            "than zero.", method = MethodType.SETTER)
    public void setCoreThreads(int coreThreads) {
        executorService.setCorePoolSize(coreThreads);
    }

    @ManagedAttribute(description = "Returns the maximum thread pool size.")
    public final int getMaxThreads() {
        return executorService.getMaximumPoolSize();
    }

    @ManagedAttribute(description = "Sets the maximum thread pool size. The maximum pool size should greater or " +
            "equals to the core pool size.", method = MethodType.SETTER)
    public final void setMaxThreads(int maxThreads) {
        executorService.setMaximumPoolSize(maxThreads);
    }

    @ManagedAttribute(description = "Returns the idle time in milliseconds before the thread be terminated.")
    public final long getKeepAliveTime() {
        return executorService.getKeepAliveTime(TimeUnit.MILLISECONDS);
    }

    @ManagedAttribute(description = "Sets the idle time in milliseconds for which a thread can be idle before be " +
            "terminated. This value should be higher than zero.", method = MethodType.SETTER)
    public final void setKeepAliveTime(long keepAliveTime) {
        executorService.setKeepAliveTime(keepAliveTime, TimeUnit.MILLISECONDS);
    }

    @ManagedAttribute(description = "Returns the current enqueued tasks.")
    public final int getQueueSize() {
        return pendingRequests.size();
    }

    @ManagedAttribute(description = "Returns the current running threads.")
    public int getNumberOfRunningThreads() {
        return executorService.getActiveCount();
    }

    @ManagedAttribute(description = "Returns the number of exeucted tasks since last reset.")
    public final int getNumberOfExecutedTasks() {
        return taskExecutedCounter.get();
    }

    @ManagedAttribute(description = "Return the time elapsed in seconds since last reset.")
    public final long getTimeElapsedSinceLastReset() {
        return NANOSECONDS.toSeconds(System.nanoTime() - lastReset.get());
    }

    @ManagedAttribute(description = "Returns the average number of tasks executed per second since last reset")
    public final double getAverageTasksExecutedPerSecond() {
        synchronized (statLock) {
            long timeElapsed = getTimeElapsedSinceLastReset();
            if (timeElapsed > 0) {
                return taskExecutedCounter.get() * 1.0 / timeElapsed;
            }
            return 0;
        }
    }

    @ManagedAttribute(description = "Returns the average synchronous service time in milliseconds")
    public final double getAverageSyncServiceTime() {
        synchronized (statLock) {
            long tasks = taskExecutedCounter.get();
            if (tasks > 0) {
                return NANOSECONDS.toMillis(syncServiceTime.get()) * 1.0 / tasks;
            }
            return 0;
        }
    }

    @ManagedAttribute(description = "Returns the average synchronous service time in milliseconds")
    public final double getAverageAsyncServiceTime() {
        synchronized (statLock) {
            long tasks = taskExecutedCounter.get();
            if (tasks > 0) {
                return NANOSECONDS.toMillis(asyncServiceTime.get()) * 1.0 / tasks;
            }
            return 0;
        }
    }

    @ManagedOperation(description = "Reset the thread counter")
    public final void reset() {
        synchronized (statLock) {
            taskExecutedCounter.set(0);
            lastReset.set(System.nanoTime());
            syncServiceTime.set(0);
            asyncServiceTime.set(0);
        }
    }

    private class RequestRunnable implements Runnable {

        private final Response response;
        private final String data;

        private RequestRunnable(Response response, String data) {
            this.response = response;
            this.data = data;
        }

        @Override
        public void run() {
            execute();
            RequestRunnable requestRunnable;
            while ((requestRunnable = pendingRequests.poll()) != null) {
                requestRunnable.execute();
            }
        }

        public void execute() {
            long start = 0;
            long end = 0;
            try {
                start = System.nanoTime();
                Object reply = FenixFramework.handleRequest(data);
                end = System.nanoTime();
                if (response != null) {
                    response.send(reply, false);
                }
            } catch (Throwable throwable) {
                end = System.nanoTime();
                if (response != null) {
                    response.send(throwable, true);
                }
            } finally {
                synchronized (statLock) {
                    taskExecutedCounter.incrementAndGet();
                    if (start != 0 && end != 0) {
                        AtomicLong duration = response == null ? asyncServiceTime : syncServiceTime;
                        duration.addAndGet(end - start);
                    }
                }
            }
        }
    }
}
