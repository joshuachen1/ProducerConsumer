import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Joshua Chen
 *         Date: Oct 27, 2018
 *         <p>
 *         5 Producers, 2 Consumers
 *         Each producer must produce 100 items
 */
public class PCLocks {
    public static void main(String[] args) throws InterruptedException {
        final int MAX_CAPACITY = 10;
        final int PRODUCE_NUM_ITEMS = 100;
        final BlockingQueue<Integer> buffer = new ArrayBlockingQueue<>(MAX_CAPACITY);
        final Lock lock = new ReentrantLock();

        Thread[] producerThread = new Thread[2];
        Thread[] consumerThread = new Thread[5];

        for (int i = 0; i < producerThread.length; i++) {
            producerThread[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int j = 0; j < PRODUCE_NUM_ITEMS; j++) {
                            lock.lock();
                            buffer.put(j);
                            System.out.println(Thread.currentThread().getName() + " produced Item-" + j);
                            lock.unlock();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        for (int i = 0; i < consumerThread.length; i++) {
            consumerThread[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (stillProducing(producerThread)) {
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

    private static boolean stillProducing(Thread[] producerThread) {
        for (int i = 0; i < producerThread.length; i++) {
            if (producerThread[i].isAlive()) {
                return true;
            }
        }
        return false;
    }
}