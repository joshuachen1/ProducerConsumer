import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * @author Joshua Chen
 *         Date: Nov 05, 2018
 */
public class PCActors {

    static long timeOut;

    static class Start {}

    public static void main(String[] args) {
        final ActorSystem pcSystem = ActorSystem.create("ProducerConsumer");
        final int MAX_CAPACITY = 10;
        final int produceAmount = 100;
        final int numProducers = 5;
        final int numConsumers = 2;

        long timeIn = System.nanoTime();
        ActorRef buffer = pcSystem.actorOf(BufferActor.props(MAX_CAPACITY, produceAmount, numProducers, numConsumers));

        buffer.tell(new Start(), ActorRef.noSender());

        System.out.printf("Time for (%d) Producers and (%d) Consumers: %.2f (ms)", numProducers, numConsumers, (timeOut - timeIn) * 1e-6);
    }
}
