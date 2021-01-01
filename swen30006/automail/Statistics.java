package automail;

public class Statistics {
    public Statistics(CautiousRobot[] robots){
        this.robots = robots;
    }
    private CautiousRobot[] robots;
    private int fragileItemsDeliveredTotal = 0;
    private int fragileItemsDeliveredWeightTotal = 0;
    private int normalItemsDeliveredTotal = 0;
    private int normalItemsDeliveredWeightTotal = 0;
    private int wrapsAndUnwrapsTimeTotal = 0;
   
    


    private void printTotalStats(){
        System.out.println("Number of fragile items delivered: " + fragileItemsDeliveredTotal);
        System.out.println("Weight of fragile items delivered: " + fragileItemsDeliveredWeightTotal);
        System.out.println("Number of normal items delivered: " + normalItemsDeliveredTotal);
        System.out.println("Weight of normal items delivered: " + normalItemsDeliveredWeightTotal);
        System.out.println("Total time for wrapping and unwrapping: " + wrapsAndUnwrapsTimeTotal);
    }

    public void calculateAndPrintStats (){
        calculateTotalStats();
        printTotalStats();
    }

    private void calculateTotalStats(){
        if (robots.length>0){
            for (CautiousRobot robot: robots){
            	fragileItemsDeliveredTotal += robot.getFragileItemsDelivered();
                fragileItemsDeliveredWeightTotal += robot.getFragileItemsDeliveredWeight();
                normalItemsDeliveredTotal += robot.getNormalItemsDelivered();
                normalItemsDeliveredWeightTotal += robot.getNormalItemsDeliveredWeight();
                wrapsAndUnwrapsTimeTotal += robot.getWrapsAndUnwrapsTime();
            }
        }
    }
}
