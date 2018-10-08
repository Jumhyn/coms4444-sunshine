package sunshine.g3.Protocol;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.Collections;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import sunshine.sim.Trailer;

import sunshine.g3.Util;
import sunshine.g3.Util.*;

import sunshine.g3.Protocol.RandomNoTrailers;
import sunshine.g3.Protocol.CentroidRandomAllTrailers;


public class FurthestPointCluster {
    public static List<Point> updateBales(Tractor tractor,
                                           List<Point> bales,
                                           HashMap<Integer, BalesProtocol> balesAssignments)
    {
        Integer Id = tractor.getId();

        List<Point> assignedBales = balesAssignments.get(Id).balesLocations;
        Integer protocol = balesAssignments.get(Id).protocol;

        if (assignedBales.size() == 0)
        {
            TwoList two = Util.nearestBales(bales);
            List<Point> proposed = new ArrayList<Point>(two.first);
            //System.out.println(bales);
            //System.out.println(bales.size());
            //System.out.println(bales.equals(two.second));
            //for (int i = 0; i < bales.size(); i++)
            //{
            //    Point p = bales.get(i);
            //    if (proposed.contains(p))
            //    {
            //        bales.remove(i);
            //    }
            //}
            //bales = two.second;
            bales = two.second;
            //System.out.println(bales.equals(two.second));
            //System.out.println(bales);
            //System.out.println(bales.size());
            //System.out.println(two.second);
            //System.out.println(two.second.size());

            if (Util.timeWithoutTrailer(proposed) <= Util.timeWithTrailer(proposed))
            {
                protocol = 0;
            }
            else
            {
                protocol = 1;
            }

            balesAssignments.put(Id, new BalesProtocol(proposed, protocol));
        }
        return bales;
    }

    public static Command getCommand(Random rand,
                                     Tractor tractor,
                                     List<Point> bales,
                                     HashMap<Integer, Trailer> pairs,
                                     HashMap<Integer, Point> preemptive,
                                     HashMap<Integer, BalesProtocol> balesAssignments)
    {
        Integer Id = tractor.getId();

        List<Point> assignedBales = balesAssignments.get(Id).balesLocations;
        Integer protocol = balesAssignments.get(Id).protocol;

        //if (assignedBales.size() == 0)
        //{
        //    TwoList two = Util.nearestBales(bales);
        //    List<Point> proposed = new ArrayList<Point>(two.first);
        //    //System.out.println(bales);
        //    //System.out.println(bales.size());
        //    //System.out.println(bales.equals(two.second));
        //    //for (int i = 0; i < bales.size(); i++)
        //    //{
        //    //    Point p = bales.get(i);
        //    //    if (proposed.contains(p))
        //    //    {
        //    //        bales.remove(i);
        //    //    }
        //    //}
        //    //bales = two.second;
        //    bales = two.second;
        //    //System.out.println(bales.equals(two.second));
        //    //System.out.println(bales);
        //    //System.out.println(bales.size());
        //    //System.out.println(two.second);
        //    //System.out.println(two.second.size());

        //    if (Util.timeWithoutTrailer(proposed) <= Util.timeWithTrailer(proposed))
        //    {
        //        protocol = 0;
        //    }
        //    else
        //    {
        //        protocol = 1;
        //    }

        //    balesAssignments.put(Id, new BalesProtocol(proposed, protocol));
        //}

        if (protocol == 0)
        {
            return RandomNoTrailers.getCommand(rand,
                                               tractor,
                                               assignedBales,
                                               pairs,
                                               preemptive,
                                               balesAssignments);
        }
        else if (protocol == 1)
        {
            return CentroidRandomAllTrailers.getCommand(rand,
                                                tractor,
                                                assignedBales,
                                                pairs,
                                                preemptive,
                                                balesAssignments);
        }
        return null;
    }
}





