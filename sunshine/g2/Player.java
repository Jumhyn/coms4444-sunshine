package sunshine.g2;
import java.util.ArrayList;
import java.lang.*;
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
    List<List<Point>> bucketAnchors;
    List<List<Cluster>> buckets;
    private List<Point> clusterAnchors;
    private Map<Integer, List<Command>> commandCenter;
    private Map<Point, List<Point>> farPoints;
    private List<Cluster> sortedClusters;
    private int numTractors;
    private double edgeLength;
    private double time;
    private int numBins;

    public Player() {
        rand = new Random(seed);
        commandCenter = new HashMap<Integer, List<Command>>();
        farPoints = new HashMap<Point, List<Point>>();
        sortedClusters = new ArrayList<Cluster>();
        numTractors = 0;
        edgeLength = 0.0;
        time = 0.0;
        numBins = 0;
    }
    
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = new ArrayList<Point>(bales);
        clusterAnchors = new ArrayList<Point>();
        bucketAnchors = new ArrayList<>();
        buckets = new ArrayList<>();
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
        numBins = numTractors / 2;
        for (int i = 0; i < numTractors; i++) {
            List<Command> commands = new ArrayList<Command>();
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
                // farPoints.put(p, ten);
                List<Point> c = new ArrayList<Point>(ten);
                c.add(p);
                int index = getNearestToOrigin(c);
                Point anchor = c.remove(index);
                clusterAnchors.add(anchor);
                Cluster cluster = new Cluster(anchor, c);
                sortedClusters.add(cluster);
                ////////////////////////////////////////to do!!!!!!!!!!!!!!!! fill the queue
            }
            else {
                this.bales.add(0, p);
                break;
            }
        }
        bucketClusters();
        System.out.println(bucketAnchors.size());
        System.out.println(buckets.size());
        System.out.println(bucketAnchors.get(0).size());
        System.out.println(buckets.get(0).size());
        Collections.sort(sortedClusters);
    }






// private void handleClusters(Tractor tractor) {

//         if (tractor.getId() % 2 == 0) {
//             // Iterate through bin of clusters for this tractor
//             Cluster cluster = sortedClusters.remove(sortedClusters.size() - 1);
//             collectWithTrailer(tractor, cluster.getAnchor(), cluster.getOthers());
//         } else {
//             // populate list with clusters this tractor is responsible for condensing
//             // Chinmay returns list of clusters which are near each other
//             //  Cluster cluster = sortedClusters.remove(sortedClusters.size() - 1);
            
//             Cluster cluster = sortedClusters.remove(sortedClusters.size() - 1);
//             condenseCluster(tractor, cluster.getAnchor(), cluster.getOthers());

//             // After all dedicated clusters have been condensed, run the greedy collection
//             // Algorithm on the remaining bales
//             Point p = bales.remove(0);
//             collectGreedy(tractor, p);

//             // Alternatively go help unload the trailers at home
//             commands.add(Command.createMoveCommand(new Point(0.0, 0.0)));
//             // cycle through unloading trailers
//             // KEEP global variable of trailers that are currently being unloaded to query their current capacity
//             // if trailer capacity > 3
//             commands.add(new Command(CommandType.UNSTACK));
//             commands.add(new Command(CommandType.UNLOAD));

//         }
//         // if (sortedClusters.size() != 0) {
//         //     Cluster cluster = sortedClusters.remove(sortedClusters.size() - 1);
//         //     collectWithTrailer(tractor, cluster.getAnchor(), cluster.getOthers());
//         // }

//         // else {
//         //     Cluster cluster = sortedClusters.remove(sortedClusters.size() - 1);
//         //     collectWithoutTrailer(tractor, cluster.getAnchor(), cluster.getOthers());
//         // }

