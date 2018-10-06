package sunshine.g3;

import java.util.List;
import java.util.Collections;
import java.util.Random;
import java.util.HashMap;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import sunshine.sim.Trailer;

import sunshine.g3.Util;

import sunshine.g3.Protocol.RandomNoTrailers;
import sunshine.g3.Protocol.RandomAllTrailers;


public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    
    List<Point> bales;

    ////////// Custom Variables //////////

    HashMap<Integer, Trailer> pairs = new HashMap<Integer, Trailer>();
    HashMap<Integer, Point> preemptive = new HashMap<Integer, Point>();

    ////////// End Custom Variables ////////// 

    public Player() {
        rand = new Random(seed);
    }
    
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = bales;
    }

    
    public Command getCommand(Tractor tractor)
    {
        //return RandomNoTrailers.getCommand(rand, tractor, bales, pairs, preemptive);
        return RandomAllTrailers.getCommand(rand, tractor, bales, pairs, preemptive);
    }
}
