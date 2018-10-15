package sunshine.g3.Protocol;

import java.util.List;
import java.util.Random;
import java.util.HashMap;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import sunshine.sim.Trailer;

import sunshine.g3.Util;
import sunshine.g3.Util.*;


public class NoTrailers {
    public static Command getCommand(Random rand,
                                     Tractor tractor,
                                     List<Point> bales,
                                     HashMap<Integer, Trailer> pairs,
                                     HashMap<Integer, Point> preemptive,
                                     HashMap<Integer, BalesProtocol> balesAssignments)
    {
        Integer Id = tractor.getId();
        Point origin = new Point(0.0, 0.0);
        Point nullPoint = new Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Point tracLoc = tractor.getLocation();

        //System.out.println(bales.size());

        List<Point> assignedBales = balesAssignments.get(Id).balesLocations;

        //if (balesAssignments.containsKey(Id))
        //{
        //    assignedBales = balesAssignments.get(Id).bales;
        //}
        //else
        //{
        //    assignedBales = bales;
        //}

        if (!preemptive.containsKey(Id)) 
        {
            preemptive.put(Id, nullPoint);
        }
    
        Boolean hb = tractor.getHasBale();
        Boolean atOrigin = tracLoc.equals(origin);
        Boolean attached = tractor.getAttachedTrailer() != null;
        Boolean havePreemptive = !preemptive.get(Id).equals(nullPoint);
        Boolean areBalesRem = assignedBales.size() > 0;
        areBalesRem = areBalesRem || havePreemptive;

        // TODO: (attached)
        if (attached)
        {
            return new Command(CommandType.DETATCH);
        }
        else if (!hb && atOrigin && areBalesRem)
        {
            Point p = Util.furthestPoint(bales);
            bales.remove(p);
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





