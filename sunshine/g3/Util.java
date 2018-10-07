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

    public static Point centroid(List<Point> pointList)
    {
        Double x = 0;
        Double y = 0;
        Integer n = 0;

        for (Point p: pointList)
        {
            x += p.x;
            y += p.y;
            n += 1;
        }

        return new Point(x/n, y/n);
    }

    public static Point weiszfeld(List<Point> pointList)
    {
        Point y_prev = centroid(pointList);
        Point y_next = new Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

        Double improvement = distance(y_prev, y_next);

        // TODO may need to adjust cutoff 
        while (improvement > 1)
        {
            Point sum_weighted_x = 0;
            Double scale = 0.0;
            for (Point x: pointList)
            {
                Double raw = distance(x, y);
                xy_dist += distance(x, y);
            }
        }
        return null;
    }
}
