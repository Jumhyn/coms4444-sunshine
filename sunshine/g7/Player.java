package sunshine.g7;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Comparator;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.Trailer;
import sunshine.sim.CommandType;
import sunshine.sim.Point;


public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    
    PriorityQueue<Point> near;
    List<PointClump> far;
    List<PointClump> currentClump;

    public Player() {
        rand = new Random(seed);
    }
    
    public void init(List<Point> bales, int n, double m, double t)
    {
    	this.near = new PriorityQueue<Point>(bales.size(),
    	    new Comparator(){
		       public int compare(Object o1, Object o2) {
		       	    Point p1 = (Point) o1;
		       	    Point p2 = (Point) o2;
		            return -(int)Math.signum(p1.x*p1.x + p1.y*p1.y - p2.x*p2.x - p2.y*p2.y);
		       }
        	} 
    	);
    	this.near.addAll(bales);

    	AbstractSplitter splitter = new CircleLineSplitter();
    	this.far = new ArrayList<PointClump>();
        this.currentClump = new ArrayList<PointClump>();

    	Point farthest = this.near.peek();
    	while (farthest != null && Math.hypot(farthest.x, farthest.y) > 300 /* Thanks Quincy! */ ) {
    		this.far.addAll(splitter.splitUpPoints(PointUtils.pollNElements(this.near, 11*n)));
    		farthest = this.near.peek();
    	}

    	System.out.println(this.near.size());
    	System.out.println(this.far.size());
    }
    
    public Command getCommand(Tractor tractor)
    {

        // If tractor is at the barn
        if (tractor.getLocation().equals(new Point(0.0, 0.0)))
        {

            if (tractor.getAttachedTrailer()!=null)
            {
                if (tractor.getAttachedTrailer().getNumBales()>0)
                {
                    return new Command(CommandType.DETATCH);
                }
                else
                {
                    for (PointClump i : currentClump){
                        if (i.tractor == tractor){
                            currentClump.remove(i);
                        }
                    }
                    PointClump next;
                    if (this.far.size() > 0)
                    {
                        next = this.far.get(0);
                        this.far.remove(next);
                        next.tractor = tractor;
                        next.trailer = tractor.getAttachedTrailer();
                        currentClump.add(next);
                        return Command.createMoveCommand(next.dropPoint);
                    }
                    else if(near.size() > 0)
                    {
                        Point p = near.poll();
                        return Command.createMoveCommand(p);
                    }
                    else
                    {
                        return null;
                    }
                }
            }
            else
            {
                if (tractor.getHasBale())
                {
                    return new Command(CommandType.UNLOAD);
                }
                else
                {
                    // get bale from trailer or attach to trailer for next Clump
                    return new Command(CommandType.STACK);
                }
            }
        }
        // tractor is away
        else
        {
            Trailer trailer = tractor.getAttachedTrailer();
            if (trailer != null)
            {
                // if trailer is empty, detach the trailer
                if (trailer.getNumBales()<10)
                {
                    System.out.println(trailer.getLocation().x+","+trailer.getLocation().y);
                    return new Command(CommandType.DETATCH);
                }
                // if trailer is full, send to barn
                else
                {
                    return Command.createMoveCommand(new Point(0.0, 0.0));
                }
            }
            // When not attached to trailer: 1. pick up bales in clump 2. attach to trailer
            else
            {
                PointClump clump=null;
                Point nextPoint;
                for (PointClump i : currentClump){
                    if (i.tractor == tractor){
                        clump = i;
                        break;
                    }
                }
                if (tractor.getHasBale())
                {
                    if (clump.trailer.getNumBales()>=10)
                    {
                        return new Command(CommandType.ATTACH);
                    }
                    else
                    {
                        if (clump.trailer.getLocation().x==tractor.getLocation().x && clump.trailer.getLocation().y==tractor.getLocation().y)
                        {    
                            return new Command(CommandType.UNSTACK);
                        }
                        else
                        {
                            return Command.createMoveCommand(clump.trailer.getLocation());
                        }
                    }
                }
                else
                {
                    if (tractor.getLocation().x == clump.get(0).x && tractor.getLocation().y == clump.get(0).y)
                    {       
                        clump.remove(0);
                        return new Command(CommandType.LOAD);
                    }
                    else
                    {
                        if (clump.size()>0)
                        {
                            nextPoint=clump.get(0);
                            return Command.createMoveCommand(nextPoint);
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
