package sunshine.sim;

import java.util.Random;
import java.util.ArrayList;

import sunshine.sim.Point;

public class Harvester {
    public static ArrayList<Point> harvest(Random r, int m)
    {
        ArrayList<Point> ret = new ArrayList<>();
        double total = m * m / 5;
        double dist = r.nextDouble() * 10.0 + 20.0;
        while (dist < total)
        {
            ret.add(distToPoint(dist, m, r));
            dist += r.nextDouble() * 10.0 + 20.0;
        }
        
        return ret;
    }
    
    private static Point distToPoint(double length, int m, Random r)
    {
        double row = Math.floor(length / m);
        double yInRow = r.nextDouble() * 5.0;
        return new Point(length % m, row * 5.0 + yInRow);
    }
}
