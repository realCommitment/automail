package automail;

public class Floor {
	private int number;
	
	private int numRobots;
	
	private boolean booked;
	 
	public Floor(int number, int numRobots) {
		this.number = number;
		if (number == Building.MAILROOM_LOCATION) {
			this.setNumRobots(numRobots);
		}
		this.booked = false; 
	}

	public int getNumber() {
		return number;
	}	

	public int getNumRobots() {
		return numRobots;
	}
	
	public void setNumRobots(int numRobots) {
		this.numRobots = numRobots;
		//System.out.printf("Set numRobots to %d on floor %d\n", numRobots, this.getNumber());
	}
	
	public void addRobot() {
		numRobots++;
		//System.out.printf("Added %s to floor %d\n", robotId, this.getNumber());
	}
	
	public void removeRobot() {
		numRobots--;
		//System.out.printf("Removed %s from floor %d\n", robotId, this.getNumber());
	}
	
	// books the floor
	public void addBooking() {
		booked = true;
	}
	
	// removes the booking
	public void removeBooking() {
		booked = false;
	}
	
	// returns if a robot is allowed to enter the floor
	public boolean isNotBooked() {
		return (!booked);
	}
}
