package sunshine.g3;


import sunshine.g3.Util;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import sunshine.sim.*;

public class Util_ashley {

    // given 11 points, calculator time taken for tracker WITH trailer 
    // 10 m/s * distance + detach(60) + attach(60) + detach load, unload(10 * 2) 
    public static Double timeWithTrailer(List<Point> pointList) {
        Double time;
        Point origin = new Point(0.0, 0.0);
        Point centroid = Util.centroidTrailer(pointList);
        System.out.println(centroid.x + " " + centroid.y);
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
    public static Double timeWithoutTrailer(List<Point> pointList) {
        Double time = 0.0;;
        Point origin = new Point(0.0, 0.0);
        time += 60; // detach at origin
        for (Point p : pointList) {
            time += 2 * Util.distance(p, origin)/10;
            time += 10 * 2; // load, unload 
        }
        return time;
    }

    public static Point centroid(List<Point> pointList) {
        return pointList.get(0);

    }
 
    public static void main(String[] args) {
        Random rand = new Random();
        List<Point> baleLocations = Harvester.harvest(rand, 400);
        List<Point> newList = new ArrayList<Point>();
        for (int i = baleLocations.size() - 1; i > baleLocations.size() - 12; i--) {
            newList.add(baleLocations.get(i));
        } 
        System.out.println(newList);
        System.out.println(timeWithTrailer(newList));
        System.out.println(timeWithoutTrailer(newList));
    }


}

