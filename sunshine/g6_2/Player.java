package sunshine.g6_2; 

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

import sunshine.sim.Trailer;


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

    public double dist(double x1,double y1,double x2,double y2){
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }
    
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = bales;
        //System.out.println("total num_bales = " + bales.size());
        mapsize = m * m;


        // #######################################################
        // # Split bale points according to distance from origin #
        // #######################################################
        // for (int i=0; i<bales.size(); i++){
        //     if (dist(bales.get(i).x,bales.get(i).y,0,0) > 600){
        //         farBales.add(bales.get(i));
        //     }
        //     else{
        //         closeBales.add(bales.get(i));
        //     }
        // }

        // #################################
        // # Divide the grid into segments #
        // #################################

        // segments will map the index of each segment to the list of all bale points in that segment
        // segments are numbered along each row first, before moving to the next row
        segments = new HashMap<Integer, ArrayList<Point>>();
        // midpoints will map the index of each segment to a Point object containing the midpoint of that segment
        midpoints = new HashMap<Integer, Point>();

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
    
    // ######################################
    // helper function to generate random int
    // ######################################
    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    // public Command getCommand(Tractor tractor)
    // {
    //     System.out.println("tractor has bale? " + tractor.getHasBale());
        
    //     // if tractor is at (0, 0)
    //     if (tractor.getLocation().equals(new Point(0.0, 0.0)))
    //     {
    //         if (tractor.getAttachedTrailer() == null)
    //         {
    //             return new Command(CommandType.ATTACH);
    //         }
    //         else if (tractor.getHasBale())
    //         {
    //             return new Command(CommandType.UNLOAD);
    //         }
    //         else 
    //         {
    //             //head towards a random segment
    //             Integer randomInt = getRandomNumberInRange(0, midpoints.size() - 1);
    //             return Command.createMoveCommand(midpoints.get(randomInt));
    //         }
    //     }
    //     if (tractor.getAttachedTrailer() != null) 
    //     {
    //         Trailer trailer = tractor.getAttachedTrailer();

    //         // System.out.println("number of bales:" + trailer.getNumBales());

    //         // if trailer size > 0, go to (0.0, 0.0)
    //         if (trailer.getNumBales() == 10) {
    //             return Command.createMoveCommand(new Point(0.0, 0.0));
    //         }
            
    //         return new Command(CommandType.DETATCH);
    //     }
    //     if (tractor.getHasBale())
    //     {
    //         if (tractor.getLocation().equals(new Point(0.0, 0.0)))
    //         {
    //             return new Command(CommandType.UNLOAD);
    //         }
    //         else
    //         {
    //             // find closest trailer
    //             Point currentLocation = tractor.getLocation();
    //             double maxDistance = Double.POSITIVE_INFINITY;
    //             Point desiredLocation = new Point(0.0, 0.0);
                
    //             System.out.println("VALUES:" + midpoints.values());

    //             for (Point midpoint : midpoints.values())
    //             {
    //                 double distance = Math.hypot(midpoint.x - currentLocation.x, midpoint.y - currentLocation.y);
                    
    //                 if (distance == 0 || distance == 1)
    //                 {
    //                     // if size of trailer < 10, STACK
    //                     return new Command(CommandType.STACK);
    //                     // else ATTACH
    //                 }
    //                 if (distance < maxDistance)
    //                 {
    //                     maxDistance = distance;
    //                     desiredLocation = midpoint;
    //                 }
    //             }
    //             return Command.createMoveCommand(desiredLocation);  
    //         }  
    //     }
    //     else
    //     {
    //         return new Command(CommandType.LOAD);
    // }

        // #############################
        // BRUTE FORCE GETCOMMAND METHOD
        // #############################
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
                    if (tractor.getAttachedTrailer() != null)
                    {
                        return new Command(CommandType.DETATCH);
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

        // ORIGINAL GETCOMMAND METHOD
        // public Command getCommand(Tractor tractor)
        // {
        //     if (tractor.getHasBale())
        //     {
        //         if (tractor.getLocation().equals(new Point(0.0, 0.0)))
        //         {
        //             return new Command(CommandType.UNLOAD);
        //         }
        //         else
        //         {
        //             return Command.createMoveCommand(new Point(0.0, 0.0));
        //         }
        //     }
        //     else
        //     {
        //         if (tractor.getLocation().equals(new Point(0.0, 0.0)))
        //         {
        //             if (rand.nextDouble() > 0.5)
        //             {
        //                 if (tractor.getAttachedTrailer() == null)
        //                 {
        //                     return new Command(CommandType.ATTACH);
        //                 }
        //                 else
        //                 {
        //                     return new Command(CommandType.DETATCH);
        //                 }
        //             }
        //             else if (bales.size() > 0)
        //             {
        //                 Point p = bales.remove(rand.nextInt(bales.size()));
        //                 return Command.createMoveCommand(p);
        //             }
        //             else
        //             {
        //                 return null;
        //             }
        //         }
        //         else
        //         {
        //             return new Command(CommandType.LOAD);
        //         }
        //     }
        // }
    // }
}
