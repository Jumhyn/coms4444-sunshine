package sunshine.random;

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

    public Player() {
        rand = new Random(seed);
    }
    
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = bales;
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
