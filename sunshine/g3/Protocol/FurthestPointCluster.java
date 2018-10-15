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

import sunshine.g3.Protocol.NoTrailers;
import sunshine.g3.Protocol.CentroidAllTrailers;


public class FurthestPointCluster {
    public static List<Point> updateBales(Tractor tractor,
                                           List<Point> bales,
                                           HashMap<Integer, BalesProtocol> balesAssignments)
    {
        Integer Id = tractor.getId();

        List<Point> assignedBales = balesAssignments.get(Id).balesLocations;
        Integer protocol = balesAssignments.get(Id).protocol;

        //if (assignedBales.size() == 0)
        if (protocol == -1)
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
            //System.out.println(bales.equals(two.second));
            //System.out.println(bales);
            //System.out.println(bales.size());
            //System.out.println(two.second);
            //System.out.println(two.second.size());

            if (Util.timeWithoutTrailer(proposed) <= Util.timeWithTrailer(proposed))
            {
                System.out.println("COMMAND: TRACTOR " + Integer.toString(Id) + " CHANGE_PROTOCOL_TO_0");
                protocol = 0;
                balesAssignments.put(Id, new BalesProtocol(bales, protocol));
            }
            else
            {
                System.out.print("\nCOMMAND: TRACTOR " + Integer.toString(Id) + " ASSIGNED_TO");
                for (Point p: two.first)
                {
                    String x = Double.toString(p.x);
                    String y = Double.toString(p.y);

                    System.out.print("(" + x + "," + y + ")_");
                }
                System.out.print("\n");
                bales = two.second;
                protocol = 1;
                balesAssignments.put(Id, new BalesProtocol(proposed, protocol));
            }
            //balesAssignments.put(Id, new BalesProtocol(proposed, protocol));
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
            System.out.println("COMMAND: TRACTOR " + Integer.toString(Id) + " COMMITTING_TO_0");
            return NoTrailers.getCommand(rand,
                                         tractor,
                                         assignedBales,
                                         pairs,
                                         preemptive,
                                         balesAssignments);
        }
        else if (protocol == 1)
        {
            System.out.println("COMMAND: TRACTOR " + Integer.toString(Id) + " COMMITTING_TO_1");
            return CentroidAllTrailers.getCommand(rand,
                                                  tractor,
                                                  assignedBales,
                                                  pairs,
                                                  preemptive,
                                                  balesAssignments);
        }
        return null;
    }
}





