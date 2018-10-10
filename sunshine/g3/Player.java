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
import sunshine.g3.Util.*;

import sunshine.g3.Protocol.RandomNoTrailers;
import sunshine.g3.Protocol.RandomAllTrailers;
import sunshine.g3.Protocol.FurthestPointCluster;


public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    
    List<Point> bales;

    ////////// Custom Variables //////////

    HashMap<Integer, Trailer> pairs = new HashMap<Integer, Trailer>();
    HashMap<Integer, Point> preemptive = new HashMap<Integer, Point>();
    HashMap<Integer, Util.BalesProtocol> balesAssignments= new HashMap<Integer, Util.BalesProtocol>();

    Integer test = 0;
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
        // balesAssignments initialization
        Integer Id = tractor.getId();

        if (!balesAssignments.containsKey(Id))
        {
            List<Point> emptyList = Collections.emptyList();
            BalesProtocol emptyBP = new BalesProtocol(emptyList, -1);

            balesAssignments.put(Id, emptyBP);
        }

        System.out.println(bales.size());
        //return RandomNoTrailers.getCommand(rand, tractor, bales, pairs, preemptive, balesAssignments);
        //return RandomAllTrailers.getCommand(rand, tractor, bales, pairs, preemptive, balesAssignments);
        bales = FurthestPointCluster.updateBales(tractor, bales, balesAssignments);
        Command c = FurthestPointCluster.getCommand(rand, tractor, bales, pairs, preemptive, balesAssignments);
        //return FurthestPointCluster.getCommand(rand, tractor, bales, pairs, preemptive, balesAssignments);
        //if (c == new Command(CommandType.LOAD))
        //{
        //    if (!tractor.getHasBale())
        //    {
        //        System.out.println("COMMAND: TRACTOR " + Integer.toString(Id) + " ERROR");
        //    }
        //}
        return c; 
    }
}
