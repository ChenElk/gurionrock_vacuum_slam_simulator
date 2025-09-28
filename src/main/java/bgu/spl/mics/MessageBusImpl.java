package bgu.spl.mics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
	private final ConcurrentHashMap<MicroService, BlockingQueue<Message>> serviceQueues;
	private final ConcurrentHashMap<Class<? extends Broadcast>, BlockingQueue<MicroService>> broadcastSubscribers;
	private final ConcurrentHashMap<Class<? extends Event<?>>, BlockingQueue<MicroService>> eventSubscribers;
	private final ConcurrentHashMap<Event<?>, Future<?>> eventFutures;
	//singleton instance
	private static MessageBusImpl instance = null;

	private MessageBusImpl() {
		serviceQueues = new ConcurrentHashMap<>();
		broadcastSubscribers= new ConcurrentHashMap<>();
		eventSubscribers= new ConcurrentHashMap<>();
		eventFutures =new ConcurrentHashMap<>();
	}

	public static MessageBusImpl getInstance() {
		if (instance == null) {
			synchronized (MessageBusImpl.class) {
				if (instance == null) {
					instance = new MessageBusImpl();
				}
			}
		}
		return instance;
	}


	/**
	 * PRE:
	 * - The event type `type` and MicroService `m` must not be null.
	 * POST:
	 * - The MicroService `m` is added to the list of subscribers for the specified event type.
	 * - If the event type has no existing subscriber list, a new list is created.
	 *
	 * @param type The class of the event type.
	 * @param m The subscribing MicroService.
	 */
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		synchronized (eventSubscribers){
			// ensure the list exists
			eventSubscribers.putIfAbsent(type, new LinkedBlockingQueue<>());
			// add the microService to the queue
			eventSubscribers.get(type).offer(m);
		}
	}

	/**
	 * PRE:
	 * - The broadcast type `type` and MicroService `m` must not be null.
	 * POST:
	 * - The MicroService `m` is added to the list of subscribers for the specified broadcast type.
	 * - If the broadcast type has no existing subscriber list, a new list is created.
	 *
	 * @param type The class of the broadcast type.
	 * @param m The subscribing MicroService.
	 */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		//ensure the list exists
		broadcastSubscribers.putIfAbsent(type, new LinkedBlockingQueue<>());
		// add the microService to the queue
		broadcastSubscribers.get(type).offer(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		synchronized (eventFutures) {
			//look for the appropriate future
			Future<T> future = (Future<T>) eventFutures.get(e);

			//using the resolve method of future
			if (future != null) {
				future.resolve(result);
			}
		}

	}

	/**
	 * PRE: The broadcast `b` must not be null.
	 * POST:
	 * - The broadcast is added to the message queues of all subscribed MicroServices.
	 * - If no MicroServices are subscribed, no action is taken.
	 *
	 * @param b The broadcast message to send.
	 */
	@Override
	public void sendBroadcast(Broadcast b) {
		//get the list of MicroServices subscribed to this type
		BlockingQueue<MicroService> subscriberQueue = broadcastSubscribers.get(b.getClass());

		if (subscriberQueue != null) {
			for (MicroService m : subscriberQueue) {
				BlockingQueue<Message> queue = serviceQueues.get(m);
				if (queue != null) {
					try {
						queue.put(b);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}


	/**
	 * PRE:
	 * - The event `e` must not be null.
	 * - At least one MicroService must be subscribed to handle the type of event `e`.
	 * POST:
	 * - The event is added to the message queue of a subscribed MicroService in a round-robin manner.
	 * - A Future object is returned, allowing the sender to track the result of the event.
	 * - Returns null if no MicroServices are subscribed to the event.
	 *
	 * @param e The event to send.
	 * @param <T> The type of the result.
	 * @return A Future representing the result of the event or null if no subscribers exist.
	 */
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		//get the subscribers to this event
		BlockingQueue<MicroService> subscriberQueue = eventSubscribers.get(e.getClass());
		//there are no subscruibers to this event
		if (subscriberQueue == null || subscriberQueue.isEmpty()) {
			return null;
		}

		synchronized (subscriberQueue){
			// Round-robin: take the first microService and put it back at the end
			MicroService currService = subscriberQueue.poll();
			if (currService != null) {
				subscriberQueue.offer(currService); //add back to the end of the queue

				BlockingQueue<Message> queue = serviceQueues.get(currService);
				if(queue != null){
					try {
						//add the event to the microService's message queue
						queue.put(e);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
						return null;
					}

					Future<T> future = new Future<>();
					synchronized (eventFutures) {
						eventFutures.put(e, future);
					}
					return future;
				}
            }

		}
		return null;
	}

	/**
	 * PRE: The MicroService `m` must not be null and should not already be registered.
	 * POST: A new message queue is created for the given MicroService if it is not already registered.
	 *
	 * @param m The MicroService to register.
	 */
	@Override
	public void register(MicroService m) {
		serviceQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
	}

	/**
	 * PRE: The MicroService `m` must be registered in the system.
	 * POST:
	 * - The MicroService is removed from all event and broadcast subscribers.
	 * - The message queue for the MicroService is cleared and removed.
	 *
	 * @param m The MicroService to unregister.
	 */
	@Override
	public void unregister(MicroService m) {

		//remove the MicroService from all broadcast subscribers
		synchronized (broadcastSubscribers) {
			broadcastSubscribers.values().forEach(subscriberQueue -> subscriberQueue.remove(m));
		}

		//remove the MicroService from all event subscribers
		synchronized (eventSubscribers) {
			eventSubscribers.values().forEach(subscriberQueue -> subscriberQueue.remove(m));
		}

		BlockingQueue<Message> queue = serviceQueues.remove(m);
		if (queue != null) {
			queue.clear();
		}

	}

	/**
	 * PRE:
	 * - The MicroService `m` must be registered and have a valid message queue.
	 * - The message queue for the MicroService must not be null.
	 * POST:
	 * - Returns the next message in the queue, blocking if no messages are available.
	 * - Throws an IllegalStateException if the MicroService is not registered.
	 *
	 * @param m The MicroService awaiting a message.
	 * @return The next message in the queue.
	 * @throws InterruptedException if the thread is interrupted while waiting.
	 */
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		BlockingQueue<Message> messages= serviceQueues.get(m);

		//check if the MicroService is registered
		if (messages == null) {
			throw new IllegalStateException("MicroService " + m.getClass().getSimpleName() + " is not registered");
		}

		//blocks until there is a message
		return messages.take();
	}

	//method for testing
	public void clearInstance() {
		serviceQueues.clear(); // Clear all service queues
		broadcastSubscribers.clear(); // Clear all broadcast subscribers
		eventSubscribers.clear(); // Clear all event subscribers
		eventFutures.clear(); // Clear all pending event futures
	}

}
