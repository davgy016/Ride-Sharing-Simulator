package nuber.students;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A single Nuber region that operates independently of other regions, other than getting 
 * drivers from bookings from the central dispatch.
 * 
 * A region has a maxSimultaneousJobs setting that defines the maximum number of bookings 
 * that can be active with a driver at any time. For passengers booked that exceed that 
 * active count, the booking is accepted, but must wait until a position is available, and 
 * a driver is available.
 * 
 * Bookings do NOT have to be completed in FIFO order.
 * 
 * @author james
 *
 */
public class NuberRegion implements Runnable{
	
	private NuberDispatch dispatch;
	private String regionName;
	private final int maxSimultaneousJobs;
	private ExecutorService bookingExecuter;
	private final AtomicInteger activeBookings = new AtomicInteger(0);
	private final BlockingQueue<Passenger> pendingQueue = new LinkedBlockingDeque<Passenger>();
	
	

	
	/**
	 * Creates a new Nuber region
	 * 
	 * @param dispatch The central dispatch to use for obtaining drivers, and logging events
	 * @param regionName The regions name, unique for the dispatch instance
	 * @param maxSimultaneousJobs The maximum number of simultaneous bookings the region is allowed to process
	 */
	public NuberRegion(NuberDispatch dispatch, String regionName, int maxSimultaneousJobs)
	{
		this.dispatch = dispatch;
		this.regionName=regionName;
		this.maxSimultaneousJobs=maxSimultaneousJobs;
		this.bookingExecuter = Executors.newFixedThreadPool(maxSimultaneousJobs);
	}
	
	/**
	 * Creates a booking for given passenger, and adds the booking to the 
	 * collection of jobs to process. Once the region has a position available, and a driver is available, 
	 * the booking should commence automatically. 
	 * 
	 * If the region has been told to shutdown, this function should return null, and log a message to the 
	 * console that the booking was rejected.
	 * 
	 * @param waitingPassenger
	 * @return a Future that will provide the final BookingResult object from the completed booking
	 * @throws InterruptedException 
	 */
	public Future<BookingResult> bookPassenger(Passenger waitingPassenger) throws InterruptedException
	{		
		if(bookingExecuter.isShutdown()) {
			dispatch.logEvent(null, "Booking was rejected!");
			return null;
		}else {
			
		if(activeBookings.get()< maxSimultaneousJobs) {
			
		Booking booking= new Booking(dispatch, waitingPassenger);
			activeBookings.incrementAndGet();	
			
			return bookingExecuter.submit(new Callable<BookingResult>() {
				@Override
				public BookingResult call() throws Exception{
					
					activeBookings.decrementAndGet();
						return booking.call();
					
					
				}					
				
			});
		}
		pendingQueue.put(waitingPassenger);
		return null;
		
		}
	}	
	
	public String getRegionName() {	
		return regionName;
	}

	/**
	 * Called by dispatch to tell the region to complete its existing bookings and stop accepting any new bookings
	 */
	public void shutdown()
	{
		bookingExecuter.shutdown();		 		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!bookingExecuter.isShutdown()) {
			try {
				if(!pendingQueue.isEmpty()) {			
					bookPassenger(pendingQueue.take());
				}  
				Thread.sleep(50);
					
					
			}catch(InterruptedException e){
				Thread.currentThread().interrupt();
				dispatch.logEvent(null, "interrupted "+ regionName);
			}
		}
		System.out.println("Shutdown compl: "+regionName);
	}

		
	
		
}
