package nuber.students;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The core Dispatch class that instantiates and manages everything for Nuber
 * 	
 * @author james
 *
 */
public class NuberDispatch {

	/**
	 * The maximum number of idle drivers that can be awaiting a booking 
	 */
	private final int MAX_DRIVERS = 999;
	
	private boolean logEvents = false;
	
	private final  HashMap<String, NuberRegion> regions;
	
	//BlockingQueue is thread safe, it handles thread synchronization by itself 
	private BlockingQueue<Driver> driverQueue;
//	private BlockingQueue<Booking> pendingBookingQueue;
	private final AtomicInteger pendingBooking = new AtomicInteger(0);
	private final ExecutorService bookingExecutor;
	private boolean isShutdown=false;
	
	/**
	 * Creates a new dispatch objects and instantiates the required regions and any other objects required.
	 * It should be able to handle a variable number of regions based on the HashMap provided.
	 * 
	 * @param regionInfo Map of region names and the max simultaneous bookings they can handle
	 * @param logEvents Whether logEvent should print out events passed to it
	 */
	public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents)
	{
		this.logEvents =logEvents;
		this.regions= new HashMap<>();
		this.driverQueue = new LinkedBlockingQueue<Driver>(MAX_DRIVERS);
//		this.pendingBookingQueue = new LinkedBlockingQueue<Booking>();
		this.bookingExecutor = Executors.newFixedThreadPool(5);	
		for(String regionName: regionInfo.keySet()) {
			regions.put(regionName, new NuberRegion(this, regionName, regionInfo.get(regionName)));
		}
		
	}
	
	/**
	 * Adds drivers to a queue of idle driver.
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @param The driver to add to the queue.
	 * @return Returns true if driver was added to the queue
	 * @throws InterruptedException 
	 */
	public boolean addDriver(Driver newDriver) throws InterruptedException
	{
		return driverQueue.offer(newDriver);
		
	}
		
	/**
	 * Gets a driver from the front of the queue
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @return A driver that has been removed from the queue
	 * @throws InterruptedException 
	 */
	public  Driver getDriver() throws InterruptedException
	{
		Driver driver =  driverQueue.take();
		
			if(pendingBooking.get()>0 && driver!=null) {
				
				pendingBooking.decrementAndGet();
			}
			return driver;
		
	}

	/**
	 * Prints out the string
	 * 	    booking + ": " + message
	 * to the standard output only if the logEvents variable passed into the constructor was true
	 * 
	 * @param booking The booking that's responsible for the event occurring
	 * @param message The message to show
	 */
	public void logEvent(Booking booking, String message) {
		
		if (!logEvents) return;
		
		System.out.println(booking + ": " + message);
		
	}

	/**
	 * Books a given passenger into a given Nuber region.
	 * 
	 * Once a passenger is booked, the getBookingsAwaitingDriver() should be returning one higher.
	 * 
	 * If the region has been asked to shutdown, the booking should be rejected, and null returned.
	 * 
	 * @param passenger The passenger to book
	 * @param region The region to book them into
	 * @return returns a Future<BookingResult> object
	 * @throws InterruptedException 
	 */
	public Future<BookingResult> bookPassenger(Passenger passenger, String region) throws InterruptedException {
		NuberRegion nuberRegion = regions.get(region);
		if(region !=null && isShutdown==false) {
			pendingBooking.incrementAndGet();
			return nuberRegion.bookPassenger(passenger);
		}
		return null;		
	}
			
		
		

	/**
	 * Gets the number of non-completed bookings that are awaiting a driver from dispatch
	 * 
	 * Once a driver is given to a booking, the value in this counter should be reduced by one
	 * 
	 * @return Number of bookings awaiting driver, across ALL regions
	 */
	public int getBookingsAwaitingDriver()
	{
		return pendingBooking.get();
	}
	
	/**
	 * Tells all regions to finish existing bookings already allocated, and stop accepting new bookings
	 */
	public void shutdown() {
		isShutdown = true;
		for(NuberRegion region : regions.values()) {
			region.shutdown();
			logEvent(null,"Region Shutdown: " + region.getRegionName());
		}
		
	}

}
