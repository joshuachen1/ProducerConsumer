import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Joshua Chen
 *         Date: Nov 05, 2018
 */
public class BufferActor extends AbstractActor {

    static public Props props(int MAX_CAPACITY, int produceAmount, int numProducers, int numConsumers) {
        return Props.create(BufferActor.class, () -> new BufferActor(MAX_CAPACITY, produceAmount, numProducers, numConsumers));
    }


    static class ItemsReady{}
    static class RequestItems{}
    static class Completed{}

    final BlockingQueue<Integer> buffer;
    final int produceAmount, numProducers, numConsumers;
    boolean producersFinished, consumersFinished;
    int counter = 0;

    public BufferActor(int MAX_CAPACITY, int produceAmount, int numProducers, int numConsumers) {
        this.buffer = new ArrayBlockingQueue<Integer>(MAX_CAPACITY);
        this.produceAmount = produceAmount;
        this.numProducers = numProducers;
        this.numConsumers = numConsumers;
        producersFinished = consumersFinished = false;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SieveActors.Start.class, b -> {

                    ActorRef[] producers = new ActorRef[numProducers];
                    for (int i = 0; i < numProducers; i++) {
                        producers[i] = getContext().actorOf(ProducerActor.props(buffer, produceAmount, self()));
                    }

                    ActorRef[] consumers = new ActorRef[numConsumers];
                    for (int i = 0; i < numConsumers; i++) {
                        consumers[i] = getContext().actorOf(ConsumerActor.props(buffer, self()));
                    }

                    // Start Producers
                    for (int i = 0; i < numProducers; i++) {
                        producers[i].tell(new RequestItems(), ActorRef.noSender());
                    }

                    while (!producersFinished || !buffer.isEmpty()) {
                        // Start Consumers
                        for (int i = 0; i < numConsumers; i++) {
                            consumers[i].tell(new ItemsReady(), ActorRef.noSender());
                        }
                    }
                })
                .match(ProducerActor.Finished.class, foo -> {
                    counter += 1;
                    if (counter == numProducers) {
                        producersFinished = true;

                    }
                })
                .build();
    }
}
