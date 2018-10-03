package sunshine.g2;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;

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

    HashMap<Integer, Trailer> pairs = new HashMap<Integer, Trailer>();
    HashMap<Integer, Point> pairs_locs = new HashMap<Integer, Point>();

    List<Trailer> trailers;
    List<Point> trailerLocs;
    Point preemptive;

    private static double relativeDist(Point a, Point b)
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

    //private static Trailer nearestTrailer(Tractor tractor, List<Trailer> trailerList)
    //{
    //    Trailer nearestTrailer;
    //    List<Point> pointList;
    //}


    public Player() {
        rand = new Random(seed);
    }
    
    public void init(List<Point> bales, int n, double m, double t)

    {
        //for (Point b : bales) {
        //    System.out.println(String.valueOf(b.x) + " " + String.valueOf(b.y));
        //}
        this.bales = bales;
    }

    
    public Command getCommand(Tractor tractor)
    {
        Integer Id = tractor.getId();
        Point origin = new Point(0.0, 0.0);
        Point tracLoc = tractor.getLocation();
        Trailer trailer;
        Point trailerLoc;

        if (tractor.getAttachedTrailer() == null)
        {
            trailer = pairs.get(Id);
            trailerLoc = trailer.getLocation();
            // Test
            trailerLoc = pairs_locs.get(Id);
        }
        else
        {
            trailer = tractor.getAttachedTrailer();
            trailerLoc = trailer.getLocation();
            // Test
            trailerLoc = tractor.getLocation();
        }

        //Boolean hb = tractor.getHasBale();
        //Boolean origin = tracLoc.equals(origin);
        //Boolean attached = tractor.getAttachedTrailer() != null;
        //Boolean atTrailer = tracLoc.x == trailerLoc.x && tracLoc.y == trailerLoc.y;
        //Integer numBales = 
        

        // hb
        if (tractor.getHasBale())
        {
            System.out.println("has bale");
            // origin 
            if (tracLoc.equals(origin))
            {
                return new Command(CommandType.UNLOAD);
            }
            // not origin
            else
            {
                // attached
                if (tractor.getAttachedTrailer() != null)
                {
                    System.out.println("GOING HOME");
                    return Command.createMoveCommand(origin);
                }
                // detached
                else 
                {
                    // trailer loc
                    if (trailerLoc.equals(tracLoc))
                    {
                        if (trailer.getNumBales() != 10)
                        {
                            return new Command(CommandType.STACK);
                        }
                        else
                        {
                            return new Command(CommandType.ATTACH);
                        }
                    }
                    // not trailer loc
                    else
                    {
                        System.out.println("GOING HOME2");
                        return Command.createMoveCommand(trailerLoc);
                    }
                }
            }
        }
        // if not hb
        else
        {
            // if origin
            if (tracLoc.equals(origin))
            {
                Integer numBales = trailer.getNumBales();

                // attached
                if (tractor.getAttachedTrailer() != null)
                {
                    // trailer full
                    if (numBales == 10)
                    {
                        Point preemptive;
                        return new Command(CommandType.DETATCH);
                    }
                    else
                    {
                        // if more hb's on field
                        if (bales.size() > 0)
                        {
                            pairs.put(Id, trailer);
                            pairs_locs.put(Id, trailer.getLocation());

                            // PREEMPTIVE REMOVAL
                            Point p = bales.remove(rand.nextInt(bales.size()));
                            preemptive = p;
                            //System.out.println(p);
                            System.out.println("GOING HOME3");
                            return Command.createMoveCommand(p);
                        }
                        // if no more hb's on field
                        else
                        {
                            return null;
                        }
                    }
                }
                // detached
                else
                {
                    // trailer "full"
                    if (numBales > 0)
                    {
                        return new Command(CommandType.UNSTACK);
                    }
                    else
                    {
                        return new Command(CommandType.ATTACH);
                    }
                }
            }
            // if not origin
            else
            {
                // if attached
                if (tractor.getAttachedTrailer() != null)
                {
                    //trailers.add(tractor.getAttachedTrailer());
                    //trailerLocs.add(tracLoc); 
                    pairs.put(Id, tractor.getAttachedTrailer());
                    pairs_locs.put(Id, tractor.getLocation());
                    return new Command(CommandType.DETATCH);
                }
                // if detached 
                else
                {
                    // if hb loc
                    if (preemptive.equals(tracLoc))
                    {
                        System.out.println("another load");
                        System.out.println(preemptive);
                        preemptive = origin;
                        return new Command(CommandType.LOAD);
                    }
                    else
                    {
                        if (bales.size() > 0)
                        {
                            System.out.println("moving to another");
                            Point p = bales.remove(rand.nextInt(bales.size()));
                            preemptive = p;
                            System.out.println(p);
                            return Command.createMoveCommand(p);
                        }
                        else
                        {
                            return null;
                        }
                    }
                }
            }
        }
    }
}
