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

    // given 11 points, calculator time taken for tracker WITH trailer 
    // 10 m/s * distance + detach(60) + attach(60) + detach load, unload(10 * 2) 
    public static Double timeWithTrailer(List<Point> pointList) 
    {
        Double time;
        //Point origin = new Point(0.0, 0.0);
        Point centroid = Util.centroidTrailer(pointList);
        //System.out.println(centroid.x + " " + centroid.y);
        // from origin to centroid
        time = 2 * Util.distance(centroid, origin)/4 + 60 * 3; // 2 * distance/speed + detach
        for (Point p : pointList) {
            // from centroid to point  
            time += 2 * Util.distance(p, centroid)/4; //
            time += 10 * 4; // load, stack, unstack, unload 
        }
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
    public static void main(String[] args)
    {
        Random rand = new Random();
        List<Point> baleLocations = Harvester.harvest(rand, 100);
        System.out.println(baleLocations.size());

        Point p = furthestPoint(baleLocations);
        System.out.println(p.x + " " + p.y);

        TwoList nearestPoints = nearest_Bales(p, baleLocations);
        System.out.println(nearestPoints.toLoad.size());
        System.out.println(nearestPoints.toLoad);
        System.out.println(nearestPoints.other.size());
        System.out.println(nearestPoints.other);
    }
    */

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
}
