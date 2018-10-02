package sunshine.g6; 

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;

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

    public Player() {
        rand = new Random(seed);
    }
    
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = bales;
        mapsize = m * m;
        
    }
    
    public Command getCommand(Tractor tractor)
    {

        // #################################
        // # Divide the grid into segments #
        // #################################

        // currently this is hardcoded to be 16 segments, but will eventually be dynamic and dependent on mapsize

        // Get the min and max x and y coordinates for the bale positions
        System.out.println("Number of haybales: " + bales.size()); //

        double x_min = Double.POSITIVE_INFINITY;
        double x_max = -1;
        double y_min = Double.POSITIVE_INFINITY;
        double y_max = -1;

        for (Point pos : bales) {
            if (pos.x < x_min)
                x_min = pos.x;
            if (pos.x > x_max)
                x_max = pos.x;
            if (pos.y < y_min)
                y_min = pos.y;
            if (pos.y > y_max)
                y_max = pos.y;
        }
        System.out.println("x_min: " + x_min + ", y_min: " + y_min + ", x_max: " + x_max + ", y_max: " + y_max);



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
