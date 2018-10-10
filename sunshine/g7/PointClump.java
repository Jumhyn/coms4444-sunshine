package sunshine.g7;

import java.util.Collection;
import java.util.ArrayList;
import java.lang.Math;
import java.lang.Double;

import sunshine.sim.Point;
import sunshine.sim.Tractor;
import sunshine.sim.Trailer;
import sunshine.sim.Point;

/*
TODO: implement function that checks if it is worth it to use a trailer for this clump
*/

class PointClump extends ArrayList<Point> {

    public Point dropPoint;
    public boolean barnClump;
    // public Tractor tractor;
    // public Trailer trailer;

    private double accuracy = 1;
    
    public PointClump(Collection<? extends Point> c) {

	super(c);

	//	System.out.println("in constructor");
	
	setDropPoint();

	double trailerCost = trailerTime();
	double tractorCost = tractorTime();
	barnClump = ( trailerCost > tractorCost );

	System.out.println("tractor cost: " + tractorCost + "\t trailer cost: " + trailerCost + "\t barnClump: " + barnClump);

	    
    }
    
    public PointClump() {
	super();
	barnClump = false;
	
    }

    public double tractorTime() {
	double total = 0;
	for ( Point bale : this ) {
	    total += Math.sqrt( bale.x*bale.x + bale.y*bale.y );
	}
	return total * 2; //all distances must be traveled twice
    }

    public double trailerTime() {
	double total = distanceSum(dropPoint) * 2; //all distaces must be traveled twice
	total += 10*4*60; //unhitching/hitching that is added as opportunity cost in meters
	total += 10*20*10; //stacking/unstacking added as opportunity cost
	return total;
    }
    
    public Point centroid() {
	double x = 0;
	double y = 0;
	for ( Point p : this ) {
	    x += p.x;
	    y += p.y;
	}
	x = x / (super.size() + 1);
	y = y / (super.size() + 1);

	//	System.out.println("centriod is: " + x + ", " + y);

	return new Point(x, y);
    }

    public double distanceSum(Point center) {
	double total = 0;
	for ( Point p : this ) {
	    double Xdist = Math.abs(center.x - p.x);
	    double Ydist = Math.abs(center.y - p.y);

	    total += Math.sqrt( Xdist*Xdist + Ydist*Ydist );
	}

	total += 2.5*( Math.sqrt( center.x*center.x + center.y*center.y ));
	//distance from origin costs more
	
	return total;
    }
    
    public Point geometricMean() {
	//Uses Weiszfeld's Algorithm to approximate geometric mean

	boolean decreaseStep = false;
	double stepSize = 100;
	Point est = centroid();
	//System.out.println("dropPoint is: " + est.x + ", " + est.y);

	
	double minCost = distanceSum(est);

	//	System.out.println("cost is " + minCost);

	while ( stepSize > accuracy ) {

	    decreaseStep = true;

	    ArrayList<Point> neighbors = new ArrayList<Point>();
	    neighbors.add( new Point(est.x, est.y+stepSize) );
	    neighbors.add( new Point(est.x, est.y-stepSize) );
	    neighbors.add( new Point(est.x-stepSize, est.y) );
	    neighbors.add( new Point(est.x+stepSize, est.y) );

	    for (Point n : neighbors) {

		//System.out.println("neighbor is: " + n.x + ", " + n.y);
		
		double cost = distanceSum(n);

		//System.out.println("cpst is: " + cost);
		if (cost < minCost) {
		    //System.out.println("new mincost");
		    minCost = cost;
		    est = n;
		    decreaseStep = false;
		}
	    }

	    if ( decreaseStep ) {
		//System.out.println("decreasing step");
		stepSize = stepSize/2;
	    }
	    
	}

	return est;
    }
    
    public Point findCloset(Point geoMean){
    	Point ret=this.get(0);
    	double minDist=Double.MAX_VALUE;
    	for (Point i : this){
    		if (((geoMean.x-i.x)*(geoMean.x-i.x)+(geoMean.y-i.y)*(geoMean.y-i.y))<minDist)
    		{
    			minDist=(geoMean.x-i.x)*(geoMean.x-i.x)+(geoMean.y-i.y)*(geoMean.y-i.y);
    			ret = i;
    		}
    	}
    	return ret;
    }
    public void setDropPoint() {
	//System.out.println("setting drop point");
	dropPoint = geometricMean();
	dropPoint=findCloset(dropPoint);
	System.out.println("dropPoint is: " + dropPoint.x + ", " + dropPoint.y);
    }
    
    
}
