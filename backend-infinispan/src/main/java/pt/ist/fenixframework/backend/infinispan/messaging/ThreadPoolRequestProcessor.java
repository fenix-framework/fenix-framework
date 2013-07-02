package pt.ist.fenixframework.backend.infinispan.messaging;

import org.jgroups.blocks.Response;
import pt.ist.fenixframework.FenixFramework;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
public class ThreadPoolRequestProcessor {

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
    private final ExecutorService executorService;
    private final Queue<RequestRunnable> pendingRequests;

    public ThreadPoolRequestProcessor(int coreThreads, int maxThreads, int keepAliveTime, final String applicationName) {
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

    public void execute(String data, Response response) {
        executorService.execute(new RequestRunnable(response, data));
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
            try {
                response.send(FenixFramework.handleRequest(data), false);
            } catch (Throwable throwable) {
                response.send(throwable, true);
            }
        }
    }
}
