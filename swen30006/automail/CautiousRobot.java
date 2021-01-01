package automail;

import exceptions.BreakingFragileItemException;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import exceptions.ItemNotCompatibleException;
import strategies.IMailPool;

public class CautiousRobot extends Robot {
	
	private MailItem specialHand;
	private boolean isDeliveringFragile;
	private MailItem focusItem;
	
	public enum DeliveringState { WRAPPING, MOVING, EMPTYING_FLOOR, UNWRAPPING, POSTING }
	public DeliveringState deliveringState;
	
	private int fragileItemsDelivered = 0;
	private int fragileItemsDeliveredWeight = 0;
    private int normalItemsDelivered = 0;
    private int normalItemsDeliveredWeight = 0;
    private int wrapsAndUnwrapsTime = 0;
    
    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     *
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     */
	public CautiousRobot(IMailDelivery delivery, IMailPool mailPool) {
        super(delivery, mailPool);
        this.isDeliveringFragile = false;
        this.specialHand = null;
        deliveringState = DeliveringState.WRAPPING;
    }
	
	@Override
	protected void delivering() throws ExcessiveDeliveryException {
		
		if (isDeliveringFragile) {
			deliveringFragile();
		} else {
			super.delivering();
		}
	}
	
	protected void deliveringFragile() throws ExcessiveDeliveryException {
		
			switch(deliveringState) {
				case WRAPPING:
					// if delivering fragile then wrap unless already fully wrapped
					if (wrapping()) {
						// wrapping this tick
						break;
					} else {
						// if done wrapping then set to moving
						deliveringState = DeliveringState.MOVING;
					}

				case MOVING:
					// if moving then end the tick
					if (moving()) {
						break;
					} else {
						// if done moving then empty the floor
						deliveringState = DeliveringState.EMPTYING_FLOOR;
					}

				case EMPTYING_FLOOR:
					// if emptying floor then end the tick
					if (emptyingFloor()) {
						break;
					} else {
						// if done emptying floor then unwrap
						deliveringState = DeliveringState.UNWRAPPING;
					}

				case UNWRAPPING:
					// if unwrapping then end the tick
					if (unwrapping()) {
						break;
					} else {
						// if done unwrapping then post
						deliveringState = DeliveringState.POSTING;
					}

				case POSTING:

					postingFragileItem();

					// done posting so reset delivering state back to preparing
					deliveringState = DeliveringState.WRAPPING;

					// end the tick
					break;
			}
	}
	
	private boolean wrapping() {
		
		if (specialHand.isUnwrapped()) {
			// if not wrapped then half wrap (first tick)
			specialHand.halfWrap();
			wrapsAndUnwrapsTime += 1;
			return true;
		} else if (specialHand.isHalfWrapped()) {
			// if half wrapped then fully wrap (second tick)
			specialHand.fullyWrap();
			wrapsAndUnwrapsTime += 1;
			return true;
		} else {
			// if already wrapped then ready to move
			return false;
		}
	}
	
	private boolean moving() {
		if(currentFloor == destinationFloor){
		    // if already at the destination (for example if destination = mailroom) then done moving
			return false;
		} else {
			/** The robot is not at the destination yet, move towards it! */
		    moveTowards(destinationFloor);
		    
		    // if it has arrived at the destination by the end of this tick then book the floor (unless it is the mailroom)
		    if((currentFloor == destinationFloor) && (currentFloor != Building.MAILROOM_LOCATION)) {
			    Building.floors[currentFloor].addBooking();
		    }
		    
		    // end tick
		    return true;
		}
	}
	
	private boolean emptyingFloor() {
		if (Building.floors[currentFloor].getNumRobots() == 1) {
			return false;
		} else {
			// still emptying;
			return true;
		}
	}
	
	private boolean unwrapping() {
		if (specialHand.isWrapped()) {
			// if wrapped then unwrap 
			specialHand.unwrap();
			wrapsAndUnwrapsTime += 1;
			return true;
		} else {
			// if already unwrapped then proceed to posting
			return false;
		}
	}
	
