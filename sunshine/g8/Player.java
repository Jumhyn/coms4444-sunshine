package sunshine.g8;

import java.lang.Math;


import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Comparator;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import sunshine.sim.Trailer;


public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    
    List<Point> bales;
    //Point origin;

    public Player() {
        rand = new Random(seed);
    }
    private double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x-p2.x,2)+Math.pow(p1.y-p2.y,2));

    }
    Comparator<Point> pointComparator = new Comparator<Point>() {
        @Override
        public int compare(Point p1, Point p2) {
            Point origin = new Point(0.0,0.0);
            double d1 = distance(origin, p1);
            double d2 = distance(origin, p2);
            if (d1 < d2) {
                return -1;
            } else if (d1 > d2) {
                return 1;
            } else {
                return 0;
            }
        }
    };
    
    public void init(List<Point> bales, int n, double m, double t)
    {

        Collections.sort(bales,pointComparator);
        this.bales = bales;

        //System.out.println(n); //30 - number of tractors
        //System.out.println(m); //500 - length of field
        //System.out.println(t); //10000 - time

        int cellSize = 200; //TODO


    }
    
    public Command getCommand(Tractor tractor)
    {
        if (tractor.getHasBale()) {
            if (tractor.getLocation().equals(new Point(0.0, 0.0))) {
                return new Command(CommandType.UNLOAD);
            } else {
                return Command.createMoveCommand(new Point(0.0, 0.0));
            }
        } else { // no bale
            if (tractor.getLocation().equals(new Point(0.0, 0.0))) {
                if (tractor.getAttachedTrailer() == null) {
                    Point p = bales.remove(bales.size()-1);
                    return Command.createMoveCommand(p);
                } else {
                    return new Command(CommandType.DETATCH);
                }

            } else {
                return new Command(CommandType.LOAD);
            }

        }
    }
}
