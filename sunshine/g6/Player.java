package sunshine.g6;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.LinkedList;
import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import sunshine.sim.Trailer;
import sunshine.g6.Point_In_Poly;
import sunshine.g6.weiszfeld.WeightedPoint;
import sunshine.g6.weiszfeld.WeiszfeldAlgorithm;
import sunshine.g6.weiszfeld.Input;
import sunshine.g6.weiszfeld.Output;


public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    
    List<Point> bales;
    List<Integer> close_tractor = new ArrayList<Integer>();
    List<Integer> away_tractor = new ArrayList<Integer>();
    Map<Integer, Integer> tractor_mode = new HashMap<Integer, Integer>();
    Point dropPoint;

    // Class members for bale sorting and separating into segments
    private LinkedList<Integer> dropPointsToDo = new LinkedList<Integer>();
    private HashMap<Integer, Integer> dropPointPerTractor = new HashMap<Integer, Integer>();
    private HashMap<Point, Double> balePointsSorted;
    private HashMap<Point, Double> balePointsForSorting;
    private HashMap<Integer, ArrayList<Point>> segments;
    private HashMap<Integer, Point> eleventhBale;
    private HashMap<Integer, Integer> balesPerLocation = new HashMap<Integer, Integer>();  
    private List<Point> closeBales;
    private List<Point> farBalePoints;
    private HashMap<Integer, Boolean> segmentVisited;

    List<Point> copy_bales;
    ArrayList<List<Point>> scan_zones;
    ArrayList<List<Point>> equizones; 
    ArrayList<Integer> Tasks;
    int curr_idx, cutoff_thresh;
    double dim;
    Point seed_cluster;
    ArrayList<Point> centers;

    Integer secondBatch;
    int numTrailers;

    public Player() {
        rand = new Random(seed);
    }

    // The following function was taken from g1
    private static double distance(Point p1, Point p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }


    // The following function was taken from g1
    public void partition()
    {
        double k=25;
        scan_zones =  new ArrayList<List<Point>>();

        while((dim*2)/k >=1)
        {
            ArrayList<Point> curr_zone = new ArrayList<Point>();
            for(int i=0;i<copy_bales.size();i++)
            {
                Point tmp = copy_bales.get(i);

                if(tmp.x + tmp.y <= k)
                {
                    curr_zone.add(tmp);
                    copy_bales.remove(i);
                    i--;
                }
            }

            scan_zones.add(curr_zone);

           k+=25;
        }

        int counter =0;
        for(int i=0;i<scan_zones.size();i++)
        {
            for(int j=0;j<scan_zones.get(i).size();j++)
            {
                counter++;
            }
        }
        System.out.println("scan_zones.size() = " + scan_zones.size());
        ArrayList<List<Point>> scan_zones_copy = (ArrayList<List<Point>>)(scan_zones.clone());
        scan_zones.clear();
        for (List<Point> lp : scan_zones_copy) {
            if (lp.size() > 0)
                scan_zones.add(lp);
        }
        int total = 0;
        for (List<Point> lp : scan_zones) {
            total += lp.size();
        }
        System.out.println("!!! " + total);

        System.out.println("partitioned point count is " + counter);


    }

    // The following function was taken from g1
    public void cluster_points()
    {
        List<Point> initial = scan_zones.get(0);
        Collections.sort(initial,new Comparator <Point>() {

        public int compare(Point o1, Point o2) {
        return Double.compare(Math.sqrt(Math.pow(o1.x,2)+ Math.pow(o1.y,2)),Math.sqrt(Math.pow(o2.x,2)+ Math.pow(o2.y,2)));
        }
        });

        
        seed_cluster = initial.get(0);
        equizones = new ArrayList<List<Point>>();
        int zone_count = (farBalePoints.size() % 11 == 0) ? farBalePoints.size()/11 : farBalePoints.size()/11 + 1;

        int j=0;
        while( j<scan_zones.size())
            {
                ArrayList<Point> curr_cluster = new ArrayList<Point>();
                
                while(curr_cluster.size()<11 && j<scan_zones.size() )
                {
                    List<Point> curr_zone = scan_zones.get(j);
                    Collections.sort(curr_zone,new Comparator <Point>() {

                    public int compare(Point o1, Point o2) {
                    return Double.compare(Math.sqrt(Math.pow(o1.x - seed_cluster.x,2)+ Math.pow(o1.y - seed_cluster.y,2)),Math.sqrt(Math.pow(o2.x-seed_cluster.x,2)+ Math.pow(o2.y-seed_cluster.y,2)));
                     }
                    });

                    for(int i=0;i<curr_zone.size() && curr_cluster.size()<11;i++)
                    {
                        curr_cluster.add(curr_zone.get(0));
                        curr_zone.remove(0);
                    }

                    seed_cluster = curr_cluster.get(curr_cluster.size()-1);

                     scan_zones.set(j,curr_zone);
                     if(curr_zone.size() == 0)
                        j++;

                }

                equizones.add(curr_cluster);
            }


            Collections.reverse(equizones);

            int count =0;
            for(int i=0;i<equizones.size();i++)
            {
                for(int z=0;z<equizones.get(i).size();z++)
                {
                    Point tmp = equizones.get(i).get(z);
                    //System.out.print(tmp.x + "," + tmp.y +"\t");
                    count++;
                }
                //System.out.println();
            }
                System.out.println("Points printed are " + count);
            
    }

    // The following function was taken from g1
    public Point getClusterCenter(List<Point> cluster)
    {
        // We use Weiszfeld Algorithm to find the weighted geometric median
        List<WeightedPoint> weightedPoints = new LinkedList<WeightedPoint>();
        for (Point p : cluster) {
            weightedPoints.add(new WeightedPoint(0.2D, p.x, p.y));
        }
        
        weightedPoints.add(new WeightedPoint(0.5D, 0.0D, 0.0D));
        
        Input input = new Input();
        input.setDimension(2);
        input.setPoints(weightedPoints);
        input.setPermissibleError(0.00001);
        
        double[] centerCoordinate = WeiszfeldAlgorithm.process(input).getPointCoordinate();
        
        Point center = new Point(centerCoordinate[0], centerCoordinate[1]);
        Point origin = new Point(0.0D, 0.0D);
        double efficiency = 0.0D; // The time saved from using trailer strategy
        
        for (Point p : cluster) {
            // For each point, we go back to the trailer instead of the origin
            efficiency += 0.2D * (distance(p, origin) - distance(p, center));
        }
        efficiency -= 0.5D * distance(center, origin) // Carrying trailer from and back to the origin
                + (cluster.size() - 1) * 20.0D // Stacking & unstacking cost
                + 240.0D; // attaching & detatching cost
        

        /// hack to not leave behind any bale
        center.x += 0.001;
        center.y += 0.001;
        if (efficiency > 0.0D)
            return center;
        else
            return null;
        
        /*
        Point center = new Point(0,0);

        for(int i=0;i<cluster.size();i++)
        {
            center.x += cluster.get(i).x;
            center.y += cluster.get(i).y;
        }

        if(cluster.size()>0)
        {
            center.x/=cluster.size();
            center.y/=cluster.size();
        }
        
        return center;
        */
    }



    
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = bales;

        // ##############################################################
        // # Sort the bale points according to distance from the origin #
        // ##############################################################

        balePointsForSorting = new HashMap<Point, Double>();
        double dist = 0.0;
        for (Point p : bales) {
            dist = Math.hypot(p.x - 0.0, p.y - 0.0); //distance
            balePointsForSorting.put(p, dist); //assuming there cannot be more than 1 bale of hay in a position
            balePointsSorted = balePointsForSorting
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .collect(
                    toMap(e -> e.getKey(), e -> e.getValue(), (e1,e2) -> e2,
                        LinkedHashMap::new));
            balePointsSorted = balePointsForSorting
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(
                    toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e2,
                        LinkedHashMap::new));
        } //Credit to the following website for the logic: https://www.javacodegeeks.com/2017/09/java-8-sorting-hashmap-values-ascending-descending-order.html
        //System.out.println("balePointsSorted: " + balePointsSorted); //


        // #############################################
        // # Divide the grid into pizza slice segments #
        // #############################################

        // divide into segments according to how many bales there are outside of 300m radius

        int num_bales = bales.size();
        System.out.println("num_bales = " + num_bales);

        farBalePoints = new ArrayList<Point>();
        int index = 0;
        for (Point p : balePointsSorted.keySet()) {
            if (balePointsSorted.get(p) > 50) {
                break;
            }
            index+=1;
        }
        int numFarBales = num_bales - index;
        System.out.println("old index = " + index);
        System.out.println("numFarBales (before) = " + numFarBales);

        if (numFarBales % 11 != 0) {
            index = index + (numFarBales % 11);
        }
        numFarBales = num_bales - index;
        System.out.println("new index = " + index);
        System.out.println("numFarBales = " + numFarBales);

        double num_segs = numFarBales/11;
        System.out.println("num_segs = " + num_segs);

        farBalePoints = (new ArrayList<Point>(balePointsSorted.keySet())).subList(index, num_bales);
        System.out.println("num far bales (farBalePoints.size()) = " + farBalePoints.size());

        closeBales = (new ArrayList<Point>(balePointsSorted.keySet())).subList(0, index);
        System.out.println("num close bales (closeBales.size()) = " + closeBales.size());

        copy_bales = new ArrayList<Point>();
        for(int i=0;i<farBalePoints.size();i++)
            copy_bales.add(farBalePoints.get(i));

        curr_idx=0;
        seed_cluster = new Point(0.0,0.0);

        centers = new ArrayList<Point>(n);
        for (int i = 0; i < n; i++) {
            centers.add(null);
        }

        dim =m;

        partition();
        cluster_points();

        // segments will map the index of each segment to the list of all bale points in that segment
        segments = new HashMap<Integer, ArrayList<Point>>();
        segmentVisited = new HashMap<Integer, Boolean>();
        for (int i=0; i<num_segs; i++) {
            segments.put(i, (ArrayList<Point>)(equizones.get(i)));
            segmentVisited.put(i, false);
        }
        // System.out.println("segmentVisited: " + segmentVisited);
        // System.out.println("segments.size() = " + segments.size());
        // System.out.println("lengths of lists of points in segments:");
        // for (List<Point> lp : segments.values()) {
        //     System.out.println(lp.size());
        // }

        // create the eleventhBale list
        eleventhBale = new HashMap<Integer, Point>();
        Point closestP = new Point(0.0, 0.0);
        double p_dist = 0.0;
        for (int seg_i : segments.keySet()) {
            double min_dist = Double.POSITIVE_INFINITY;
            for (Point p : segments.get(seg_i)) {
                p_dist = Math.hypot(p.x - 0.0, p.y - 0.0);
                if (p_dist < min_dist) {
                    min_dist = p_dist;
                    closestP = p;
                }
            }
            segments.get(seg_i).remove(closestP);
            eleventhBale.put(seg_i, closestP);
            this.dropPointsToDo.add(seg_i);
        }
        
        System.out.println("eleventhBale list:");
        for (int seg_i : eleventhBale.keySet()) {
            System.out.println("seg " + seg_i + ": (" + eleventhBale.get(seg_i).x + ", " + eleventhBale.get(seg_i).y + ")");
        }
        int count = 0;
        for (int i : segments.keySet()) {
            //System.out.println("seg " + i + " number of bales: " + segments.get(i).size());
            count += segments.get(i).size();
        }
        // System.out.println("check numFarBales: " + (count + eleventhBale.size()));

        System.out.println("size of segments:" + segments.size());

        // add tractors into away_tractor and close_tractor list
        for (int i = 0; i < n; i++) {
            if (i < segments.size()) {
                away_tractor.add(i);
                tractor_mode.put(i, 1);
            }
            else {
                close_tractor.add(i);
                tractor_mode.put(i, 0);
            }
        }

        // System.out.println("away_tractors: " + away_tractor.size());
        // System.out.println("close_tractors: " + close_tractor.size());

        for (int i=0; i<segments.size(); i++) {
            balesPerLocation.put(i, 0);
        }
    }

    int secondBatchStart = 0;
    int multiplier = 1;

    public Command getCommand(Tractor tractor){

        int id = tractor.getId();
        int randNum = 0;
        Point p;
        int segmentId = 0;        
        int segmentsLeft = 0;
        
        switch(tractor_mode.get(id))
        {     
            // at 0,0, close_tractor ready to go to task
            case 0:
            tractor_mode.put(id,1);
            return new Command(CommandType.DETATCH);
            // at 0,0, away_tractor with trailer ready to go do task
            case 1:
            if (close_tractor.contains(id)) {
                tractor_mode.put(id,7);
                if (closeBales.size() > 0) {
                    randNum = closeBales.size() - 1;
                    p = closeBales.remove(randNum);
                    bales.remove(randNum);
                    return Command.createMoveCommand(p);
                }
                else {
                    return null;
                }
            }
            else { 
                tractor_mode.put(id,2);
                
                if(dropPointsToDo.size() == 0) {
                    // We're done!
                    close_tractor.add(id);
                    away_tractor.remove(Integer.valueOf(id));
                    tractor_mode.put(id, 1);
                    return Command.createMoveCommand(new Point(0,0));
                }

                // There's still work to do

                // Pick in order
                int thisDropPoint = dropPointsToDo.pop();

                // Pick at random
                // int thisDropPoint = dropPointsToDo.remove(rand.nextInt(dropPointsToDo.size()));

                dropPointPerTractor.put(id, thisDropPoint); // remember which bale we're working on

                balesPerLocation.put(dropPointPerTractor.get(id),0);  // this point has 0 bales initially
                dropPoint = eleventhBale.get(dropPointPerTractor.get(id));
                segmentVisited.put(dropPointPerTractor.get(id), true);
                return Command.createMoveCommand(dropPoint); //go to the point that drops trailer
            }

            case 2:
            tractor_mode.put(id,3);
            return new Command(CommandType.DETATCH); 

            //go to location of bale in far area
            case 3:
            tractor_mode.put(id,7);
            // System.out.println("error:" + (dropPointPerTractor.get(id)));
            randNum = rand.nextInt(segments.get(dropPointPerTractor.get(id)).size());
            p = segments.get(dropPointPerTractor.get(id)).remove(randNum);
            bales.remove(randNum);
            farBalePoints.remove(randNum);
            return Command.createMoveCommand(p);

            //return to the trailer and bales amount at this point +1
            case 4:
            tractor_mode.put(id,5);
            return Command.createMoveCommand(eleventhBale.get(dropPointPerTractor.get(id)));

            //stack bale into trailer, increment trailer count
            case 5:
            tractor_mode.put(id,6);
            // System.out.println("checking if tractor " + id + " has bale at case 5: " + tractor.getHasBale());
            System.out.println("case 5 id: " + (dropPointPerTractor.get(id)));

            System.out.println("all keys in balesPerLocation");
            for (Map.Entry<Integer, Boolean> entry : segmentVisited.entrySet()) {
                Integer key = entry.getKey();
                Boolean value = entry.getValue();
                System.out.println("segment no:" + key + " - " + value);
            }

            balesPerLocation.put(dropPointPerTractor.get(id), balesPerLocation.get(dropPointPerTractor.get(id))+1);    
            return new Command(CommandType.STACK);    

            case 6:
            // if full, attach
            if ((balesPerLocation.get(dropPointPerTractor.get(id))>9)) // || (segments.get(dropPointPerTractor.get(id)).size() == 0)
            {
                tractor_mode.put(id,12);
                segmentVisited.put(dropPointPerTractor.get(id),true); 
                return new Command(CommandType.LOAD);
                // return new Command(CommandType.ATTACH);
            }
            else{   // if not, find more bales
                tractor_mode.put(id,7);
                randNum = rand.nextInt(segments.get(dropPointPerTractor.get(id)).size());
                p = segments.get(dropPointPerTractor.get(id)).remove(randNum);
                bales.remove(randNum);
                farBalePoints.remove(randNum);
                return Command.createMoveCommand(p);
            }

            //load
            case 7:
            if (close_tractor.contains(id)) {
                tractor_mode.put(id,8);
                return new Command(CommandType.LOAD);
            }
            else {
                tractor_mode.put(id,4);
                return new Command(CommandType.LOAD);
            }
            
            // return to origin
            case 8:
            tractor_mode.put(id,9);
            return Command.createMoveCommand(new Point(0,0));

            case 9:
            if (close_tractor.contains(id)){
                tractor_mode.put(id,1);
                return new Command(CommandType.UNLOAD);
            }
            else{
                tractor_mode.put(id,13);
                return new Command(CommandType.DETATCH);
            }

            case 10:
            if (balesPerLocation.get(dropPointPerTractor.get(id)) > 0) {
                tractor_mode.put(id, 11);
                System.out.println("secondBatchStart value in case 10: " + secondBatchStart);
                System.out.println("dropPointPerTractor.get(id) value in case 10: " + (dropPointPerTractor.get(id)));
                balesPerLocation.put(dropPointPerTractor.get(id), balesPerLocation.get(dropPointPerTractor.get(id)) - 1);
                return new Command(CommandType.UNSTACK);
            }
            else {
                tractor_mode.put(id, 1);
                close_tractor.add(id);
                away_tractor.remove(Integer.valueOf(id));
                return Command.createMoveCommand(new Point(0,0));
            }

            case 11:
            tractor_mode.put(id,10);
            return new Command(CommandType.UNLOAD);

            case 12:
            tractor_mode.put(id,8);
            return new Command(CommandType.ATTACH);
            
            //detect after the first batch of trailers returned to origin, are there still has segments
            case 13:  
            // unload everything (up to 11 bales)
            tractor_mode.put(id,14);
            return new Command(CommandType.UNLOAD);

            // still has segments unvisited
            //here's some problme, the number of balesPerLocation is when secondBatchstart=0,but it is already updated
            case 14:
            if (balesPerLocation.get(dropPointPerTractor.get(id)) > 0) {
                tractor_mode.put(id, 15);
                balesPerLocation.put(dropPointPerTractor.get(id), balesPerLocation.get(dropPointPerTractor.get(id)) - 1);
                return new Command(CommandType.UNSTACK);
            }
            else {
                tractor_mode.put(id, 1);
                return new Command(CommandType.ATTACH);
            }
            //unload, similar as case 11
            case 15:
            tractor_mode.put(id,14);
            return new Command(CommandType.UNLOAD);
            

        }        
        return Command.createMoveCommand(new Point(0,0));
        
    }
}