	private void postingFragileItem() throws ExcessiveDeliveryException {
		fragileItemsDelivered += 1;
		fragileItemsDeliveredWeight += (specialHand.getWeight());
		delivery.deliver(specialHand);
		specialHand = null;
		isDeliveringFragile = false;
		deliveryCounter++;
		
		// remove the floor booking unless it is in the mailroom (in which case no booking was made)
		if (currentFloor != Building.MAILROOM_LOCATION) {
			Building.floors[currentFloor].removeBooking();
		}
	
		if(deliveryCounter > 3){  // Implies a simulation bug
			throw new ExcessiveDeliveryException();
		}
		
		if (hasNormalItem()) {
			setRoute();
			changeState(RobotState.DELIVERING);
		} else {
			changeState(RobotState.RETURNING);
		}
	}
	
	@Override
	protected void postingNormalItem() throws ExcessiveDeliveryException {
		/** Delivery complete, report this to the simulator! */
		
		normalItemsDelivered += 1;
		normalItemsDeliveredWeight += (deliveryItem.getWeight());
		delivery.deliver(deliveryItem);
		deliveryItem = null;
		deliveryCounter++;
		
		// <edit> changed the below error to only trigger after 3 deliveries
		if(deliveryCounter > 3){  // Implies a simulation bug
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
	
	@Override
    protected void moveTowards(int destination) {
        // get the next floor
		int nextFloor = getNextFloor(destination);
		
        // if the next floor allows entry then move and update floors accordingly
        if(Building.floors[nextFloor].isNotBooked()) {
            Building.floors[currentFloor].removeRobot();
        	Building.floors[nextFloor].addRobot();
        	currentFloor = nextFloor;
        }  else {
        	// robot waiting
        }
    }
	
	
    /**
     * Sets the route for the robot
     */
	@Override
    protected void setRoute() {
        
		// set the focus to the fragile item if the robot has one
		isDeliveringFragile = hasFragileItem();
		
		/** Set the destination floor */
		if(isDeliveringFragile) {
			focusItem = specialHand;
			destinationFloor = specialHand.getDestFloor();
		} else {
			focusItem = deliveryItem;
			destinationFloor = deliveryItem.getDestFloor();
		}
        
    }
	
	// <edit>
	@Override
	public boolean isEmpty() {
		return (deliveryItem == null && tube == null && specialHand == null);
	}
	
	@Override
    protected void changeState(RobotState nextState){
    	assert(!(deliveryItem == null && tube != null));
    	if (currentState != nextState) {
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), currentState, nextState);
    	}
    	currentState = nextState;
    	if(nextState == RobotState.DELIVERING){
            System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), getIdTube(), focusItem.toString());
    	}
    }
	
	@Override
    public String getIdTube() {
		int armId;
    	if (!(specialHand == null)) {
    		armId = 2;
    	} else if (!(deliveryItem == null) && !(tube == null)) {
    		armId = 1; 
    	} else {
    		armId = 0;
    	}
    	return String.format("%s(%1d)", id, armId);
    }
	
	private boolean hasFragileItem() {
		return(!(specialHand == null));
	}
	
	private boolean hasNormalItem() {
		return(!(deliveryItem == null));
	}
	
	private int getNextFloor(int destination) {
        if(currentFloor < destination){
            return(currentFloor + 1);
        } else if (currentFloor > destination) {
        	return(currentFloor - 1);
        } else {
        	return(currentFloor);
        }
	}
	
	public void addToSpecialHand(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException, ItemNotCompatibleException {
		assert(specialHand == null);
		if(!(mailItem.fragile)) throw new ItemNotCompatibleException();
		specialHand = mailItem;
		if (specialHand.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}

	public int getNormalItemsDelivered() {
		return normalItemsDelivered;
	}
	
	public int getNormalItemsDeliveredWeight() {
		return normalItemsDeliveredWeight;
	}

	public int getFragileItemsDelivered() {
		return fragileItemsDelivered;
	}

	public int getFragileItemsDeliveredWeight() {
		return fragileItemsDeliveredWeight;
	}
	
	public int getWrapsAndUnwrapsTime() {
		return wrapsAndUnwrapsTime;
	}
}