//         // System.out.println("**************************************tractor ID is: " + tractorID + "*********************************************");
//     }

    private void condenseCluster(Tractor tractor, Point p, List<Point> ten) {
        int tractorID = tractor.getId();
        List<Command> commands = commandCenter.get(tractorID);
        if (tractor.getAttachedTrailer() != null) {
            commands.add(new Command(CommandType.DETATCH));
        }

        for (Point bale: ten) {
            commands.add(Command.createMoveCommand(bale));
            commands.add(new Command(CommandType.LOAD));
            commands.add(Command.createMoveCommand(p));
            commands.add(new Command(CommandType.UNLOAD));
        }
    }

    private void collectGreedy(Tractor tractor, Point p) {
        int tractorID = tractor.getId();
        List<Command> commands = commandCenter.get(tractorID);
        if (tractor.getAttachedTrailer() != null) {
            commands.add(new Command(CommandType.DETATCH));
        }
        commands.add(Command.createMoveCommand(p));
        commands.add(new Command(CommandType.LOAD));
        commands.add(Command.createMoveCommand(new Point(0.0, 0.0)));
        commands.add(new Command(CommandType.UNLOAD));
    }

    // private void collectWithTrailer(Tractor tractor, Point p, List<Point> ten) {
    //     int tractorID = tractor.getId();
    //     List<Command> commands = commandCenter.get(tractorID);

    //     // forward trip
    //     if (tractor.getAttachedTrailer() == null) {
    //         commands.add(new Command(CommandType.ATTACH));
    //     }
    //     commands.add(Command.createMoveCommand(p));
    //     commands.add(new Command(CommandType.DETATCH));
    //     for (Point bale : ten) {
    //         commands.add(Command.createMoveCommand(bale));
    //         commands.add(new Command(CommandType.LOAD));
    //         commands.add(Command.createMoveCommand(p));
    //         commands.add(new Command(CommandType.STACK));
    //     }
    //     commands.add(new Command(CommandType.LOAD));
    //     commands.add(new Command(CommandType.ATTACH));

    //     //backward trip
    //     commands.add(Command.createMoveCommand(new Point(0.0, 0.0)));
    //     commands.add(new Command(CommandType.DETATCH));
    //     commands.add(new Command(CommandType.UNLOAD));
    //     for (int i = 0; i < ten.size(); i++) {
    //         commands.add(new Command(CommandType.UNSTACK));
    //         commands.add(new Command(CommandType.UNLOAD));
    //     }

    //     // possible callback function
    // }
















    public List<Point> closestAnchors(List<Point> anchorsCopy,int bucketSize){
        List<Point> anchors = new ArrayList<Point>(anchorsCopy);
        Point p = anchors.get(0);
        anchors.remove(p);
        PriorityQueue<Point> sortedToPoint = new PriorityQueue<Point>(anchors.size(), 
            new Comparator(){
                public int compare(Object o1, Object o2) {
                    Point p1 = (Point) o1;
                    Point p2 = (Point) o2;
                    return (int) Math.signum((p1.x - p.x) * (p1.x - p.x) + (p1.y - p.y) * (p1.y - p.y) - (p2.x - p.x) * (p2.x - p.x) - (p2.y - p.y) * (p2.y - p.y));
               }
            }
        );
        for (Point bale : anchors) {
            sortedToPoint.add(bale);
        }
        List<Point> result = new ArrayList<Point>();
        int numBales = bucketSize;
        if (sortedToPoint.size() < numBales) {
            numBales = sortedToPoint.size();
        }
        for (int i = 0; i < numBales; i++) {
            Point point = sortedToPoint.poll();
            result.add(point);
        }
        return result;
    }
    public void bucketClusters(){
        int bucketSize = (int) Math.floor(2.0*sortedClusters.size()/numTractors);
        int times = sortedClusters.size() - bucketSize*numTractors/2;
        List<Point> anchors = new ArrayList<Point>(clusterAnchors);
        bucketSize += 1;
        while(anchors.size()!=0){
            if(times==0){
                bucketSize-=1;
            }
            List<Cluster> temp = new ArrayList<Cluster>();
            List<Point> temp2 = new ArrayList<Point>();
            List<Point> closestPoints = new ArrayList<Point>();
            closestPoints = closestAnchors(anchors,bucketSize-1);
            Point p = anchors.get(0);
            temp2.add(p);
            for(int j=0;j<sortedClusters.size();j++){
                if ((sortedClusters.get(j).getAnchor().x==p.x)&&(sortedClusters.get(j).getAnchor().y==p.y)){
                    temp.add(sortedClusters.get(j));
                    break;
                }
            }
            anchors.remove(p);
            for(int i=0;i<closestPoints.size();i++){
                p = closestPoints.get(i);
                temp2.add(p);
                for(int j=0;j<sortedClusters.size();j++){
                    if ((sortedClusters.get(j).getAnchor().x==p.x)&&(sortedClusters.get(j).getAnchor().y==p.y)){
                        temp.add(sortedClusters.get(j));
                        break;
                    }
                }
                anchors.remove(p);
            }
            buckets.add(temp);
            bucketAnchors.add(temp2);
            times-=1;
        }
    }

    private void oneTrip(Tractor tractor) {

        int index = getBin(tractor);
        if (isHelper(tractor) && buckets.get(index).size() != 0) {
        	List<Cluster> clusters = buckets.get(index);
        	Cluster c = clusters.remove(clusters.size() - 1);
        	System.out.println(clusters.size());
            condenseCluster(tractor, c.getAnchor(), c.getOthers());
        }
        else if (bucketAnchors.get(index).size() != 0) {
            // haul clusters back with trailer
            List<Point> anchors = bucketAnchors.get(index);
            Point p = anchors.remove(anchors.size() - 1);
            collectWithTrailer(tractor, p);
        }
        else {
            Point p = bales.remove(0);
            collectWithoutTrailer(tractor, p);
        }
        // if (sortedClusters.size() != 0) {
        //     Cluster cluster = sortedClusters.remove(sortedClusters.size() - 1);
        //     collectWithTrailer(tractor, cluster.getAnchor(), cluster.getOthers());
        // }
        // else {
        //     Point p = bales.remove(0);
        //     collectWithoutTrailer(tractor, p);
        // }
        // System.out.println("***************************************tractor ID is: " + tractorID + "**********************************************");
    }

    private int getBin(Tractor tractor) {
        int tractorID = tractor.getId();
        return tractorID / 2;
    }

    private boolean isHelper(Tractor tractor) {
        int tractorID = tractor.getId();
        return (tractorID % 2 == 0) ?true: false;
    }
    // private void oneTrip(Tractor tractor) {
    //     if (farPoints.size() != 0) {
    //         Map.Entry<Point, List<Point>> entry = farPoints.entrySet().iterator().next();

    //         List<Point> cluster = new ArrayList<Point>();
    //         cluster.add(entry.getKey());
    //         List<Point> nearBales = entry.getValue();
    //         for(int i=0; i < nearBales.size();i++){
    //           cluster.add(nearBales.get(i));
    //         }
    //         int minIndex = getNearestToOrigin(cluster);
    //         Point nearest = cluster.get(minIndex);
    //         cluster.remove(minIndex);

    //         farPoints.remove(entry.getKey());
    //         collectWithTrailer(tractor, nearest, cluster);
    //     }
    //     else {
    //         Point p = bales.remove(0);
    //         collectWithoutTrailer(tractor, p);
    //     }

    //     // System.out.println("***************************************tractor ID is: " + tractorID + "**********************************************");
    // }

    // when the tractor is back to the original
    // private void oneTrip(Tractor tractor) {
    //     if (sortedClusters.size() != 0) {
    //         Cluster cluster = sortedClusters.remove(sortedClusters.size() - 1);
    //         collectWithTrailer(tractor, cluster.getAnchor(), cluster.getOthers());
    //     }
    //     else {
    //         Point p = bales.remove(0);
    //         collectWithoutTrailer(tractor, p);
    //     }
    // }

    private void collectWithoutTrailer(Tractor tractor, Point p) {
        int tractorID = tractor.getId();
        List<Command> commands = commandCenter.get(tractorID);
        if (tractor.getAttachedTrailer() != null) {
            commands.add(new Command(CommandType.DETATCH));
        }
        commands.add(Command.createMoveCommand(p));
        commands.add(new Command(CommandType.LOAD));
        commands.add(Command.createMoveCommand(new Point(0.0, 0.0)));
        commands.add(new Command(CommandType.UNLOAD));
    }

    // private void collectWithTrailer(Tractor tractor, Point p, List<Point> ten) {
    //     int tractorID = tractor.getId();
    //     List<Command> commands = commandCenter.get(tractorID);

    //     // forward trip
    //     if (tractor.getAttachedTrailer() == null) {
    //         commands.add(new Command(CommandType.ATTACH));
    //     }
    //     commands.add(Command.createMoveCommand(p));
    //     commands.add(new Command(CommandType.DETATCH));
    //     for (Point bale : ten) {
    //         commands.add(Command.createMoveCommand(bale));
    //         commands.add(new Command(CommandType.LOAD));
    //         commands.add(Command.createMoveCommand(p));
    //         commands.add(new Command(CommandType.STACK));
    //     }
    //     commands.add(new Command(CommandType.LOAD));
    //     commands.add(new Command(CommandType.ATTACH));

    //     //backward trip
    //     commands.add(Command.createMoveCommand(new Point(0.0, 0.0)));
    //     commands.add(new Command(CommandType.DETATCH));
    //     commands.add(new Command(CommandType.UNLOAD));
    //     for (int i = 0; i < ten.size(); i++) {
    //         commands.add(new Command(CommandType.UNSTACK));
    //         commands.add(new Command(CommandType.UNLOAD));
    //     }

    //     // possible callback function
    // }

    private void collectWithTrailer(Tractor tractor, Point p) {
        int tractorID = tractor.getId();
        List<Command> commands = commandCenter.get(tractorID);

        // forward trip
        if (tractor.getAttachedTrailer() == null) {
            commands.add(new Command(CommandType.ATTACH));
        }
        commands.add(Command.createMoveCommand(p));
        commands.add(new Command(CommandType.DETATCH));
        for (int i =0; i < 10; i++) {
            commands.add(Command.createMoveCommand(p));
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
        for (int i = 0; i < 10; i++) {
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

    public int getNearestToOrigin(List<Point> bales)
    {
        Point target = new Point(0.0, 0.0);
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

        if (commands.size() == 0 && bales.size() != 0) {
            oneTrip(tractor);
        }

        if (commands.size() == 0) {
            return new Command(CommandType.UNLOAD);
        }
        else {
            return commands.remove(0);
        }
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

    /* A new data structure to represent each cluster in the field. A cluster is 
    consisted of one anchor, which is the closest Point to the origin among the 11
    Points, and other 10 Points.
    */

    private class Cluster implements Comparable<Cluster>{
        Point anchor;
        List<Point> others;

        public Cluster (Point a, List<Point> o) {
            anchor = new Point(a.x, a.y);
            others = new ArrayList<Point>(o);
        }

        public Point getAnchor() {
            return new Point(anchor.x, anchor.y);
        }

        public List<Point> getOthers() {
            return new ArrayList<Point>(others);
        }

        @Override
        public int compareTo(Cluster cluster) {
            return (int) Math.signum(anchor.x * anchor.x + anchor.y * anchor.y - cluster.anchor.x * cluster.anchor.x - cluster.anchor.y * cluster.anchor.y);
        }

    }

    
}
