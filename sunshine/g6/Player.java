package sunshine.g6;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Comparator;
import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import sunshine.sim.Trailer;
import sunshine.g6.Point_In_Poly;


public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    
    List<Point> bales;

    // Class members for bale sorting and separating into segments
    private Map<Point, Double> balePointsSorted;
    private HashMap<Point, Double> balePointsForSorting;
    private HashMap<Integer, ArrayList<Point>> segments;
    private HashMap<Integer, ArrayList<Point>> seg_vertices;
    private HashMap<Integer, Point> eleventhBale;
    private HashMap<Integer, Integer> trailerBales;
    private List<Point> closeBales;

    public Player() {
        rand = new Random(seed);
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
            dist = Math.hypot(p.x - 0.0, p.y - 0.0);
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

        // divide into segments according to how many tractors there are

        int num_bales = bales.size();
        double num_segs = n;

        // initialize trailerBales
        trailerBales = new HashMap<Integer, Integer>();
        for (int i=0; i<n; i++) {
            trailerBales.put(i, 0);
        }

        //System.out.println("num tractors = num segs = " + n); //
        //System.out.println("num bales = " + num_bales); //

        int numFarBales = n*11; // so that each trailer can be mapped to a segment of 10

        List<Point> farBalePoints = (new ArrayList<Point>(balePointsSorted.keySet())).subList(num_bales - numFarBales, num_bales);
        //System.out.println("num far bales (farBalePoints.size()) = " + farBalePoints.size());

        closeBales = (new ArrayList<Point>(balePointsSorted.keySet())).subList(0, num_bales - numFarBales);
        //System.out.println("num close bales (closeBales.size()) = " + closeBales.size());

        // segments will map the index of each segment to the list of all bale points in that segment
        segments = new HashMap<Integer, ArrayList<Point>>();

        // get the farBalePoint with the smallest x value and the farBalePoint with the smallest y value
        Double min_x = Double.POSITIVE_INFINITY;
        Double min_y = Double.POSITIVE_INFINITY;
        for (Point p : farBalePoints) {
            if (p.x < min_x) {
                min_x = p.x;
            }
            if (p.y < min_y) {
                min_y = p.y;
            }
        }

        // the corner of the square vertex:
        double sq_x = min_x-0.0001;
        double sq_y = min_y-0.0001;
        double y_len = m - min_y;
        double x_len = m - min_x;

        double seg_rows = Math.floor(num_segs/2.0);
        double seg_cols = Math.ceil(num_segs/2.0);
        //System.out.println("seg_rows = " + seg_rows);
        //System.out.println("seg_cols = " + seg_cols);

        // seg_vertices will map the index of each segment to a list of its 3 vertices
        seg_vertices = new HashMap<Integer, ArrayList<Point>>();

        double x_inc = x_len/seg_cols;
        double y_inc = y_len/seg_rows;
        /*System.out.println("num cols = " + seg_cols);
        System.out.println("num rows = " + seg_rows);
        System.out.println("y_len = " + y_len);
        System.out.println("y_inc = " + y_inc);
        System.out.println("x_len = " + x_len);
        System.out.println("x_inc = " + x_inc);*/
        
        // compute the segment vertices
        double seg_y_Bbound = m;
        double seg_y_Tbound = m-y_inc;

        for (int seg_i=0; seg_i<seg_rows; seg_i++) {

            segments.put(seg_i, new ArrayList<Point>());

            double x_bound = sq_x;

            ArrayList<Point> vertex_list = new ArrayList<Point>();

            vertex_list.add(new Point(m, m));
            vertex_list.add(new Point(x_bound, seg_y_Tbound));
            vertex_list.add(new Point(x_bound, seg_y_Bbound));

            /*System.out.println("seg " + seg_i + " vertices:"); //
            System.out.println("(" + 0.0 + ", " + 0.0 + ")"); //
            System.out.println("(" + x_bound + ", " + seg_y_Tbound + ")"); //
            System.out.println("(" + x_bound + ", " + seg_y_Bbound + ")"); //*/

            seg_vertices.put(seg_i, vertex_list);

            seg_y_Bbound = seg_y_Tbound;
            seg_y_Tbound -= y_inc;

        }

        double seg_x_Lbound = m-x_inc;
        double seg_x_Rbound = m;

        for (int seg_i=(int)(seg_rows); seg_i<(seg_cols+seg_rows); seg_i++) {

            segments.put(seg_i, new ArrayList<Point>());

            double y_bound = sq_y;

            ArrayList<Point> vertex_list = new ArrayList<Point>();

            vertex_list.add(new Point(m, m));
            vertex_list.add(new Point(seg_x_Lbound, y_bound));
            vertex_list.add(new Point(seg_x_Rbound, y_bound));

            /*System.out.println("seg " + seg_i + " vertices:"); //
            System.out.println("(" + 0.0 + ", " + 0.0 + ")"); //
            System.out.println("(" + seg_x_Lbound + ", " + y_bound + ")"); //
            System.out.println("(" + seg_x_Rbound + ", " + y_bound + ")"); //*/

            seg_vertices.put(seg_i, vertex_list);

            seg_x_Rbound = seg_x_Lbound;
            seg_x_Lbound -= x_inc;

        }
        //System.out.println("seg_vertices.size() = " + seg_vertices.size()); //

        //System.out.println("farBalePoints.size() = " + farBalePoints.size());

        for (Point p : farBalePoints) {

            for (int seg_i=0; seg_i<num_segs; seg_i++) {
                if (Point_In_Poly.isInside(seg_vertices.get(seg_i), 3, p)) {
                    segments.get(seg_i).add(p);
                    break;
                }
            }
        }

        /*int count = 0;
        for (int i : segments.keySet()) {
            System.out.println("seg " + i + " number of bales: " + segments.get(i).size());
            count += segments.get(i).size();
        }
        System.out.println("check numFarBales: " + count);*/

        //System.out.println("DISTRIBUTE");
        // distribute bales equally amongst all segments (randomly right now--eventually based on distance for optimization)
        for (int i : segments.keySet()) {
            int seg_size = segments.get(i).size();
            //System.out.println("CHECKING seg " + i + " size = " + seg_size);
            if (seg_size < 11) {
                while (seg_size < 11) {
                    Point removed = segments.get(i+1).remove(0);
                    segments.get(i).add(removed);
                    seg_size+=1;
                }   
            } else {
                while (seg_size > 11) {
                    Point removed = segments.get(i).remove(0);
                    segments.get(i+1).add(removed);
                    seg_size-=1;
                }
            }
            //System.out.println("DONE seg " + i + " size = " + seg_size);
        }
        /*count = 0;
        for (int i : segments.keySet()) {
            //System.out.println("seg " + i + " number of bales: " + segments.get(i).size());
            count += segments.get(i).size();
        }
        System.out.println("check numFarBales: " + count);*/

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
            eleventhBale.put(seg_i, closestP);
        }
        
        /*System.out.println("eleventhBale list:");
        for (int seg_i : eleventhBale.keySet()) {
            System.out.println("seg " + seg_i + ": (" + eleventhBale.get(seg_i).x + ", " + eleventhBale.get(seg_i).y + ")");
        }*/
        


    }


    
    public Command getCommand(Tractor tractor)
    {
        if (tractor.getHasBale())
        {
            if (tractor.getLocation().equals(new Point(0.0, 0.0)))
            {
                return new Command(CommandType.UNLOAD);
            }
            else
            {
                return Command.createMoveCommand(new Point(0.0, 0.0));
            }
        }
        else
        {
            if (tractor.getLocation().equals(new Point(0.0, 0.0)))
            {
                if (rand.nextDouble() > 0.5)
                {
                    if (tractor.getAttachedTrailer() == null)
                    {
                        return new Command(CommandType.ATTACH);
                    }
                    else
                    {
                        return new Command(CommandType.DETATCH);
                    }
                }
                else if (bales.size() > 0)
                {
                    Point p = bales.remove(rand.nextInt(bales.size()));
                    return Command.createMoveCommand(p);
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return new Command(CommandType.LOAD);
            }
        }
    }
}
