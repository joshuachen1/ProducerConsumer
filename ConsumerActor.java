import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Joshua Chen
 *         Date: Nov 05, 2018
 */
public class ConsumerActor extends AbstractActor {
    static public Props props(BlockingQueue<Integer> buffer, ActorRef bufferActor) {
        return Props.create(ConsumerActor.class, () -> new ConsumerActor(buffer, bufferActor));
    }

    static class Finished {
    }

    final BlockingQueue<Integer> buffer;
    final ActorRef bufferActor;


    public ConsumerActor(BlockingQueue<Integer> buffer, ActorRef bufferActor) {
        this.buffer = buffer;
        this.bufferActor = bufferActor;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BufferActor.ItemsReady.class, foo -> {
                    try {
                        while (!buffer.isEmpty()) {
                            Integer nextConsume = buffer.poll(1, TimeUnit.SECONDS);
                            if (nextConsume != null) {
                                System.out.println(Thread.currentThread().getName() + " consumed Item-" + nextConsume);
                                Thread.sleep(1000);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                .match(BufferActor.Completed.class, foo -> {
                    PCActors.timeOut = System.nanoTime();
                })
                .build();
    }
}
