package nuber;

public class Driver extends Person {

	private Passenger currentPassenger;
	
	
	public Driver(String driverName, int maxSleep)
	{
		super(driverName, maxSleep);
		
	}
	
	/**
	 * Stores the provided passenger as the driver's current passenger and then
	 * sleeps the thread for between 0-maxDelay milliseconds.
	 * 
	 * @param newPassenger Passenger to collect
	 * @throws InterruptedException
	 */
	public void pickUpPassenger(Passenger newPassenger) throws InterruptedException {
	
		currentPassenger = newPassenger;
		delay();
	}

	/**
	 * Sleeps the thread for the amount of time returned by the current 
	 * passenger's getTravelTime() function
	 * 
	 * @throws InterruptedException
	 */
	public void driveToDestination() throws InterruptedException{
		if(currentPassenger!= null) {
			int travelTime = currentPassenger.getTravelTime();
			Thread.sleep(travelTime);
			currentPassenger=null;
		}
		
	}
	
	// get random sleep time 	
	private void delay() {
        int actualDelay;
        try {
            actualDelay = randomWithRange(0, maxSleep);
            Thread.sleep(actualDelay);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private int randomWithRange(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }
	
}
