package sunshine.g3;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;

import sunshine.sim.Point;
import sunshine.sim.Harvester;


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

    public static Double sumTimeTrailer(Point point, List<Point> pointList)
    {
        Double sumTime = 0.0;
        for (Point p: pointList)
        {
            Double t = distance(point, p) / 10;
            sumTime += t;
        }
        Double originTime = distance(origin, point);
        sumTime += originTime / 4;
        return sumTime;
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

    // TODO: weights (2.5 for origin)
    public static Point weiszfeldTrailer(List<Point> pointList)
    {
        //List<Double> weightList = getWeightList(pointList); 
        Point med_prev = new Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Point med_next = centroidTrailer(pointList);

        Double dist_prev = Double.POSITIVE_INFINITY;
        Double dist_next = sumTimeTrailer(med_next, pointList);

        // TODO - testing
        //System.out.println("Centroid:\t" + "(" + Double.toString(med_next.x) + "," + Double.toString(med_next.y) + ")");
        //System.out.println("Centroid Time:\t" + Double.toString(dist_next));

        // TODO may need to adjust cutoff 
        while (dist_prev - dist_next > 0.0001)
        {
            med_prev = med_next;
            Double weighted_x = 0.0;
            Double weighted_y = 0.0;
            
            Double scale = 0.0;
            for (Point p: pointList)
            {
                Double raw = distance(p, med_prev);
                weighted_x += p.x / raw;
                weighted_y += p.y / raw;
                scale += 1 / raw;
            }
            Double originRaw = distance(origin, med_prev);
            scale += 2.5 / originRaw;

            med_next = new Point(weighted_x / scale, weighted_y / scale);
            dist_prev = sumTimeTrailer(med_prev, pointList);
            dist_next = sumTimeTrailer(med_next, pointList);

            //System.out.println("Geometric Med:\t" + "(" + Double.toString(med_next.x) + "," + Double.toString(med_next.y) + ")");
            //System.out.println(dist_prev - dist_next);
        }
        //System.out.println("Weiszfeld:\t" + "(" + Double.toString(med_next.x) + "," + Double.toString(med_next.y) + ")");
        //System.out.println("Weiszfeld time:\t" + Double.toString(sumTimeTrailer(med_next, pointList)));

        return med_next;
    }

    // given 11 points, calculator time taken for tracker WITH trailer 
    // 10 m/s * distance + detach(60) + attach(60) + detach load, unload(10 * 2) 
    public static Double timeWithTrailer(List<Point> pointList) 
    {
        //Double time_0;
        Double time;
        //Point origin = new Point(0.0, 0.0);
        //Point centroid = Util.centroidTrailer(pointList);
        Point weiszfeld = Util.weiszfeldTrailer(pointList);
        //Point centroid = Util.centroidTrailer(pointList);
        //System.out.println("CENTROID:\t" + Double.toString(centroid.x) + "," + Double.toString(centroid.y));
        //System.out.println("WEISZFELD:\t" + Double.toString(weiszfeld.x) + "," + Double.toString(weiszfeld.y));
        // from origin to centroid
        //time_0 = 2 * Util.distance(centroid_0, origin)/4 + 60 * 3; // 2 * distance/speed + detach
        time = 2 * Util.distance(weiszfeld, origin)/4 + 60 * 3; // 2 * distance/speed + detach
        for (Point p : pointList) {
            // from centroid to point  
            //time_0 += 2 * Util.distance(p, centroid_0)/10; //
            //time_0 += 10 * 4; // load, stack, unstack, unload 
            time += 2 * Util.distance(p, weiszfeld)/10; //
            time += 10 * 4; // load, stack, unstack, unload 
        }
        //System.out.println("TIME FOR CENTROID:\t" + Double.toString(time_0));
        //System.out.println("TIME FOR WEISZFELD:\t" + Double.toString(time));
        //if (time > time_0)
        //{
        //    System.out.println("WOAH, CENTROID BEATS WEISZFELD");
        //}
        return time;
    }

    // given 11 points, calculator time taken for tracker WITHOUT trailer 
    // 4 m/s * distance + detach, attach, detach (60 * 3) + load, stack, unstack, unload (10 * 4) 
    public static Double timeWithoutTrailer(List<Point> pointList) 
    {
        Double time = 0.0;;
        //Point origin = new Point(0.0, 0.0);
        time += 60; // detach at origin
        for (Point p : pointList) {
            time += 2 * Util.distance(p, origin)/10;
            time += 10 * 2; // load, unload 
        }
        //System.out.println("TIME WITHOUT TRAILER:\t" + Double.toString(time));
        return time;
    }

    public static class PointIndex 
    {
        public Point point;
        public Integer index;

        public PointIndex(Point p, Integer i)
        {
            this.point = p;
            this.index = i;
        }
    }

	public static Point furthestPoint(List<Point> hb_locations)
    {
        Point furthest = origin;
		Double furthestDistance = 0.0;

        for (Point p: hb_locations)
        {
			Double candidate = distance(origin,p);

			if (candidate > furthestDistance) {
                furthest = p;
				furthestDistance = candidate;
			}
		}
		return furthest;
	}

    public static class TwoList
    {
        public List<Point> first;
        public List<Point> second;

        public TwoList(List<Point> f, List<Point> s)
        {
            this.first = f;
            this.second = s;
        }
    }

    public static TwoList nearestBales(Point p, List<Point> hb_locations, int n)
    {
    	//declaring hash map and point id integer variable to keep track of distances
    	Map<Integer,Point> pointId_point = new HashMap();
    	Map<Integer,Double> pointId_distance = new HashMap();

    	List<Point> nearestPoints = new ArrayList<Point>();
        List<Point> otherPoints = new ArrayList<Point>();

    	Integer id = new Integer(0);
    	for (Point hb_point: hb_locations)
        {
    		pointId_point.put(id,hb_point);
    		pointId_distance.put(id,distance(p,hb_point));
    		id ++;
    	}

		//sorting hash map by values
		Map<Integer, Double> sortedMap = pointId_distance
		.entrySet()
		.stream()
		.sorted(comparingByValue())
		.collect(
			toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,LinkedHashMap::new));

		int j = 0 ;
		for (Map.Entry<Integer, Double> entry : sortedMap.entrySet())
		{
    		Point keyPoint = pointId_point.get(entry.getKey());

			if (j < n)
            {
    		    //System.out.println(entry.getKey() + "/" + entry.getValue());

			    //System.out.println("keyPoint: " + keyPoint.x + " " + keyPoint.y);
			    nearestPoints.add(keyPoint);
			    j++;
			}
            else if (j >= n)
            {
    		    //System.out.println(entry.getKey() + "/" + entry.getValue());

			    //System.out.println("keyPoint: " + keyPoint.x + " " + keyPoint.y);
			    otherPoints.add(keyPoint);
			    j++;
            }
		}
        //System.out.println(nearestPoints.size());
        //System.out.println(otherPoints.size());
        //System.out.println(hb_locations.size());

        TwoList two = new TwoList(nearestPoints, otherPoints);
		return two;
    }

    public static TwoList nearestBales(List<Point> hb_locations, int n)
    {
        Point fp = furthestPoint(hb_locations);
        return nearestBales(fp, hb_locations, n);
    }

    public static TwoList nearestBales(Point p, List<Point> hb_locations)
    {
        return nearestBales(p, hb_locations, 11);
    }

    public static TwoList nearestBales(List<Point> hb_locations)
    {
        Point fp = furthestPoint(hb_locations);
        return nearestBales(fp, hb_locations, 11);
    }

    /*
    public static List<Point> nearest_Bales(Point p, List<Point> hb_locations)
    {
    	//declaring hash map and point id integer variable to keep track of distances
    	Map<Integer,Point> pointId_point = new HashMap();
    	Map<Integer,Double> pointId_distance = new HashMap();

    	int n = 11;

    	List<Point> nearestPoints = new ArrayList<Point>();
        List<Point> otherPoints = new ArrayList<Point>();

    	Integer id = new Integer(0);
    	for (Point hb_point:hb_locations)
        {

    		pointId_point.put(id,hb_point);
    		pointId_distance.put(id,distance(p,hb_point));
    		id ++;
    	}

		//sorting hash map by values
		Map<Integer, Double> sortedMap = pointId_distance
		.entrySet()
		.stream()
		.sorted(comparingByValue())
		.collect(
			toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,LinkedHashMap::new));

		int j = 0;
		for (Map.Entry<Integer, Double> entry : sortedMap.entrySet())
		{
    		Point keyPoint = pointId_point.get(entry.getKey());

			if (j < n)
            {
    		    //System.out.println(entry.getKey() + "/" + entry.getValue());

			    //System.out.println("keyPoint: " + keyPoint.x + " " + keyPoint.y);
			    nearestPoints.add(keyPoint);
			    j++;
			}
            else if (j >= n)
            {
    		    //System.out.println(entry.getKey() + "/" + entry.getValue());

			    //System.out.println("keyPoint: " + keyPoint.x + " " + keyPoint.y);
			    otherPoints.add(keyPoint);
			    j++;
            }
		}
        TwoList two = new TwoList(nearestPoints, otherPoints);
		return two;
    }
    */

    public static class BalesProtocol
    {
        public List<Point> balesLocations;
        public Integer protocol;

        public BalesProtocol(List<Point> b, Integer p)
        {
            this.balesLocations = b;
            this.protocol = p;
        }
    }

    public static void printCommand(Integer Id, String command)
    {
        System.out.println("COMMAND: Tractor " + Integer.toString(Id) + " " + command);
    }

    public static Point trailerOrigin(Integer Id)
    {
        Double xy = 1.0 / Math.sqrt(2.0);
        Double scale = 1.0 - (1.0 / (2 * (Double.valueOf(Id) + 1.0)));
        xy = xy * scale;
        return new Point(xy, xy);
    }

    public static Double step(Double scale, Double coordinate, Double r, Double theta, Integer trig)
    {
        if (trig == 0)
        {
            return coordinate + (r - scale) * Math.cos(theta);
        }
        else
        {
            return coordinate + (r - scale) * Math.sin(theta);
        }
    }

    public static Point shortcut(Point source, Point dest, List<Point> pointList)
    {
        Double dist = distance(source, dest);
        System.out.println("DISTANCE?:\t" + Double.toString(dist));
        if (dist <= 1.0)
        {
            System.out.println("NOT SAVING ANY DISTANCE");
            return dest;
        }
        else
        {
            Double theta = Math.atan2(dest.y - source.y, dest.x - source.x);
            Double scale = 0.9;
            //Double scale = 0.0;
            Double x_new = step(scale, source.x, dist, theta, 0);
            Double y_new = step(scale, source.y, dist, theta, 1);
            //Double x_new = source.x + (dist - scale) * Math.cos(theta);
            //Double y_new = source.y + (dist - scale) * Math.sin(theta);
            //if (distance(dest, new Point(x_new, y_new)) > 1)
            //{
            //    scale = 0.999;
            //    x_new = step(scale, source.x, dist, theta, 0);
            //    y_new = step(scale, source.y, dist, theta, 1);
            //}

            Point sc = new Point(x_new, y_new);
            Point nearest = nearestPoint(sc, pointList);
            System.out.println("NEAREST_(" + Double.toString(nearest.x) + "," + Double.toString(nearest.y) + ")");
            System.out.println("DESIRED_(" + Double.toString(dest.x) + "," + Double.toString(dest.y) + ")");
            //while (distance(sc, nearest) < distance(sc, dest))
            while (!nearest.equals(dest))
            {
                //System.out.println("step step step");
                //System.out.println("SHORTCUT:\t" + "(" + Double.toString(sc.x) + "," + Double.toString(sc.y) + ")");
                //System.out.println("DESIRED:\t" + "(" + Double.toString(dest.x) + "," + Double.toString(dest.y) + ")");
                //System.out.println("DESIRED DISTANCE:\t" + Double.toString(distance(sc, dest)));
                //System.out.println("NEAREST:\t" + "(" + Double.toString(nearest.x) + "," + Double.toString(nearest.y) + ")");
                //System.out.println("NEAREST DISTANCE:\t" + Double.toString(distance(sc, nearest)));

                scale = scale - 0.01;
                x_new = step(scale, source.x, dist, theta, 0);
                y_new = step(scale, source.y, dist, theta, 1);

                sc = new Point(x_new, y_new);
                nearest = nearestPoint(sc, pointList);
            }
            return sc;
        }
    }

    public static Point shortcut(Point source, Point dest)
    {
        Double dist = distance(source, dest);
        System.out.println("DISTANCE?:\t" + Double.toString(dist));
        if (dist <= 1.0)
        {
            System.out.println("NOT SAVING ANY DISTANCE");
            return dest;
        }
        else
        {
            Double theta = Math.atan2(dest.y - source.y, dest.x - source.x);
            Double scale = .99;
            //Double scale = 0.0;
            Double x_new = step(scale, source.x, dist, theta, 0);
            Double y_new = step(scale, source.y, dist, theta, 1);
            //Double x_new = source.x + (dist - scale) * Math.cos(theta);
            //Double y_new = source.y + (dist - scale) * Math.sin(theta);
            //if (distance(dest, new Point(x_new, y_new)) >= 1)
            //{
            //    scale = 0.99;
            //    x_new = step(scale, source.x, dist, theta, 0);
            //    y_new = step(scale, source.y, dist, theta, 1);
            //}

            Point sc = new Point(x_new, y_new);
            return sc;
        }
    }

    public static void main(String[] args)
    {
        System.out.println("test");
    }

}
