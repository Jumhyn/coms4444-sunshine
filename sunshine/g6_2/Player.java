package sunshine.g6_2;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Comparator;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;



public class Player implements sunshine.sim.Player {
    private int seed = 42;
    private Random rand;   
    List<Point> bales = new ArrayList<Point>();
    List<Point> farBales = new ArrayList<Point>();
    List<Point> closeBales = new ArrayList<Point>();
    int closeTractor = 105;
    int farTractor = 45;
    double circleArc = 90/farTractor;
    List<Point> pointOncircle = new ArrayList<Point>();
    double radius = 600;

    List<Integer> close_tractor = new ArrayList<Integer>();
    List<Integer> away_tractor = new ArrayList<Integer>();
    Map<Integer, Integer> tractor_mode = new HashMap<Integer, Integer>();

    public Player() {
        rand = new Random(seed);
    }
    public double dist(double x1,double y1,double x2,double y2){
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }
    public List<Point> dropTrailers(){
        List<Point> dropPoints = new ArrayList<Point>();
        for(int i=0;i<90;i+=circleArc){
            double center_x = 0.0;
            double center_y = 0.0;
            double x = center_x + radius * Math.cos(Math.PI*i/180);
            double y = center_y - radius * Math.sin(Math.PI*i/180);
            Point p_temp = new Point(x, y);
            dropPoints.add(p_temp);
        }
        return dropPoints;
    }
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = bales;
        for (int i=0; i<bales.size(); i++){
            if (dist(bales.get(i).x,bales.get(i).y,0,0) > radius){
                farBales.add(bales.get(i));
            }
            else{
                closeBales.add(bales.get(i));
            }
        }
        for (int i=0; i<n;i++){
            if (i<105){
                close_tractor.add(i);
                tractor_mode.put(i,0);
                // state 0, needs to detach
            }
            else{
                Point p = farBales.remove(rand.nextInt(farBales.size()));
                away_tractor.add(i);
                tractor_mode.put(i, 1);
                // state 1, ready to go
            }
        }
    }
    
    public Command getCommand(Tractor tractor){
        int id = tractor.getId();
        List<Point> drop = dropTrailers();
        Map<Integer,Integer> dropPlaceNum = new HashMap<Integer,Integer>();
        int randNum = 0;
        Point p;
        Point p_drop;
        
        switch(tractor_mode.get(id))
        {
            // at 0,0, need to detatch
            case 0: 
            tractor_mode.put(id,1);
            return new Command(CommandType.DETATCH);
            
            // at 0,0, ready to go do task
            case 1:
            if (close_tractor.contains(id)){
                tractor_mode.put(id,8);
                randNum = rand.nextInt(closeBales.size());
                p = closeBales.remove(randNum);
                bales.remove(randNum);
                return Command.createMoveCommand(p);
            }
            else{
                tractor_mode.put(id,2);
                dropPlaceNum.put(id, 0);  // this point has 0 bales initially
                p_drop = drop.get(id-105);
                return Command.createMoveCommand(p_drop); //go to the point that drops trailer
            }

            case 2:
            tractor_mode.put(id,3);
            return new Command(CommandType.DETATCH); 

            //go to collect bales from far area
            case 3:
            tractor_mode.put(id,4);
            randNum = rand.nextInt(farBales.size());
            p = farBales.remove(randNum);
            bales.remove(randNum);
            return Command.createMoveCommand(p);

            //return to the trailer and bales amount at this point +1
            case 4:
            tractor_mode.put(id,5);
            return Command.createMoveCommand(drop.get(id-105));

            //unload
            case 5:
            tractor_mode.put(id,6);
            dropPlaceNum.put(id, dropPlaceNum.get(id)+1);
            return new Command(CommandType.UNLOAD);

            case 6:
            if (dropPlaceNum.get(id)>9)
            {
                tractor_mode.put(id,7); 
                return new Command(CommandType.ATTACH);
            }
            else{
                tractor_mode.put(id,4);
                randNum = rand.nextInt(farBales.size());
                p = farBales.remove(randNum);
                bales.remove(randNum);
                return Command.createMoveCommand(p);
            }

            //stack
            case 7:
            tractor_mode.put(id,8);
            return new Command(CommandType.STACK);
        
            // return to origin
            case 8:
            tractor_mode.put(id,9);
            return Command.createMoveCommand(new Point(0,0));

            case 9:
            if (close_tractor.contains(id)){
                tractor_mode.put(id,1);
                return new Command(CommandType.UNLOAD);
            }
            else{
                tractor_mode.put(id,1);
                return new Command(CommandType.UNSTACK);
            }

        }        
        return Command.createMoveCommand(new Point(0,0));
    }


    // public Command getCommand(Tractor tractor)
    // {
    //     if (tractor.getHasBale())
    //     {
    //         if (tractor.getLocation().equals(new Point(0.0, 0.0)))
    //         {
    //             return new Command(CommandType.UNLOAD);
    //         }
    //         else
    //         {
    //             return Command.createMoveCommand(new Point(0.0, 0.0));
    //         }
    //     }
    //     else
    //     {
    //         if (tractor.getLocation().equals(new Point(0.0, 0.0)))
    //         { 
    //             if (tractor.getAttachedTrailer()!=null){
    //             	return new Command(CommandType.DETATCH);
    //             }
    //             else
    //             if (closeBales.size() > 0)
    //             {
                    // int randNum = rand.nextInt(closeBales.size());
                    // Point p = closeBales.remove(randNum);
                    // bales.remove(randNum);
                    // return Command.createMoveCommand(p);
    //             }
    //             else
    //             {
    //                 return null;
    //             }
    //         }
    //         else
    //         {
    //             return new Command(CommandType.LOAD);
    //         }
    //     }
    // }
}
