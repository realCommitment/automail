package strategies;

import java.util.LinkedList;
import java.util.Comparator;
import java.util.ListIterator;

import automail.CautiousRobot;
import automail.MailItem;
import automail.Robot;
import automail.Simulation;
import exceptions.BreakingFragileItemException;
import exceptions.ItemNotCompatibleException;
import exceptions.ItemTooHeavyException;

public class MailPool implements IMailPool {

	private class Item {
		int destination;
		MailItem mailItem;
		
		public Item(MailItem mailItem) {
			destination = mailItem.getDestFloor();
			this.mailItem = mailItem;
		}
	}
	
	public class ItemComparator implements Comparator<Item> {
		@Override
		public int compare(Item i1, Item i2) {
			int order = 0;
			if (i1.destination > i2.destination) {  // Further before closer
				order = 1;
			} else if (i1.destination < i2.destination) {
				order = -1;
			}
			return order;
		}
	}
	
	private LinkedList<Item> pool;
	private LinkedList<Robot> robots;

	public MailPool(int nrobots){
		// Start empty
		pool = new LinkedList<Item>();
		robots = new LinkedList<Robot>();
	}

	public void addToPool(MailItem mailItem) {
		Item item = new Item(mailItem);
		pool.add(item);
		pool.sort(new ItemComparator());
	}
	
	@Override
	public void step() 
			throws ItemTooHeavyException, BreakingFragileItemException, ItemNotCompatibleException {
		
		try{
			ListIterator<Robot> i = robots.listIterator();
			while (i.hasNext()) loadRobot(i);
		} catch (Exception e) { 
            throw e; 
        } 
	}
	
	private void loadRobot(ListIterator<Robot> i) 
			throws ItemTooHeavyException, BreakingFragileItemException, ItemNotCompatibleException {
		
		Robot robot = i.next();
		assert(robot.isEmpty());
		// System.out.printf("P: %3d%n", pool.size());
		ListIterator<Item> j = pool.listIterator();
		if (pool.size() > 0) {
			try {
			if (Simulation.isInCautionMode()) {
				loadCautiousRobot((CautiousRobot) robot, j);
			} else {
				loadNormalRobot(robot, j);
			}
			robot.dispatch(); // send the robot off if it has any items to deliver
			i.remove();       // remove from mailPool queue
			} catch (Exception e) { 
	            throw e; 
	        } 
		}
	}

	private void loadNormalRobot(Robot robot, ListIterator<Item> j)
			throws ItemTooHeavyException, BreakingFragileItemException {
		
		robot.addToHand(j.next().mailItem); // hand first as we want higher priority delivered first
		j.remove();
		if (pool.size() > 0) {
			robot.addToTube(j.next().mailItem);
			j.remove();
		}
	}
	
	// <edit> clean this up
	private void loadCautiousRobot(CautiousRobot robot, ListIterator<Item> j)
			throws ItemTooHeavyException, BreakingFragileItemException, ItemNotCompatibleException {
		
		MailItem itemToAdd = null;
		boolean loadedSpecialHand = false;
		boolean loadedNormalHand = false;
		boolean loadedTube = false;
		boolean done = false;
		
		while (!done) {
			// if has items to load
			if (pool.size() > 0) {
				itemToAdd = j.next().mailItem;
				// if fragile item
				if (itemToAdd.isFragile()) {
					// try to load into special hand
					if (!loadedSpecialHand) {
						robot.addToSpecialHand(itemToAdd);
						loadedSpecialHand = true;
						itemToAdd = null;
						j.remove();
					// already full
					} else {
						done = true;
					}
				// if normal item
				} else {
					// try to load into normal hand
					if (!loadedNormalHand) {
						robot.addToHand(itemToAdd);
						loadedNormalHand = true;
						itemToAdd = null;
						j.remove();
					} else {
						// try to load into tube
						if (!loadedTube) {
							robot.addToTube(itemToAdd);
							loadedTube = true;
							itemToAdd = null;
							j.remove();
						// already full
						} else {
							done = true;
						}		
					}
				}
			// out of items to load
			} else {
				done = true;
			}
		}	
	}
	
	

	@Override
	public void registerWaiting(Robot robot) { // assumes won't be there already
		robots.add(robot);
	}

}
