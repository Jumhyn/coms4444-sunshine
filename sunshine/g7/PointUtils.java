package sunshine.g7;

import java.util.Queue;
import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;

import sunshine.sim.Point;

class PointUtils {

	public static PriorityQueue<Point> reorderElements(Collection<? extends Point> points, Comparator comp) {
		PriorityQueue<Point> ret = new PriorityQueue<Point>(points.size(), comp);
		ret.addAll(points);
		return ret;
	}

	public static List<Point> pollNElements(PriorityQueue<? extends Point> points, int n) {
		if(points.size() < n) {
			throw new IllegalArgumentException("Too few elements in given queue");
		}

		List<Point> ret = new ArrayList<Point>(n);
		for(int i = 0; i < n; ++i) {
			ret.add(points.poll());
		}

		return ret;
	}
}