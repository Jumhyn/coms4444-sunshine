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
    List<Point> balesList;
    Point balesListCenter;

    List<Point> clusterAnchors;

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
        List<Point> greaterThan = new ArrayList<Point>();
        while (this.bales.size() != 0) {
            Point p = this.bales.remove(0);
            double disToOrigin = calcEucDistance(new Point(0.0, 0.0), p);
            if (disToOrigin > 260) {
                greaterThan.add(p);
                
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

        //KMeans(greaterThan,10000);
        Collections.sort(sortedClusters);
    }

    // when the tractor is back to the original
    public Point lazyMove(Point current, Point dest) 
    {
        double distance = calcEucDistance(current, dest);
        if (distance < 1) {
        	return current;
        }

        double xDif = dest.x - current.x;
        double yDif = dest.y - current.y;

        double newX = dest.x + (xDif * 0.99) / distance;
        double newY = dest.y + (yDif * 0.99) / distance;
        Point lazyPoint = new Point(newX, newY);
        return lazyPoint;
    }

    private void oneTrip(Tractor tractor) {
        if (sortedClusters.size() != 0) {
            Cluster cluster = sortedClusters.remove(sortedClusters.size() - 1);
            Point parking = calculateParking(cluster.getAnchor());
            collectWithTrailerDistant(tractor, parking, cluster.getAll());
        }
        else {
            Point p = bales.remove(0);
            collectWithoutTrailer(tractor, p);
        }
    }

    private Point calculateParking(Point p) {
        int dis = (int) Math.sqrt(p.x * p.x + p.y * p.y);
        int x = (dis + 10) * (int) p.x / dis;
        int y = (dis + 10) * (int) p.y / dis;
        return new Point(x, y);
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


        Point home = new Point(0.0, 0.0);
        Point tractorPos = tractor.getLocation();
        Point newP = lazyMove(tractorPos, home);

        commands.add(Command.createMoveCommand(newP));
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

        Point tractorPos = tractor.getLocation();
        Point newP = lazyMove(tractorPos, p);

        commands.add(Command.createMoveCommand(newP));
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
        Point home = new Point(0.0, 0.0);
        tractorPos = tractor.getLocation();
        newP = lazyMove(tractorPos, home);

        commands.add(Command.createMoveCommand(newP));
        commands.add(new Command(CommandType.DETATCH));
        commands.add(new Command(CommandType.UNLOAD));
        for (int i = 0; i < 10; i++) {
            commands.add(new Command(CommandType.UNSTACK));
            commands.add(new Command(CommandType.UNLOAD));
        }
    }


    private void collectWithTrailerDistant(Tractor tractor, Point parking, List<Point> eleven) {
        int tractorID = tractor.getId();
        List<Command> commands = commandCenter.get(tractorID);

        // forward trip
        if (tractor.getAttachedTrailer() == null) {
            commands.add(new Command(CommandType.ATTACH));
        }

        Point tractorPos = tractor.getLocation();
        Point newP = lazyMove(tractorPos, parking);

        commands.add(Command.createMoveCommand(newP));
        commands.add(new Command(CommandType.DETATCH));
        for (int i = 0; i < eleven.size() - 1; i++) {
            Point bale = eleven.get(i);
            commands.add(Command.createMoveCommand(bale));
            commands.add(new Command(CommandType.LOAD));
            commands.add(Command.createMoveCommand(parking));
            commands.add(new Command(CommandType.STACK));
        }
        commands.add(Command.createMoveCommand(eleven.get(eleven.size() - 1)));
        commands.add(new Command(CommandType.LOAD));
        commands.add(Command.createMoveCommand(parking));
        commands.add(new Command(CommandType.ATTACH));

        //backward trip
        Point home = new Point(0.0, 0.0);
        tractorPos = tractor.getLocation();
        newP = lazyMove(tractorPos, home);

        commands.add(Command.createMoveCommand(newP));
        commands.add(new Command(CommandType.DETATCH));
        commands.add(new Command(CommandType.UNLOAD));
        for (int i = 0; i < eleven.size() - 1; i++) {
            commands.add(new Command(CommandType.UNSTACK));
            commands.add(new Command(CommandType.UNLOAD));
        }
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
    }
    private void buildList() {
    	for (int i = 0; i < 11; i++) {
    		int index = getNearestBale();
    		balesList.add(bales.get(index));
    		bales.remove(index);
    	}
    }
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

        public List<Point> getAll() {
            List<Point> result = new ArrayList<Point>(others);
            result.add(new Point(anchor.x, anchor.y));
            System.out.println("the size of all points: " + result.size());
            return result;
        }

        @Override
        public int compareTo(Cluster cluster) {
            return (int) Math.signum(anchor.x * anchor.x + anchor.y * anchor.y - cluster.anchor.x * cluster.anchor.x - cluster.anchor.y * cluster.anchor.y);
        }
    }
	public void KMeans(List<Point> bales, int max_iter){
		double[][] points = new double[bales.size()][2];
		for(int i=0;i<bales.size();i++){
			points[i][0] = bales.get(i).x;
			points[i][1] = bales.get(i).y;
		}
		sortPointsByX(points);
		int maxIterations = max_iter;
		int clusters = bales.size()/11;
		double[][] means = new double[clusters][2];
		for(int i=0; i<means.length; i++) {
			means[i][0] = points[(int) (Math.floor((bales.size()*1.0/clusters)/2) + i*bales.size()/clusters)][0];
			means[i][1] = points[(int) (Math.floor((bales.size()*1.0/clusters)/2) + i*bales.size()/clusters)][1];
		}
		ArrayList<Integer>[] oldClusters = new ArrayList[clusters];
		ArrayList<Integer>[] newClusters = new ArrayList[clusters];
		for(int i=0; i<clusters; i++) {
			oldClusters[i] = new ArrayList<Integer>();
			newClusters[i] = new ArrayList<Integer>();
		}
		formClusters(oldClusters, means, points);
		int iterations = 0;
		while(true) {
			updateMeans(oldClusters, means, points);
			formClusters(newClusters, means, points);
			iterations++;
			if(iterations > maxIterations || checkEquality(oldClusters, newClusters))
				break;
			else
				resetClusters(oldClusters, newClusters);
		}
		storeOutputs(oldClusters, points, bales, means);
		System.out.println(sortedClusters.get(0).getAnchor().x);
		System.out.println(sortedClusters.get(0).getAnchor().y);
		System.out.println(sortedClusters.get(0).getOthers().get(0).x);
		System.out.println(sortedClusters.get(0).getOthers().get(0).y);
		System.out.println(sortedClusters.get(0).getOthers().get(1).x);
		System.out.println(sortedClusters.get(0).getOthers().get(1).y);

	}
	public void sortPointsByX(double[][] points) {
		double[] temp;
		for(int i=0; i<points.length; i++)
		    for(int j=1; j<(points.length-i); j++)
			if(points[j-1][0] > points[j][0]) {
			    temp = points[j-1];
			    points[j-1] = points[j];
			    points[j] = temp;
			}
	}
	public void updateMeans(ArrayList<Integer>[] clusterList, double[][] means, double[][] points) {
		double totalX = 0;
		double totalY = 0;
		for(int i=0; i<clusterList.length; i++) {
			totalX = 0;
			totalY = 0;
			for(int index: clusterList[i]) {
				totalX += points[index][0];
				totalY += points[index][1];
			}
			means[i][0] = totalX/clusterList[i].size();
			means[i][1] = totalY/clusterList[i].size();
		}
	}
	public void formClusters(ArrayList<Integer>[] clusterList, double[][] means, double[][] points) {
		double distance[] = new double[means.length];
		double minDistance = 999999999;
		int minIndex = 0;

		for(int i=0; i<points.length; i++) {
			minDistance = 999999999;
			for(int j=0; j<means.length; j++) {
				distance[j] = Math.sqrt(Math.pow((points[i][0] - means[j][0]), 2) + Math.pow((points[i][1] - means[j][1]), 2));
				if((distance[j] < minDistance)&&(clusterList[j].size()<=11)) {
					minDistance = distance[j];
					minIndex = j;
				}
			}
			clusterList[minIndex].add(i);
		}
	}
	public boolean checkEquality(ArrayList<Integer>[] oldClusters, ArrayList<Integer>[] newClusters) {
		for(int i=0; i<oldClusters.length; i++) {
			// Check only lengths first
			if(oldClusters[i].size() != newClusters[i].size())
				return false;

			// Check individual values if lengths are equal
			for(int j=0; j<oldClusters[i].size(); j++)
				if(oldClusters[i].get(j) != newClusters[i].get(j))
					return false;
		}

		return true;
	}

	public void resetClusters(ArrayList<Integer>[] oldClusters, ArrayList<Integer>[] newClusters) {
		for(int i=0; i<newClusters.length; i++) {
			// Copy newClusters to oldClusters
			oldClusters[i].clear();
			for(int index: newClusters[i])
				oldClusters[i].add(index);

			// Clear newClusters
			newClusters[i].clear();
		}
	}

	public void storeOutputs(ArrayList<Integer>[] clusterList, double[][] points, List<Point> bales, double[][] means) {
        for(int i=0; i<clusterList.length; i++) {
            List<Point> others = new ArrayList();
            for(int index: clusterList[i]){
                Point p = bales.get(index);
				others.add(p);
			}
			Point anchor = new Point(means[i][0],means[i][1]);
		    Cluster c = new Cluster(anchor,others);
		    sortedClusters.add(c);

		}
	}
}
