package strategies;

import automail.CautiousRobot;
import automail.IMailDelivery;
import automail.Robot;
import automail.Simulation;
import automail.Statistics;

public class Automail {
	      
    public Robot[] robots;
    public IMailPool mailPool;
    private Statistics statistics;
    
    public Automail(IMailPool mailPool, IMailDelivery delivery, int numRobots) {
    	// Swap between simple provided strategies and your strategies here
    	    	
    	/** Initialize the MailPool */
    	
    	this.mailPool = mailPool;
    	
    	
    	if (Simulation.isInCautionMode()) {
    		
    		/** Initialize cautious robots */
    		createCautiousRobots(mailPool, delivery, numRobots);
    		
    		/** Initialise statistics */
    		if (Simulation.hasStatisticsEnabled()) {
    		statistics = new Statistics((CautiousRobot[]) robots);
    		}
    		
    	} else {
    		
    		/** Initialize robots */
    		createNormalRobots(mailPool, delivery, numRobots);
    	}
    	
    }
    
    // <edit>
    // prints statistics if enabled
    public void printStats() {
    	if (Simulation.isInCautionMode() && Simulation.hasStatisticsEnabled()) {
    		//print statistic
    		statistics.calculateAndPrintStats();
    	} else {
    		// do nothing
    	}
    }

    // creates cautious robots
	private void createCautiousRobots(IMailPool mailPool, IMailDelivery delivery, int numRobots) {
		robots = new CautiousRobot[numRobots];
		for (int i = 0; i < numRobots; i++) {
			robots[i] = new CautiousRobot(delivery, mailPool);
		}
	}
	
	// creates normal robots
	private void createNormalRobots(IMailPool mailPool, IMailDelivery delivery, int numRobots) {
		robots = new Robot[numRobots];
		for (int i = 0; i < numRobots; i++) {
			robots[i] = new Robot(delivery, mailPool);
		}
	}
}
