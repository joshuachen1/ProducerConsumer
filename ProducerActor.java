import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.concurrent.BlockingQueue;

/**
 * @author Joshua Chen
 *         Date: Nov 05, 2018
 */
public class ProducerActor extends AbstractActor {
    static public Props props(BlockingQueue<Integer> buffer, int produceAmount, ActorRef bufferActor) {
        return Props.create(ProducerActor.class, () -> new ProducerActor(buffer, produceAmount, bufferActor));
    }

    static class Finished {}

    final BlockingQueue<Integer> buffer;
    final int NUM_TO_PRODUCE;
    final ActorRef bufferActor;

    public ProducerActor(BlockingQueue<Integer> buffer, int NUM_TO_PRODUCE, ActorRef bufferActor) {
        this.buffer = buffer;
        this.NUM_TO_PRODUCE = NUM_TO_PRODUCE;
        this.bufferActor = bufferActor;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BufferActor.RequestItems.class, p -> {
                    for (int i = 0; i < NUM_TO_PRODUCE; i++) {
                        buffer.put(i);
                        System.out.println(Thread.currentThread().getName() + " produced Item-" + i);
                    }
                    bufferActor.tell(new Finished(), ActorRef.noSender());
                })
                .build();
    }
}
