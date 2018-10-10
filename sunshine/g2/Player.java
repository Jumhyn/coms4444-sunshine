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
        Collections.sort(sortedClusters);
        // int numFarPoints = farPoints.size();
        // if (numFarPoints < numTractors) {
        //     for (int i = numFarPoints; i < numTractors; i++) {
        //         List<Command> commands = new ArrayList<Command>();
        //         commands.add(new Command(CommandType.DETATCH));
        //         commandCenter.put(i, commands);
        //     }
        // }

        // System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&map size: " + farPoints.size());
        // while (this.bales.size() != 0 || farPoints.size() != 0) {
        //     for (int i = 0; i < numTractors; i++) {
        //         if (this.bales.size() != 0 || farPoints.size() != 0) {
        //             oneTrip(i);
        //         }
        //     }
        // }

     //    balesList = new ArrayList<Point>();
     //    counter = 0;
    	// balesListCenter = this.bales.get(getFurthestBale());
    	// buildList();
    }
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
            List<Point> closestPoints = new ArrayList<Point>();
            closestPoints = closestAnchors(anchors,bucketSize-1);
            Point p = anchors.get(0);
            for(int j=0;j<sortedClusters.size();j++){
                if ((sortedClusters.get(j).getAnchor().x==p.x)&&(sortedClusters.get(j).getAnchor().y==p.y)){
                    temp.add(sortedClusters.get(j));
                    break;
                }
            }
            anchors.remove(p);
            for(int i=0;i<closestPoints.size();i++){
                p = closestPoints.get(i);
                for(int j=0;j<sortedClusters.size();j++){
                    if ((sortedClusters.get(j).getAnchor().x==p.x)&&(sortedClusters.get(j).getAnchor().y==p.y)){
                        temp.add(sortedClusters.get(j));
                        break;
                    }
                }
                anchors.remove(p);
            }
            buckets.add(temp);
            times-=1;
        }
    }
    /* Chinmay's function to divide the sortedClusters into numBins, and record the anchor
       of each cluster for each bin so that Frank can remove the whole cluster when the 
       helper tractor moved all the Points in one cluster to the anchor.

       Two member variables will be filled: sortedBinClusters, sortedBinAnchors
    */ 
    // Chinmay's function here


    /* Frank's function to dispatch a tractor to move the clusters to their anchor or tractor
       with trailer to collect all the bales at the anchor position and ship them back. Frank
       can assume that tractors with odd IDs are with trailers, and those with even IDs are 
       helper tractors. And 0, 1 tractors are assigned to the first bin, 2, 3 tractors are 
       assgined to the second bin, and so on. There's no extra data structure needed for this.

       Frank's function will remove collectWithTrailer in the oneTrip function
    */ 
    // Frank's function here

    // // when the tractor is back to the original
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
    private void oneTrip(Tractor tractor) {
        if (sortedClusters.size() != 0) {
            Cluster cluster = sortedClusters.remove(sortedClusters.size() - 1);
            collectWithTrailer(tractor, cluster.getAnchor(), cluster.getOthers());
        }
        else {
            Point p = bales.remove(0);
            collectWithoutTrailer(tractor, p);
        }

        // System.out.println("***************************************tractor ID is: " + tractorID + "**********************************************");
    }

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

    private void collectWithTrailer(Tractor tractor, Point p, List<Point> ten) {
        int tractorID = tractor.getId();
        List<Command> commands = commandCenter.get(tractorID);

        // forward trip
        if (tractor.getAttachedTrailer() == null) {
            commands.add(new Command(CommandType.ATTACH));
        }
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
