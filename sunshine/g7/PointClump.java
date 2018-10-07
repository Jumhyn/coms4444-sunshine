package sunshine.g7;

import java.util.Collection;
import java.util.ArrayList;

import sunshine.sim.Point;

class PointClump extends ArrayList<Point> {
	public PointClump(Collection<? extends Point> c) {
		super(c);
	}

	public PointClump() {
		super();
	}
}