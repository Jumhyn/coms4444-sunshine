package sunshine.g3;

import java.util.List;
import java.util.ArrayList;

import sunshine.sim.Point;


public class Util {

    private static Point origin = new Point(0.0, 0.0);

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

    public static List<Double> getWeightList(List<Point> pointList)
    {
        List<Double> weightList = new ArrayList<Double>();
        for (Point p: pointList)
        {
            weightList.add(1.0);
        }

        return weightList;
    }

    public static Point centroid(List<Point> pointList, List<Double> weightList)
    {
        assert pointList.size() == weightList.size() : "Point list and weight list not of equal length.";

        Double x = 0.0;
        Double y = 0.0;
        Double n = 0.0;

        for (int i = 0; i < pointList.size(); i++)
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
        pointListOrigin.add(origin);

        List<Double> weightListOrigin;
        weightListOrigin = getWeightList(pointList);
        weightListOrigin.add(2.5);

        Point centroidOrigin = centroid(pointListOrigin, weightListOrigin);
        return centroidOrigin;
    }

    
    public static Point weiszfeld(List<Point> pointList)
    {
        List<Double> weightList = getWeightList(pointList); 
        Point med_prev = centroid(pointList, weightList);
        Point med_next = new Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

        Double improvement = distance(med_prev, med_next);

        // TODO may need to adjust cutoff 
        while (improvement > 1)
        {
            Double weighted_x = 0.0;
            Double weighted_y = 0.0;
            
            Double scale = 0.0;
            for (Point x: pointList)
            {
                Double raw = distance(x, med_prev);
                weighted_x += x.x / raw;
                weighted_y += x.y / raw;
                scale += 1 / raw;
            }

            med_next = new Point(weighted_x / scale, weighted_y / scale);
            improvement = distance(med_prev, med_next);
            med_prev = med_next;
        }
        return med_next;
    }
}
