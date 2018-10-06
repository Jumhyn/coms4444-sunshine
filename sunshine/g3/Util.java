package sunshine.g3;

import java.util.List;

import sunshine.sim.Point;


public class Util {
    public static Double distance(Point a, Point b)
    {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    public static Point nearestPoint(Point point, List<Point> pointList) 
    {
        Point nearestPoint = new Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Double nearestDist = Double.POSITIVE_INFINITY;

        for (Point p: pointList) 
        {
            Double candidate = distance(p, point);
            if (candidate < nearestDist)
            {
                nearestPoint = p;
                nearestDist = candidate;
            }
        }
        return nearestPoint;
    }
}
