package sunshine.g7;

import java.util.List;
import java.util.Collection;

import sunshine.sim.Point;

interface AbstractSplitter {
	public List<PointClump> splitUpPoints(Collection<? extends Point> points);
}