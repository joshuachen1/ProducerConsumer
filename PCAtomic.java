import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Joshua Chen
 *         Date: Nov 05, 2018
 *         <p>
 *         Optimistic Collisions
 */
public class PCAtomic {
    public static void main(String[] args) throws InterruptedException {
        final int MAX_CAPACITY = 10;
        final int PRODUCE_NUM_ITEMS = 100;
        final BlockingQueue<Integer> buffer = new ArrayBlockingQueue<>(MAX_CAPACITY);

        final AtomicInteger liveProducers = new AtomicInteger();

        Thread[] producerThread = new Thread[5];
        Thread[] consumerThread = new Thread[2];

        for (int i = 0; i < producerThread.length; i++) {
            producerThread[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    // Track the number producing threads
                    liveProducers.incrementAndGet();

                    try {
                        for (int j = 0; j < PRODUCE_NUM_ITEMS; j++) {
                            buffer.put(j);
                            System.out.println(Thread.currentThread().getName() + " produced Item-" + j);
                        }
                    } catch (InterruptedException e) {

                    } finally {
                        // No longer track a thread that is finished.
                        liveProducers.decrementAndGet();
                    }
                }
            });
        }

        for (int i = 0; i < consumerThread.length; i++) {
            consumerThread[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (liveProducers.get() != 0 || buffer.peek() != null)){
                            Integer nextConsume = buffer.poll(1, TimeUnit.SECONDS);
                            if (nextConsume != null) {
                                System.out.println(Thread.currentThread().getName() + " consumed Item-" + nextConsume);
                                Thread.sleep(1000);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        long timeIn = System.nanoTime();

        for (int i = 0; i < producerThread.length; i++) {
            producerThread[i].start();
        }

        for (int i = 0; i < consumerThread.length; i++) {
            consumerThread[i].start();
        }

        for (int i = 0; i < producerThread.length; i++) {
            producerThread[i].join();
        }

        for (int i = 0; i < consumerThread.length; i++) {
            consumerThread[i].join();
        }

        long timeOut = System.nanoTime() - timeIn;
        System.out.printf("Time for (%d) Producers and (%d) Consumers: %.2f (ms)", producerThread.length, consumerThread.length, timeOut * 1e-6);


    }
}
