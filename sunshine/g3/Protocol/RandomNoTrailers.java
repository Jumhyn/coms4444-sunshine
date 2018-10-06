package sunshine.g3.Protocol;

import java.util.List;
import java.util.Random;
import java.util.HashMap;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import sunshine.sim.Trailer;


public class RandomNoTrailers {
    public static Command getCommand(Random rand,
                                     Tractor tractor,
                                     List<Point> bales,
                                     HashMap<Integer, Trailer> pairs,
                                     HashMap<Integer, Point> preemptive)
    {
        Integer Id = tractor.getId();
        Point origin = new Point(0.0, 0.0);
        Point nullPoint = new Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Point tracLoc = tractor.getLocation();

        if (!preemptive.containsKey(Id)) 
        {
            preemptive.put(Id, nullPoint);
        }
    
        Boolean hb = tractor.getHasBale();
        Boolean atOrigin = tracLoc.equals(origin);
        Boolean attached = tractor.getAttachedTrailer() != null;
        Boolean havePreemptive = !preemptive.get(Id).equals(nullPoint);
        Boolean areBalesRem = bales.size() > 0;
        areBalesRem = areBalesRem || havePreemptive;

        // TODO: (attached)
        if (attached)
        {
            return new Command(CommandType.DETATCH);
        }
        else if (!hb && atOrigin && areBalesRem)
        {
            Point p = bales.remove(rand.nextInt(bales.size()));
            preemptive.put(Id, p);
            return Command.createMoveCommand(p);
        }
        else if (!hb && !atOrigin)
        {
            preemptive.put(Id, nullPoint);
            return new Command(CommandType.LOAD);
        }
        else if (hb && !atOrigin)
        {
            return Command.createMoveCommand(origin);
        }
        else
        {
            return new Command(CommandType.UNLOAD);
        }
    }
}





