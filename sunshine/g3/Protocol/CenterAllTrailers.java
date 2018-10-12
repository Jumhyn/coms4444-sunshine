package sunshine.g3.Protocol;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import sunshine.sim.Trailer;

import sunshine.g3.Util;
import sunshine.g3.Util.*;

public class CenterAllTrailers {
    public static Command getCommand(Random rand,
                                     Tractor tractor,
                                     List<Point> bales,
                                     HashMap<Integer, Trailer> pairs,
                                     HashMap<Integer, Point> preemptive,
                                     HashMap<Integer, BalesProtocol> balesAssignments)
    {
        Integer Id = tractor.getId();
        Point origin = new Point(0.0, 0.0);
        Point originT = Util.trailerOrigin(Id);
        Point nullPoint = new Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Point tracLoc = tractor.getLocation();
        Trailer trailer;
        Point trailerLoc;

        List<Point> assignedBales = balesAssignments.get(Id).balesLocations;
        List<Point> allBales = new ArrayList<Point>();
        allBales.addAll(assignedBales);
        allBales.addAll(bales);
   
        if (tractor.getAttachedTrailer() == null)
        {
            trailer = pairs.get(Id);
            trailerLoc = trailer.getLocation();
        }
        else
        {
            trailer = tractor.getAttachedTrailer();
            trailerLoc = trailer.getLocation();
        }
    
        if (!preemptive.containsKey(Id)) 
        {
            preemptive.put(Id, nullPoint);
        }
    
        Boolean hb = tractor.getHasBale();
        //Boolean atOrigin = tracLoc.equals(origin);
        //Boolean atOrigin = tracLoc.equals(origin) || tracLoc.equals(originT);
        Boolean atOrigin = Util.distance(tracLoc, origin) <= 1;
        Boolean attached = tractor.getAttachedTrailer() != null;
        Boolean atTrailer = tracLoc.equals(trailerLoc);
        //Boolean atTrailer = Util.distance(tracLoc, trailerLoc) <= 1;
        //Boolean atPreemptive = tracLoc.equals(preemptive.get(Id));
        Boolean atPreemptive = Util.distance(tracLoc, preemptive.get(Id)) <= 1;
        Boolean havePreemptive = !preemptive.get(Id).equals(nullPoint);
        Integer numBales = trailer.getNumBales();
        Boolean areBalesRem = assignedBales.size() > 0;
        areBalesRem = areBalesRem || havePreemptive;
    
        // TODO: (attached)
        if (!hb && atOrigin && attached && numBales == 0 && areBalesRem)
        {
            //Point p = Util.centroidTrailer(assignedBales);
            Point p = Util.weiszfeldTrailer(assignedBales);
            return Command.createMoveCommand(p);
        }
        else if (!hb && !atOrigin && attached && areBalesRem)
        {
            pairs.put(Id, trailer);
    
            return new Command(CommandType.DETATCH);
        }
        else if (!hb && !atOrigin && !attached && atPreemptive)
        {
            preemptive.put(Id, nullPoint);
            return new Command(CommandType.LOAD);
        }
        else if (!hb && !atOrigin && !attached && areBalesRem)
        {
            Point p = Util.furthestPoint(assignedBales);
            assignedBales.remove(p);
            preemptive.put(Id, p);
            //return Command.createMoveCommand(p);
            System.out.println("DISTANCE:\t" + Double.toString(Util.distance(p, tracLoc)));
            System.out.println("DISTANCE SAVED:\t" + Double.toString(Util.distance(p, Util.shortcut(tracLoc, p, allBales))));
            return Command.createMoveCommand(Util.shortcut(tracLoc, p, allBales));
        }
        else if (hb && !atOrigin && !attached && !atTrailer)
        {
            return Command.createMoveCommand(trailerLoc);
            //return Command.createMoveCommand(Util.shortcut(tracLoc, trailerLoc, allBales));
        }
        else if (hb && !atOrigin && !attached && atTrailer && numBales < 10)
        {
            return new Command(CommandType.STACK);
        }
        else if (hb && !atOrigin && !attached && atTrailer && numBales == 10)
        {
            return new Command(CommandType.ATTACH);
        }
        else if (hb && !atOrigin && attached)
        {
            //return Command.createMoveCommand(origin);
            return Command.createMoveCommand(originT);
        }
        else if (atOrigin && attached && numBales > 0)
        {
            return new Command(CommandType.DETATCH);
        }
        else if (hb && atOrigin)
        {
            if (numBales == 0)
            {
                BalesProtocol next = new BalesProtocol(assignedBales, -1);
                balesAssignments.put(Id, next);
            } 
            return new Command(CommandType.UNLOAD);
        }
        else if (!hb && atOrigin && numBales > 0)
        {
            //BalesProtocol next = new BalesProtocol(assignedBales, -1);
            //balesAssignments.put(Id, next);

            return new Command(CommandType.UNSTACK);
        }
        else if (!hb && atOrigin && !attached && numBales == 0 && assignedBales.size() == 0) 
        {
            //System.out.println("COMMAND: TRACTOR " + Integer.toString(Id) + " ATTACHING_NEEDLESSLY?");
            //BalesProtocol next = new BalesProtocol(assignedBales, -1);
            //balesAssignments.put(Id, next);

            //return Command.createMoveCommand(origin);
            return Command.createMoveCommand(originT);
            //return new Command(CommandType.ATTACH);
        }
        // TODO: shouldn't always be detached
        else if (!hb && atOrigin && !attached && numBales == 0) 
        {
            return new Command(CommandType.ATTACH);
        }
        else if (!atOrigin && numBales == 0 && !areBalesRem)
        {
            //return Command.createMoveCommand(origin);
            return Command.createMoveCommand(originT);
        }
        else if (!atOrigin && !atTrailer && numBales > 0 && !areBalesRem)
        {
            return Command.createMoveCommand(trailerLoc);
            //return Command.createMoveCommand(Util.shortcut(tracLoc, trailerLoc, allBales));
        }
        else if (!atOrigin && !areBalesRem && !attached && atTrailer)
        {
            if (numBales < 10)
            {
                Util.printCommand(Id, "GOING_HOME_FEWER_THAN_10");
            }
            return new Command(CommandType.ATTACH);
        }
        else if (!atOrigin && attached && !areBalesRem)
        {
            //return Command.createMoveCommand(origin);
            return Command.createMoveCommand(originT);
        }
        else if (atOrigin && attached && numBales > 0)
        {
            return new Command(CommandType.DETATCH);
        }
        else
        {
            return null;
        }
    }
}
