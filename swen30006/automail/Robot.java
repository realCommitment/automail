package automail;

import exceptions.BreakingFragileItemException;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import strategies.IMailPool;
import java.util.Map;
import java.util.TreeMap;

/**
 * The robot delivers mail!
 */
public class Robot {
	
    static public final int INDIVIDUAL_MAX_WEIGHT = 2000;

    IMailDelivery delivery;
    protected final String id;
    /** Possible states the robot can be in */
    public enum RobotState { DELIVERING, WAITING, RETURNING }
    public RobotState currentState;
    protected int currentFloor; // <edit> changed variables to protected
    protected int destinationFloor;
    protected IMailPool mailPool;
    protected boolean receivedDispatch;
    
    protected MailItem deliveryItem = null;
    protected MailItem tube = null;
    
    protected int deliveryCounter;

    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     * @param behaviour governs selection of mail items for delivery and behaviour on priority arrivals
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     */
    public Robot(IMailDelivery delivery, IMailPool mailPool){
    	id = "R" + hashCode();
        // current_state = RobotState.WAITING;
    	currentState = RobotState.RETURNING;
        currentFloor = Building.MAILROOM_LOCATION;
        this.delivery = delivery;
        this.mailPool = mailPool;
        this.receivedDispatch = false;
        this.deliveryCounter = 0;
    }
    
    public void dispatch() {
    	receivedDispatch = true;
    }
    
    
    // <edit> added methods
    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    public void step() throws ExcessiveDeliveryException {    	
    	switch(currentState) {
    		/** This state is triggered when the robot is returning to the mailroom after a delivery */
    		case RETURNING:
    			if (returning()) {
                	// if it is returning then break
    				break;
                }
    			// otherwise no break and proceded to waiting
    			
    		case WAITING:
    			waiting();
                break;
    		case DELIVERING:
    			delivering();
                break;
    	}
    }
    
    // <edit>
    // returns true if returning, false if waiting
	protected boolean returning() {
		/** If its current position is at the mailroom, then the robot should change state */
		if(currentFloor == Building.MAILROOM_LOCATION){
			if (tube != null) {
				mailPool.addToPool(tube);
			    System.out.printf("T: %3d >  +addToPool [%s]%n", Clock.Time(), tube.toString());
			    tube = null;
			}
			/** Tell the sorter the robot is ready */
			mailPool.registerWaiting(this);
			changeState(RobotState.WAITING);
			return false;
		} else {
			/** If the robot is not at the mailroom floor yet, then move towards it! */
		    moveTowards(Building.MAILROOM_LOCATION);
		    return true;
		}
	}
	
	// <edit>
	// waiting for mail
	protected void waiting() {
		/** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
		if(!isEmpty() && receivedDispatch){
			receivedDispatch = false;
			deliveryCounter = 0; // reset delivery counter
			setRoute();
			changeState(RobotState.DELIVERING);
		}
	}
	
	// <edit>
	// delivering mail
	protected void delivering() throws ExcessiveDeliveryException {
		if(currentFloor == destinationFloor){ // If already here drop off either way
			postingNormalItem();
		} else {
			/** The robot is not at the destination yet, move towards it! */
		    moveTowards(destinationFloor);
		}
	}

	protected void postingNormalItem() throws ExcessiveDeliveryException {
		/** Delivery complete, report this to the simulator! */
		delivery.deliver(deliveryItem);
		deliveryItem = null;
		deliveryCounter++;
		if(deliveryCounter > 2){  // Implies a simulation bug
			throw new ExcessiveDeliveryException();
		}
		/** Check if want to return, i.e. if there is no item in the tube*/
		if(tube == null){
			changeState(RobotState.RETURNING);
		}
		else{
		    /** If there is another item, set the robot's route to the location to deliver the item */
		    deliveryItem = tube;
		    tube = null;
		    setRoute();
		    changeState(RobotState.DELIVERING);
		}
	}
	
    /**
     * Sets the route for the robot
     */
    protected void setRoute() {
        /** Set the destination floor */
        destinationFloor = deliveryItem.getDestFloor();
    }
    
    // <edit> floors now get updated by movement
    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    protected void moveTowards(int destination) {
        if(currentFloor < destination){
            Building.floors[currentFloor].removeRobot();
        	currentFloor++;
        	Building.floors[currentFloor].addRobot();
        } else {
        	Building.floors[currentFloor].removeRobot();
            currentFloor--;
            Building.floors[currentFloor].addRobot();
        }
    }
    
    protected String getIdTube() {
    	return String.format("%s(%1d)", id, (tube == null ? 0 : 1));
    }
    
    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    protected void changeState(RobotState nextState){
    	assert(!(deliveryItem == null && tube != null));
    	if (currentState != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), currentState, nextState);
    	}
    	currentState = nextState;
    	if(nextState == RobotState.DELIVERING){
            System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), getIdTube(), deliveryItem.toString());
    	}
    }

	public MailItem getTube() {
		return tube;
	}
    
	static private int count = 0;
	static private Map<Integer, Integer> hashMap = new TreeMap<Integer, Integer>();

	@Override
	public int hashCode() {
		Integer hash0 = super.hashCode();
		Integer hash = hashMap.get(hash0);
		if (hash == null) { hash = count++; hashMap.put(hash0, hash); }
		return hash;
	}

	public boolean isEmpty() {
		return (deliveryItem == null && tube == null);
	}

	public void addToHand(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException {
		assert(deliveryItem == null);
		if(mailItem.fragile) throw new BreakingFragileItemException();
		deliveryItem = mailItem;
		if (deliveryItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}

	public void addToTube(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException {
		assert(tube == null);
		if(mailItem.fragile) throw new BreakingFragileItemException();
		tube = mailItem;
		if (tube.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}

}
