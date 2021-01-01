package automail;

public class Building {
	
	
    /** The number of floors in the building **/
    public static int FLOORS;
    
    /** Represents the ground floor location */
    public static final int LOWEST_FLOOR = 1;
    
    /** Represents the mailroom location */
    public static final int MAILROOM_LOCATION = 1;
    
    //<edit>
    
    // initialise floors
    public static Floor[] floors;
    
    public static Floor MAILROOM;
    
    public static void initialise(int numRobots) {
    	floors = new Floor[FLOORS + 1];
    	createFloors(numRobots);
    	MAILROOM = floors[1];
    	
    }
    
    public static void createFloors(int numRobots) {
    	// create each floor and add it to floors
    	for (int i = LOWEST_FLOOR; i <= FLOORS; i++) {
    		floors[i] = new Floor(i, numRobots);
    	}
    }
}
