package sunshine.g3;

import java.util.List;
import java.util.Collections;
import java.util.Random;
import java.util.HashMap;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import sunshine.sim.Trailer;

import sunshine.g3.Protocol.RandomNoTrailers;
import sunshine.g3.Protocol.RandomAllTrailers;


public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    
    List<Point> bales;

    ////////// Custom Variables //////////
    HashMap<Integer, Trailer> pairs = new HashMap<Integer, Trailer>();

    HashMap<Integer, Point> preemptive = new HashMap<Integer, Point>();
    ////////// End Custom Variables ////////// 

    ////////// Custom Functions ////////// 
    private static Double relativeDist(Point a, Point b)
    {
        return Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2);
    }

    private static Point nearestPoint(Point point, List<Point> pointList) 
    {
        Point nearestPoint = new Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Double nearestDist = Double.POSITIVE_INFINITY;

        for (Point p: pointList) 
        {
            Double candidate = relativeDist(p, point);
            if (candidate < nearestDist)
            {
                nearestPoint = p;
                nearestDist = candidate;
            }
        }
        return nearestPoint;
    }
    ////////// End Custom Functions ////////// 

    public Player() {
        rand = new Random(seed);
    }
    
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = bales;
    }

    
    public Command getCommand(Tractor tractor)
    {
        return RandomNoTrailers.getCommand(rand, tractor, bales, pairs, preemptive);
        return RandomAllTrailers.getCommand(rand, tractor, bales, pairs, preemptive);
    }
}
/*
    {
        Integer Id = tractor.getId();
        Point origin = new Point(0.0, 0.0);
        Point nullPoint = new Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Point tracLoc = tractor.getLocation();
        Trailer trailer;
        Point trailerLoc;

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
        Boolean atOrigin = tracLoc.equals(origin);
        Boolean attached = tractor.getAttachedTrailer() != null;
        Boolean atTrailer = tracLoc.equals(trailerLoc);
        Boolean atPreemptive = tracLoc.equals(preemptive.get(Id));
        Boolean havePreemptive = !preemptive.get(Id).equals(nullPoint);
        Integer numBales = trailer.getNumBales();
        Boolean areBalesRem = bales.size() > 0;
        areBalesRem = areBalesRem || havePreemptive;

        // TODO: (attached)
        if (!hb && atOrigin && attached && numBales == 0 && areBalesRem)
        {
          // TODO: random
          Point p = bales.remove(rand.nextInt(bales.size()));
          preemptive.put(Id, p);
          return Command.createMoveCommand(p);
        }
        else if (!hb && !atOrigin && attached && areBalesRem)
        {
            pairs.put(Id, trailer);

            return new Command(CommandType.DETATCH);
        }
        else if (!hb && !atOrigin && !attached && atPreemptive)
        {
            System.out.println("stuck?");
            preemptive.put(Id, nullPoint);
            return new Command(CommandType.LOAD);

        }
        else if (!hb && !atOrigin && !attached && areBalesRem)
        {
            // TODO: random
            Point p = bales.remove(rand.nextInt(bales.size()));
            preemptive.put(Id, p);
            return Command.createMoveCommand(p);
        }
        else if (hb && !atOrigin && !attached && !atTrailer)
        {
            return Command.createMoveCommand(trailerLoc);
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
            return Command.createMoveCommand(origin);
        }
        else if (atOrigin && attached && numBales > 0)
        {
            System.out.println("stuck?");
            return new Command(CommandType.DETATCH);
        }
        else if (hb && atOrigin)
        {
            return new Command(CommandType.UNLOAD);
        }
        else if (!hb && atOrigin && numBales > 0)
        {
            System.out.println(numBales);
            return new Command(CommandType.UNSTACK);
        }
        // TODO: shouldn't always be detached
        else if (!hb && atOrigin && !attached && numBales == 0) 
        {
            return new Command(CommandType.ATTACH);
        }
        else if (!atOrigin && numBales == 0 && !areBalesRem)
        {
            return Command.createMoveCommand(origin);
        }
        else if (!atOrigin && !atTrailer && numBales > 0 && !areBalesRem)
        {
            return Command.createMoveCommand(trailerLoc);
        }
        else if (!atOrigin && !areBalesRem && !attached && atTrailer)
        {
            return new Command(CommandType.ATTACH);
        }
        else if (!atOrigin && attached && !areBalesRem)
        {
            return Command.createMoveCommand(origin);
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
*/
