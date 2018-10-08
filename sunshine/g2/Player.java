package sunshine.g2;
import java.util.ArrayList;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.*;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;



public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    private Point dest;
    List<Point> bales;
    private int counter;
    List<Point> balesList;
    Point balesListCenter;

    private Map<Integer, List<Command>> commandCenter;
    private Map<Point, List<Point>> farPoints;
    private int numTractors;
    private double edgeLength;
    private double time;

    public Player() {
        rand = new Random(seed);
        commandCenter = new HashMap<Integer, List<Command>>();
        farPoints = new HashMap<Point, List<Point>>();
        numTractors = 0;
        edgeLength = 0.0;
        time = 0.0;
    }
    
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = new ArrayList<Point>(bales);
        Collections.sort(this.bales, 
            new Comparator(){
                @Override
                public int compare(Object o1, Object o2) {
                    Point p1 = (Point) o1;
                    Point p2 = (Point) o2;
                    return - (int)Math.signum(p1.x*p1.x + p1.y*p1.y - p2.x*p2.x - p2.y*p2.y);
               }
            } 
        );

        numTractors = n;
        edgeLength = m;
        time = t;
        for (int i = 0; i < numTractors; i++) {
            List<Command> commands = new ArrayList<Command>();
            commands.add(new Command(CommandType.DETATCH));
            commandCenter.put(i, commands);
        }

        // for (Point bale: this.bales) {
        //     System.out.println("&&&&&&&&&&&&dis: " + calcEucDistance(new Point(0.0, 0.0), bale));
        // }

        while (this.bales.size() != 0) {
            Point p = this.bales.remove(0);
            double disToOrigin = calcEucDistance(new Point(0.0, 0.0), p);
            if (disToOrigin > 175) {
                List<Point> ten = getNearestTenBales(p);
                farPoints.put(p, ten);
            }
            else {
                this.bales.add(0, p);
                break;
            }
        }

        // System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&map size: " + farPoints.size());
        while (this.bales.size() != 0 || farPoints.size() != 0) {
            for (int i = 0; i < numTractors; i++) {
                if (this.bales.size() != 0 || farPoints.size() != 0) {
                    oneTrip(i);
                }
            }
        }

     //    balesList = new ArrayList<Point>();
     //    counter = 0;
    	// balesListCenter = this.bales.get(getFurthestBale());
    	// buildList();
    }

    // when the tractor is back to the original
    private void oneTrip(int tractorID) {
        if (farPoints.size() != 0) {
            Map.Entry<Point, List<Point>> entry = farPoints.entrySet().iterator().next();
            farPoints.remove(entry.getKey());
            collectWithTrailer(tractorID, entry.getKey(), entry.getValue());
        }
        else {
            Point p = bales.remove(0);
            collectWithoutTrailer(tractorID, p);
        }

        // System.out.println("***************************************tractor ID is: " + tractorID + "**********************************************");
    }

    private void collectWithoutTrailer(int tractorID, Point p) {
        List<Command> commands = commandCenter.get(tractorID);
        commands.add(Command.createMoveCommand(p));
        commands.add(new Command(CommandType.LOAD));
        commands.add(Command.createMoveCommand(new Point(0.0, 0.0)));
        commands.add(new Command(CommandType.UNLOAD));
    }

    private void collectWithTrailer(int tractorID, Point p, List<Point> ten) {
        List<Command> commands = commandCenter.get(tractorID);

        // forward trip
        commands.add(new Command(CommandType.ATTACH));
        commands.add(Command.createMoveCommand(p));
        commands.add(new Command(CommandType.DETATCH));
        for (Point bale : ten) {
            commands.add(Command.createMoveCommand(bale));
            commands.add(new Command(CommandType.LOAD));
            commands.add(Command.createMoveCommand(p));
            commands.add(new Command(CommandType.STACK));
        }
        commands.add(new Command(CommandType.LOAD));
        commands.add(new Command(CommandType.ATTACH));

        //backward trip
        commands.add(Command.createMoveCommand(new Point(0.0, 0.0)));
        commands.add(new Command(CommandType.DETATCH));
        commands.add(new Command(CommandType.UNLOAD));
        for (int i = 0; i < ten.size(); i++) {
            commands.add(new Command(CommandType.UNSTACK));
            commands.add(new Command(CommandType.UNLOAD));
        }

        // possible callback function
    }

	public double calcEucDistance(Point origin, Point dest1)
    {
        Point result = new Point(0.0, 0.0);
        result.y = Math.abs(origin.y - dest1.y);
        result.x = Math.abs(origin.x - dest1.x);
        double distance = Math.sqrt((result.y)*(result.y) +(result.x)*(result.x));

        return distance;
    }

    public List<Point> getNearestTenBales(Point p)
    {
        // System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&bale size: " + bales.size());
        PriorityQueue<Point> sortedToPoint = new PriorityQueue<Point>(bales.size(), 
            new Comparator(){
                public int compare(Object o1, Object o2) {
                    Point p1 = (Point) o1;
                    Point p2 = (Point) o2;
                    return (int) Math.signum((p1.x - p.x) * (p1.x - p.x) + (p1.y - p.y) * (p1.y - p.y) - (p2.x - p.x) * (p2.x - p.x) - (p2.y - p.y) * (p2.y - p.y));
               }
            }
        );

        for (Point bale : bales) {
            sortedToPoint.add(bale);
        }

        List<Point> result = new ArrayList<Point>();
        int numBales = 10;
        if (sortedToPoint.size() < numBales) {
            numBales = sortedToPoint.size();
        }
        for (int i = 0; i < numBales; i++) {
            Point point = sortedToPoint.poll();
            bales.remove(point);
            result.add(point);
        }

        return result;
    }

    public int getNearestBale()
    {
    	Point target = balesListCenter;
        double minDist = 9999999999.99;
        int minIndex = 0;

        for (int i=0; i<bales.size(); i++) {
            Point bale = bales.get(i);

            Double distance = calcEucDistance(target, bale);
            if (distance < minDist)
            {
                minDist = distance;
                minIndex = i;
            }
        }

        return minIndex;
    }

    public int getFurthestBale()
    {
        Point home = new Point(0.0, 0.0);
        double maxDist = 0.0;
        int maxIndex = 0;

        for (int i=0; i<bales.size(); i++) {
           Point bale = bales.get(i);

           Double distance = calcEucDistance(home, bale);
           if (distance > maxDist)
           {
                maxDist = distance;
                maxIndex = i;
           }
        }

        return maxIndex;
    }

    public Command getCommand(Tractor tractor)
    {	
        int tractorID = tractor.getId();
        List<Command> commands = commandCenter.get(tractorID);

        if (commands.size() == 0) {
            return new Command(CommandType.UNLOAD);
        }

        return commands.remove(0);
  //       if(tractor.getLocation().equals(new Point(0.0,0.0)) && tractor.getAttachedTrailer()!=null && balesList.size()!=0){
		// 	return Command.createMoveCommand(balesListCenter);
  //       }
  //       else if(tractor.getLocation().equals(balesListCenter) && tractor.getAttachedTrailer()!=null && balesList.size()!=0){
  //       	return new Command(CommandType.DETATCH);
  //       }
  //       else if(tractor.getLocation().equals(balesListCenter) && tractor.getAttachedTrailer()==null && balesList.size()!=0 && !tractor.getHasBale()){
		// 	dest = balesList.remove(rand.nextInt(balesList.size()));
  //           return Command.createMoveCommand(dest);
  //       }
  //       else if (tractor.getHasBale()&&tractor.getLocation().equals(dest)){
  //       	return Command.createMoveCommand(balesListCenter);
  //       }
  //       else if (tractor.getHasBale()==false&&tractor.getLocation().equals(dest)){
  //       	return new Command(CommandType.LOAD);
  //       }
  //       else if (tractor.getHasBale()&&tractor.getLocation().equals(balesListCenter)&&balesList.size()!=0){
  //       	counter+=1;
  //       	return new Command(CommandType.STACK);
  //       }
  //       else if (tractor.getHasBale()&&tractor.getLocation().equals(balesListCenter)&&balesList.size()==0 && tractor.getAttachedTrailer()==null){
  //       	return new Command(CommandType.ATTACH);
  //       }
  //       else if (tractor.getHasBale()&&tractor.getLocation().equals(balesListCenter)&&balesList.size()==0 && tractor.getAttachedTrailer()!=null){
		//    return Command.createMoveCommand(new Point(0.0,0.0));
		// }
  //       else if(tractor.getLocation().equals(new Point(0.0,0.0)) && tractor.getAttachedTrailer()!=null && balesList.size()==0 && tractor.getHasBale()){
		// 	return new Command(CommandType.UNLOAD);
  //       }
  //       else if(tractor.getLocation().equals(new Point(0.0,0.0)) && tractor.getAttachedTrailer()!=null && balesList.size()==0 && !tractor.getHasBale()){
  //       	return new Command(CommandType.DETATCH);
  //       }
  //       else if(tractor.getLocation().equals(new Point(0.0,0.0)) && tractor.getAttachedTrailer()==null && balesList.size()==0 && !tractor.getHasBale() && counter!=0){
  //       	counter-=1;
  //       	return new Command(CommandType.UNSTACK);
  //       }
  //       else if(tractor.getLocation().equals(new Point(0.0,0.0)) && tractor.getAttachedTrailer()==null && balesList.size()==0 && tractor.getHasBale()){
  //       	return new Command(CommandType.UNLOAD);
  //       }
  //       else if(tractor.getLocation().equals(new Point(0.0,0.0)) && tractor.getAttachedTrailer()==null && balesList.size()==0 && !tractor.getHasBale() && counter==0){
  //       	balesListCenter = bales.get(getFurthestBale());
  //       	buildList();
  //       	return new Command(CommandType.ATTACH);
  //       }
  //       else{
  //       	return null;
  //       }
    }

    private void buildList() {
    	for (int i = 0; i < 11; i++) {
    		int index = getNearestBale();
    		balesList.add(bales.get(index));
    		bales.remove(index);
    	}
    }



    
}
