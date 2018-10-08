package sunshine.g5;

import java.util.List;
import java.util.Collections;
import java.util.*;
import java.util.Random;
import java.util.HashMap;
import java.lang.Math;
import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.Trailer;
import sunshine.sim.CommandType;
import sunshine.sim.Point;


public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    int CollectBales;
    int CarNum;
    double length;
    static int count = 0;
    boolean fill[] ;
    List<Point> bales;
    List<Trailer> tlist;
    public Player() {
        rand = new Random(seed);
    }

    public void init(List<Point> bales, int n, double m, double t)
    {
        CollectBales = bales.size();
        CarNum = n;
        length = m;
        fill = new boolean[n];
        tlist = new ArrayList<>();
        this.bales = bales;
    }
    private Trailer Cloest(Tractor tractor){
        Trailer min = tlist.get(0);
        double dx = 0.0, dy = 0.0, mx=0.0,my=0.0;
        for(Trailer t:tlist){
            mx = min.getLocation().x-tractor.getLocation().x;
            my = min.getLocation().y-tractor.getLocation().y;
            dx = t.getLocation().x-tractor.getLocation().x;
            dy = t.getLocation().y-tractor.getLocation().y;
            if(dx*dx+dy*dy<mx*mx+my*my)
                min = t;
        }
        return min;
    }
    public Command getCommand(Tractor tractor)
    {

          if(Math.abs(tractor.getLocation().x-0)<1e-3 && Math.abs(tractor.getLocation().y-0)<1e-3){
              if(tractor.getHasBale()) {
                  CollectBales--;
                  return new Command(CommandType.UNLOAD);
              }
              if(tractor.getAttachedTrailer() == null)
                  return new Command(CommandType.ATTACH);
              if(tractor.getAttachedTrailer() != null){
                  Point p = new Point(length/2,length/2);
                  return Command.createMoveCommand(p);
              }
              if(tractor.getAttachedTrailer().getNumBales()>0)
                  return new Command(CommandType.DETATCH);
              if(Cloest(tractor).getNumBales()>0){
                  fill[tractor.getId()]=false;
                  return new Command(CommandType.UNSTACK);
              }



          }
          if(Math.abs(tractor.getLocation().x-length/2)<1e-4 && Math.abs(tractor.getLocation().y-length/2)<1e-4){
              if(tractor.getAttachedTrailer()!=null){
                 if(tlist.size()<CarNum) tlist.add(tractor.getAttachedTrailer());
                 if(tractor.getAttachedTrailer().getNumBales()==0){
                     return new Command(CommandType.DETATCH);
                 }else{
                     Point p = new Point(0, 0);
                     return Command.createMoveCommand(p);
                 }
              }
              if(!tractor.getHasBale()){
                  if(bales.size()>0){
                    Point p = new Point(bales.get(0).x,bales.get(0).y);
                    return Command.createMoveCommand(p);
                  }else{
                     Point p = new Point(0, 0);
                     return Command.createMoveCommand(p);
                  }
              }
              if(fill[tractor.getId()]){
                  Point p = new Point(0, 0);
                  return Command.createMoveCommand(p);
              }
              if(tractor.getHasBale() && (count%10==0 ||CollectBales<10)){

                  fill[tractor.getId()] = true;
                  return new Command(CommandType.ATTACH);
              }


              if(tractor.getHasBale() && !fill[tractor.getId()])  {
                  count++;
                  return new Command(CommandType.STACK);
              }


          }
          if(tractor.getHasBale()){
              Point p = new Point(length/2,length/2);
              return Command.createMoveCommand(p);
          }else{
              if(bales.size()>0){
                if(Math.abs(tractor.getLocation().x-bales.get(0).x)<1e-4 && Math.abs(tractor.getLocation().y-bales.get(0).y)<1e-4){

                       bales.remove(bales.get(0));
                  return new Command(CommandType.LOAD);
                }else{
                  Point p = new Point(bales.get(0).x,bales.get(0).y);
                  return Command.createMoveCommand(p);
                }
              }else{
                  Point p = new Point(0, 0);
                  return Command.createMoveCommand(p);
              }
          }



    }
}
//public class Player implements sunshine.sim.Player {
//    // Random seed of 42.
//    private int seed = 42;
//    private Random rand;
//
//    List<Point> bales;
//
//    public Player() {
//        rand = new Random(seed);
//    }
//
//    public void init(List<Point> bales, int n, double m, double t)
//    {
//        this.bales = bales;
//    }
//
//    public Command getCommand(Tractor tractor)
//    {
//        if (tractor.getHasBale())
//        {
//            if (tractor.getLocation().equals(new Point(0.0, 0.0)))
//            {
//                return new Command(CommandType.UNLOAD);
//            }
//            else
//            {
//                return Command.createMoveCommand(new Point(0.0, 0.0));
//            }
//        }
//        else
//        {
//            if (tractor.getLocation().equals(new Point(0.0, 0.0)))
//            {
//                if (rand.nextDouble() > 0.5)
//                {
//                    if (tractor.getAttachedTrailer() == null)
//                    {
//                        return new Command(CommandType.ATTACH);
//                    }
//                    else
//                    {
//                        return new Command(CommandType.DETATCH);
//                    }
//                }
//                else if (bales.size() > 0)
//                {
//                    Point p = bales.remove(rand.nextInt(bales.size()));
//                    return Command.createMoveCommand(p);
//                }
//                else
//                {
//                    return null;
//                }
//            }
//            else
//            {
//                return new Command(CommandType.LOAD);
//            }
//        }
//    }
//}
//
