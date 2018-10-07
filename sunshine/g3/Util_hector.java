package sunshine.g3;


// import sunshine.g3;


import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;



import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;

import sunshine.sim.*;

public class Util_hector {

	public static Double distance(Point a, Point b)
    {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

	public static Point furthestPoint(List<Point> hb_locations){

		Point origin = new Point(0.0,0.0);
		Point furthestPoint = new Point(0.0,0.0);
		Double furthestDistance = 0.0;
		Double candidate;

		// List<Double> distances = new ArrayList<Double>();
		


		for (Point p:hb_locations){
			
			candidate = distance(origin,p);


			if (candidate > furthestDistance) {
				furthestPoint = p;
				furthestDistance = candidate;
			}

		}

		return furthestPoint;
	}

    public static List<Point> nearest_Bales(Point p, List<Point> hb_locations, int n){

    	//declaring hash map and point id integer variable to keep track of distances
    	Map<Integer,Point> pointId_point = new HashMap();
    	Map<Integer,Double> pointId_distance = new HashMap();

    	List<Point> nearestPoints = new ArrayList<Point>();


    	Integer id = new Integer(0);
    	for (Point hb_point:hb_locations){

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
			if (j >= n){
				break;
			}
    		// System.out.println(entry.getKey() + "/" + entry.getValue());

    		Point keyPoint = pointId_point.get(entry.getKey());


			System.out.println("keyPoint: " + keyPoint.x + " " + keyPoint.y);
			nearestPoints.add(keyPoint);

			j++;

		}



		return nearestPoints;



    }

    public static List<Point> nearest_Bales(Point p, List<Point> hb_locations){

    	//declaring hash map and point id integer variable to keep track of distances
    	Map<Integer,Point> pointId_point = new HashMap();
    	Map<Integer,Double> pointId_distance = new HashMap();

    	int n = 11;

    	List<Point> nearestPoints = new ArrayList<Point>();


    	Integer id = new Integer(0);
    	for (Point hb_point:hb_locations){

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
			if (j >= n){
				break;
			}
    		// System.out.println(entry.getKey() + "/" + entry.getValue());

    		Point keyPoint = pointId_point.get(entry.getKey());


			System.out.println("keyPoint: " + keyPoint.x + " " + keyPoint.y);
			nearestPoints.add(keyPoint);

			j++;

		}



		return nearestPoints;



    }

  
    public static void main(String[] args){

	
	Random rand = new Random();
    List<Point> baleLocations = Harvester.harvest(rand, 100);

    // System.out.println(baleLocations);

    Point p = furthestPoint(baleLocations);
    System.out.println(p.x + " " + p.y);

    // List<Point> hb_locs = nearest_N_Bales();
    List<Point> nearestPoints = nearest_Bales(p,baleLocations,50);
    // System.out.println(nearestPoints);



}

}


