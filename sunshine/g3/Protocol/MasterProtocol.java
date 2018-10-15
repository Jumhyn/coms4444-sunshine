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
import sunshine.g3.Protocol.CenterAllTrailers;


public class MasterProtocol {
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
                Util.printCommand(Id, "COMMITTING_TO_0");
                Integer stillTrailer;
                for (BalesProtocol bp: balesAssignments.values())
                {
                }
                if (bales.contains(new Point(74.94436541002597,39.0040571209928)))
                {
                    System.out.println("THERE IT IS");
                }

                protocol = 0; // 0 - NoTrailers
                balesAssignments.put(Id, new BalesProtocol(bales, protocol));
            }
            else
            {
                Util.printCommand(Id, "COMMITTING_TO_1");

                if (proposed.contains(new Point(74.94436541002597,39.0040571209928)))
                {
                    System.out.println("THERE IT IS");
                }
                else if (two.second.contains(new Point(74.94436541002597,39.0040571209928)))
                {
                    System.out.println("THERE IT IS NOW");
                }

                // TODO: Test
                System.out.printf("\nCOMMAND: Tractor " + Integer.toString(Id) + " ASSIGNED_TO_");
                for (Point p: proposed)
                {
                    System.out.printf("(" + Double.toString(p.x) + "," + Double.toString(p.y) + ")_");
                }
                System.out.printf("\n");

                bales = two.second;
                protocol = 1; // 1 - CenterAllTrailers
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

        if (protocol == 0)
        {
            return NoTrailers.getCommand(rand,
                                         tractor,
                                         assignedBales,
                                         pairs,
                                         preemptive,
                                         balesAssignments);
        }
        else if (protocol == 1)
        {
            return CenterAllTrailers.getCommand(rand,
                                                  tractor,
                                                  assignedBales,
                                                  pairs,
                                                  preemptive,
                                                  balesAssignments);
        }
        return null;
    }
}
