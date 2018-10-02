package sunshine.g6; 

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;


public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    
    List<Point> bales;
    private double mapsize;

    private HashMap<Integer, ArrayList<Point>> segments;
    private HashMap<Integer, Point> midpoints;

    public Player() {
        rand = new Random(seed);
    }
    
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = bales;
        //System.out.println("total num_bales = " + bales.size());
        mapsize = m * m;

        // segments will map the index of each segment to the list of all bale points in that segment
        // segments are numbered along each row first, before moving to the next row
        segments = new HashMap<Integer, ArrayList<Point>>();
        // midpoints will map the index of each segment to a Point object containing the midpoint of that segment
        midpoints = new HashMap<Integer, Point>();

        // #################################
        // # Divide the grid into segments #
        // #################################

        double seg_rows = 4.0; //
        double seg_cols = 4.0; //
        // currently num_segs is hardcoded to be 16 segments, but will eventually be dynamic and dependent on mapsize
        
        double x_inc = m/seg_cols;
        double y_inc = m/seg_rows;

        double midpt_x_inc = x_inc/2.0;
        double midpt_y_inc = y_inc/2.0;

        double seg_x_Lbound = 0.0;
        double seg_x_Rbound = x_inc;
        double seg_y_Tbound = 0.0;
        double seg_y_Bbound = y_inc;

        int num_full_segs = 0;

        // *** NOTE: currently assuming that a hay bale cannot be on the perimeter of the grid!!!

        for (int seg_i=0; seg_i<seg_rows; seg_i++) {
            
            seg_x_Lbound = 0.0;
            seg_x_Rbound = x_inc;
            
            for (int seg_j=0; seg_j<seg_cols; seg_j++) {
                //System.out.println("num segs = " + num_full_segs + ", seg_i = " + seg_i + ", seg_j = " + seg_j); //

                if (!bales.isEmpty()) {

                    ArrayList<Point> pos_list = new ArrayList<Point>();

                    // ### Get the midpoint of the segment ###
                    double midpt_x = seg_x_Lbound + midpt_x_inc;
                    double midpt_y = seg_y_Tbound + midpt_y_inc;

                    midpoints.put(num_full_segs, new Point(midpt_x, midpt_y));

                    // put the bales positions in this segment into the corresponding position list
                    //System.out.println("x: " + seg_x_Lbound + " -> " + seg_x_Rbound + ", y: " + seg_y_Tbound + " -> " + seg_y_Bbound);
                    //System.out.println("midpt: (" + midpt_x + ", " + midpt_y + ")");    


                    for (Point pos : bales) {
                        if ((pos.x >= seg_x_Lbound) && (pos.x < seg_x_Rbound) && (pos.y >= seg_y_Tbound) && (pos.y < seg_y_Bbound)) {
                            // put this bale in the corresponding segment list
                            pos_list.add(pos);
                        }
                    }

                    segments.put(num_full_segs, pos_list);
                    num_full_segs++;

                    seg_x_Lbound = seg_x_Rbound;
                    seg_x_Rbound += x_inc;


                } else {
                    //System.out.println("bales is empty!"); //
                    // break out of for loop
                    break;
                }
            }
            seg_y_Tbound = seg_y_Bbound;
            seg_y_Bbound += y_inc;
        }

        /*int total_bales = 0; //
        for (int seg : segments.keySet()) {
            System.out.println("seg " + seg + " has " + segments.get(seg).size());
            total_bales += segments.get(seg).size();
            System.out.println("total num bales (at end) = " + total_bales);
        } //*/

        //System.out.println("Number of full segments: " + segments.size() + " (check: " + num_full_segs + ")");
        // if segments.size() and num_full_segs are not the same, then there are some segments that are not being filled with bales
        
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
