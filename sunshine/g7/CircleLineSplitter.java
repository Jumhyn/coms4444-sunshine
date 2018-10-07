package sunshine.g7;

import java.util.Comparator;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;

import sunshine.sim.Point;

class CircleLineSplitter implements AbstractSplitter {

	public List<PointClump> splitUpPoints(Collection<? extends Point> points) {
		List<PointClump> ret = new ArrayList<PointClump>(points.size()/11);

		PriorityQueue<Point> bySlope = PointUtils.reorderElements(points, 
			new Comparator() {
				public int compare(Object o1, Object o2) {
					Point p1 = (Point) o1;
	       	    	Point p2 = (Point) o2;

	       	    	return (int)Math.signum(p2.y/p2.x - p1.y/p1.x);
	       	    }
			}
		);

    	while(bySlope.size() >= 11) {
    		ret.add(new PointClump(PointUtils.pollNElements(bySlope, 11)));
    	}

    	return ret;
	}
}