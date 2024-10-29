package nuber.students;

public class Driver extends Person {

	private Passenger currentPassenger;
	private int maxSleep;
	
	public Driver(String driverName, int maxSleep)
	{
		super(driverName, maxSleep);
		this.maxSleep = maxSleep;
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
		delay(maxSleep);
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
			delay(travelTime);
		}
		
	}
	
	// get random sleep time 	
	private void delay(int maxTime) {
        int actualDelay;
        try {
            actualDelay = randomWithRange(0, maxTime);
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
