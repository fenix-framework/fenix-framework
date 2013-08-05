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

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
@MBean(category = "messaging", description = "Messaging thread pool executor", objectName = "MessaginThreadPool")
public class ThreadPoolRequestProcessor {

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
    private final ThreadPoolExecutor executorService;
    private final Queue<RequestRunnable> pendingRequests;
    private final LoadManager loadManager;
    private volatile Stats stats;

    public ThreadPoolRequestProcessor(int coreThreads, int maxThreads, int keepAliveTime, int maxQueueSize, int minQueueSize,
                                      final String applicationName) {
        this.stats = new Stats();
        this.loadManager = new LoadManager(minQueueSize, maxQueueSize);
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
                            enqueueTask((RequestRunnable) r);
                            //to avoid some strange case in which the executor rejects the task and also
                            //removes all the threads.
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    RequestRunnable requestRunnable;
                                    while ((requestRunnable = dequeueTaskWithoutWait()) != null) {
                                        requestRunnable.execute();
                                    }
                                }
                            });
                        }
                    }
                }
        );
        JmxUtil.processInstance(this, applicationName, InfinispanBackEnd.BACKEND_NAME, null);
    }

    public final void execute(String data, Response response) {
        executorService.execute(new RequestRunnable(response, data));
        stats.taskReceived();
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

    public final void setLoadListener(LoadListener loadListener) {
        loadManager.setLoadListener(loadListener);
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

    @ManagedAttribute(description = "Returns the number of synchronous executed tasks since last reset.")
    public final int getNumberOfExecutedSynTasks() {
        return stats.getNumberOfSyncTasks();
    }

    @ManagedAttribute(description = "Returns the number of asynchronous executed tasks since last reset.")
    public final int getNumberOfExecutedAsyncTasks() {
        return stats.getNumberOfAsyncTasks();
    }

    @ManagedAttribute(description = "Return the time elapsed in seconds since last reset.")
    public final long getTimeElapsedSinceLastReset() {
        return stats.getTimeSinceLastReset();
    }

    @ManagedAttribute(description = "Returns the average number of synchronous tasks executed per second since last reset")
    public final double getAverageSyncTasksExecutedPerSecond() {
        return stats.getNumberOfSyncTasksThroughput();
    }

    @ManagedAttribute(description = "Returns the average number of synchronous tasks executed per second since last reset")
    public final double getAverageAsyncTasksExecutedPerSecond() {
        return stats.getNumberOfAsyncTasksThroughput();
    }

    @ManagedAttribute(description = "Returns the average synchronous service time in milliseconds")
    public final double getAverageSyncServiceTime() {
        return stats.getAverageSyncTasksServiceTime();
    }

    @ManagedAttribute(description = "Returns the average synchronous total time in milliseconds")
    public final double getAverageSyncTotalTime() {
        return stats.getAverageSyncTasksTotalTime();
    }

    @ManagedAttribute(description = "Returns the average synchronous service time in milliseconds")
    public final double getAverageAsyncServiceTime() {
        return stats.getAverageAsyncTasksServiceTime();
    }

    @ManagedAttribute(description = "Returns the average synchronous total time in milliseconds")
    public final double getAverageAsyncTotalTime() {
        return stats.getAverageAsyncTasksTotalTime();
    }

    @ManagedAttribute(description = "Returns the tasks arrival rate in tasks per second")
    public final double getArrivalRate() {
        return stats.getArrivalRate();
    }

    @ManagedOperation(description = "Reset the thread counter")
    public final void reset() {
        stats = new Stats();
    }

    @ManagedAttribute(description = "Maximum queue size before sending an overload threshold reached notification")
    public final int getMaxQueueSizeNotificationThreshold() {
        return loadManager.getMaxQueueSizeThreshold();
    }

    @ManagedAttribute(description = "Maximum queue size before sending an overload threshold reached notification",
            method = MethodType.SETTER)
    public final void setMaxQueueSizeNotificationThreshold(int threshold) {
        loadManager.setMaxQueueSizeThreshold(threshold);
    }

    @ManagedAttribute(description = "Minimum queue size before sending an underload threshold reached notification")
    public final int getMinQueueSizeNotificationThreshold() {
        return loadManager.getMinQueueSizeThreshold();
    }

    @ManagedAttribute(description = "Minimum queue size before sending an underload threshold reached notification",
            method = MethodType.SETTER)
    public final void setMinQueueSizeNotificationThreshold(int threshold) {
        loadManager.setMinQueueSizeThreshold(threshold);
    }

    @ManagedAttribute(description = "Returns true if this queue is overloaded")
    public final boolean isOverloaded() {
        return loadManager.isOverLoaded();
    }

    @ManagedAttribute(description = "Returns true if this queue is underloaded")
    public final boolean isUnderloaded() {
        return loadManager.isUnderLoaded();
    }

    /**
     * test only
     */
    public final int getLoadManagerQueueSize() {
        return loadManager.getQueueSize();
    }

    /**
     * test only
     */
    public final void simulateEnqueueTask() {
        loadManager.notifyTaskEnqueued();
    }

    /**
     * test only
     */
    public final void simulateDequeueTask() {
        loadManager.notifyTaskDequeued();
    }

    private void enqueueTask(RequestRunnable requestRunnable) {
        loadManager.notifyTaskEnqueued();
        pendingRequests.add(requestRunnable);
    }

    private RequestRunnable dequeueTaskWithoutWait() {
        RequestRunnable requestRunnable = pendingRequests.poll();
        if (requestRunnable != null) {
            loadManager.notifyTaskDequeued();
        }
        return requestRunnable;
    }

    public static interface LoadListener {
        void overloaded();

        void underloaded();
    }

    private class RequestRunnable implements Runnable {

        private final Response response;
        private final String data;
        private final long receivedTimeStamp;

        private RequestRunnable(Response response, String data) {
            this.response = response;
            this.data = data;
            this.receivedTimeStamp = System.nanoTime();
        }

        @Override
        public void run() {
            execute();
            RequestRunnable requestRunnable;
            while ((requestRunnable = dequeueTaskWithoutWait()) != null) {
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
                if (response == null) {
                    stats.addAsyncStats(end - start, end - receivedTimeStamp);
                } else {
                    stats.addSyncStats(end - start, end - receivedTimeStamp);
                }
            }
        }
    }

    private class Stats {
        private final long lastReset;
        private int tasksReceived;
        private int numberOfSyncTasks;
        private long syncTasksTotalTime;
        private long syncTasksServiceTime;
        private int numberOfAsyncTasks;
        private int asyncTasksTotalTime;
        private int asyncTasksServiceTime;

        private Stats() {
            lastReset = System.nanoTime();
        }

        public synchronized final void taskReceived() {
            tasksReceived++;
        }

        public synchronized final void addSyncStats(long serviceTime, long totalTime) {
            numberOfSyncTasks++;
            if (serviceTime > 0) {
                syncTasksServiceTime += serviceTime;
                syncTasksTotalTime += totalTime > 0 ? totalTime : serviceTime;
            }
        }

        public synchronized final void addAsyncStats(long serviceTime, long totalTime) {
            numberOfAsyncTasks++;
            if (serviceTime > 0) {
                asyncTasksServiceTime += serviceTime;
                asyncTasksTotalTime += totalTime > 0 ? totalTime : serviceTime;
            }
        }

        public synchronized double getAverageSyncTasksTotalTime() {
            if (numberOfSyncTasks == 0) {
                return 0;
            }
            return convertToMillis(syncTasksTotalTime * 1.0 / numberOfSyncTasks);
        }

        public synchronized double getAverageSyncTasksServiceTime() {
            if (numberOfSyncTasks == 0) {
                return 0;
            }
            return convertToMillis(syncTasksServiceTime * 1.0 / numberOfSyncTasks);
        }

        public synchronized double getAverageAsyncTasksTotalTime() {
            if (numberOfAsyncTasks == 0) {
                return 0;
            }
            return convertToMillis(asyncTasksTotalTime * 1.0 / numberOfAsyncTasks);
        }

        public synchronized double getAverageAsyncTasksServiceTime() {
            if (numberOfAsyncTasks == 0) {
                return 0;
            }
            return convertToMillis(asyncTasksServiceTime * 1.0 / numberOfAsyncTasks);
        }

        public synchronized int getNumberOfSyncTasks() {
            return numberOfSyncTasks;
        }

        public synchronized int getNumberOfAsyncTasks() {
            return numberOfAsyncTasks;
        }

        public synchronized final double getNumberOfSyncTasksThroughput() {
            double timeSinceReset = convertToSeconds(timeSinceReset());
            if (timeSinceReset == 0) {
                return 0;
            }
            return numberOfSyncTasks / timeSinceReset;
        }

        public synchronized final double getNumberOfAsyncTasksThroughput() {
            double timeSinceReset = convertToSeconds(timeSinceReset());
            if (timeSinceReset == 0) {
                return 0;
            }
            return numberOfAsyncTasks / timeSinceReset;
        }

        public synchronized double getArrivalRate() {
            double timeSinceReset = convertToSeconds(timeSinceReset());
            if (timeSinceReset == 0) {
                return 0;
            }
            return tasksReceived * 1.0 / timeSinceReset;
        }

        public synchronized long getTimeSinceLastReset() {
            return TimeUnit.NANOSECONDS.toSeconds(timeSinceReset());
        }

        private double convertToSeconds(double value) {
            return value / 1E9;
        }

        private double convertToMillis(double value) {
            return value / 1E3;
        }

        private long timeSinceReset() {
            return System.nanoTime() - lastReset;
        }
    }

    private class LoadManager {
        private LoadListener loadListener;
        private int maxQueueSizeThreshold;
        private int minQueueSizeThreshold;
        private int queueSize;
        private boolean overLoaded;
        private boolean underLoaded;

        public LoadManager(int minQueueSizeThreshold, int maxQueueSizeThreshold) {
            this.maxQueueSizeThreshold = maxQueueSizeThreshold;
            this.minQueueSizeThreshold = minQueueSizeThreshold;
            this.queueSize = 0;
            this.overLoaded = false;
            this.underLoaded = true;
            checkThresholds();
        }

        public synchronized final void notifyTaskEnqueued() {
            if (++queueSize >= maxQueueSizeThreshold && !overLoaded) {
                overLoaded = true;
                underLoaded = false;
                loadListener.overloaded();
            }
        }

        public synchronized final void notifyTaskDequeued() {
            if (--queueSize <= minQueueSizeThreshold && !underLoaded) {
                overLoaded = false;
                underLoaded = true;
                loadListener.underloaded();
            }
        }

        public synchronized final void setLoadListener(LoadListener loadListener) {
            this.loadListener = loadListener;
        }

        public synchronized final int getMaxQueueSizeThreshold() {
            return maxQueueSizeThreshold;
        }

        public synchronized final void setMaxQueueSizeThreshold(int maxQueueSizeThreshold) {
            this.maxQueueSizeThreshold = maxQueueSizeThreshold;
            checkThresholds();
        }

        public synchronized final int getMinQueueSizeThreshold() {
            return minQueueSizeThreshold;
        }

        public synchronized final void setMinQueueSizeThreshold(int minQueueSizeThreshold) {
            this.minQueueSizeThreshold = minQueueSizeThreshold;
            checkThresholds();
        }

        public synchronized final int getQueueSize() {
            return queueSize;
        }

        public synchronized final boolean isOverLoaded() {
            return overLoaded;
        }

        public synchronized final boolean isUnderLoaded() {
            return underLoaded;
        }

        private void checkThresholds() {
            if (minQueueSizeThreshold <= 0) {
                minQueueSizeThreshold = 1;
            }
            if (maxQueueSizeThreshold <= 0) {
                maxQueueSizeThreshold = 1;
            }
            if (minQueueSizeThreshold >= maxQueueSizeThreshold) {
                maxQueueSizeThreshold = minQueueSizeThreshold + 1;
            }
        }
    }
}
