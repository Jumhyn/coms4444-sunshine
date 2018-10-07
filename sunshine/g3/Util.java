package sunshine.g3;

import java.util.List;
import java.util.ArrayList;

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

    public static Point centroid(List<Point> pointList, List<Double> weightList)
    {
        assert pointList.getSize() == weightList.getSize() : "Point list and weight list not of equal length.";

        Double x = 0.0;
        Double y = 0.0;
        Double n = 0.0;

        for (int i = 0; pointList.getSize(); i++)
        {
            Point p = pointList.get(i);
            Double w = weightList.get(i);
    
            x += p.x * w;
            y += p.y * w;
            n += w;
        }

        return new Point(x/n, y/n);
    }

    public static Point centroidTrailer(List<Point> pointList)
    {
        List<Point> pointListOrigin = new ArrayList<Point>(pointList);
        pointListOrigin.add(new Point(0.0, 0.0));

        List<Double> weightListOrigin;
        for (Point p: pointList)
        {
            weightListOrigin.add(1.0);
        }
        weightListOrigin.add(2.5);

        Point centroidOrigin = centroid(pointListOrigin, weightListOrigin);
        return centroidOrigin;
    }

    /*
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
    */
}
