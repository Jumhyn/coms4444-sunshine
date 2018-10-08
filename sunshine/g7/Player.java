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
import sunshine.sim.CommandType;
import sunshine.sim.Point;


public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    
    PriorityQueue<Point> near;
    List<PointClump> far;

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
                // if (rand.nextDouble() > 0.5)
                // {
                //     if (tractor.getAttachedTrailer() == null)
                //     {
                //         return new Command(CommandType.ATTACH);
                //     }
                //     else
                //     {
                //         return new Command(CommandType.DETATCH);
                //     }
                // }
                // else 
                if (tractor.getAttachedTrailer()!=null){
                	return new Command(CommandType.DETATCH);
                }
                else
                if (near.size() > 0)
                {
                    //Point p = bales.remove(rand.nextInt(bales.size()));
                    Point p = near.poll();
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
