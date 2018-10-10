package sunshine.g7;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.LinkedList;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.Trailer;
import sunshine.sim.CommandType;
import sunshine.sim.Point;


public class Player extends sunshine.queuerandom.QueuePlayer {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    
    PriorityQueue<Point> near;
    LinkedList<PointClump> far;
    //List<PointClump> currentClump;

    public Player() {
        rand = new Random(seed);
    }
    
    @Override
    public void init(List<Point> bales, int n, double m, double t)
    {
    	super.init(bales, n, m, t);

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
    	this.far = new LinkedList<PointClump>();
        //this.currentClump = new ArrayList<PointClump>();

    	Point farthest = this.near.peek();
    	while (farthest != null && Math.hypot(farthest.x, farthest.y) > 300 /* Thanks Quincy! */ ) {
    		int nTracker = PointUtils.numTracker(farthest, m, bales.size());
    		System.err.println(nTracker);
    		this.far.addAll(splitter.splitUpPoints(PointUtils.pollNElements(this.near, 11*nTracker)));
    		farthest = this.near.peek();
    	}

    	//System.out.println(this.near.size());
    	//System.out.println(this.far.size());
    }
    

    public ArrayList<Command> getMoreCommands(Tractor tractor)
    {
    	Task ret = new Task();
    	if(far.size() > 0) {
    		PointClump first = far.pollFirst();
    		if(tractor.getAttachedTrailer() == null) {
    			ret.add(new Command(CommandType.ATTACH));
    		}
    		ret.addTrailerPickup(first);
    	} else if(near.size() > 0) {
    		if(tractor.getAttachedTrailer() != null) {
    			ret.add(new Command(CommandType.DETATCH));
    		}
    		ret.addRetrieveBale(near.poll());
    	} else {
    		ret.add(new Command(CommandType.ATTACH));
    	}

    	return ret;
    }
}
