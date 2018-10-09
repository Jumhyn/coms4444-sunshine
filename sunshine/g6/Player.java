package sunshine.g6;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Comparator;
import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import sunshine.sim.Trailer;


public class Player implements sunshine.sim.Player {
    private int seed = 42;
    private Random rand;   
    List<Point> bales = new ArrayList<Point>();
    List<Point> farBales = new ArrayList<Point>();
    List<Point> closeBales = new ArrayList<Point>();
    int closeTractor;
    int farTractor;
    double circleArc;// = 90/farTractor;
    List<Point> pointOncircle = new ArrayList<Point>();
    double radius;

    List<Integer> close_tractor = new ArrayList<Integer>();
    List<Integer> away_tractor = new ArrayList<Integer>();
    Map<Integer, Integer> tractor_mode = new HashMap<Integer, Integer>();

    List<Integer> leftover_tractor = new ArrayList<Integer>();
    List<Point> dropPoints = new ArrayList<Point>();
    List<Point> drop = new ArrayList<Point>();


    private Map<Point, Double> balePointsSorted;
    private HashMap<Point, Double> balePointsForSorting;

    public Player() {
        rand = new Random(seed);
    }
    public double dist(double x1,double y1,double x2,double y2){
        return Math.hypot(x1 - x2, y1 - y2);
    }
    public List<Point> dropTrailers(){
        
        for(int i=0;i<90;i+=circleArc){
            double center_x = 0.0;
            double center_y = 0.0;
            double x = center_x + radius * Math.cos(Math.PI*i/180);
            double y = Math.abs(center_y - radius * Math.sin(Math.PI*i/180));
            Point p_temp = new Point(x, y);
            dropPoints.add(p_temp);
        }
        return dropPoints;
    }
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = bales;
        this.radius = m;

        for (int i=0; i<bales.size(); i++){
            if (dist(bales.get(i).x,bales.get(i).y, 0, 0) > radius) {
                farBales.add(bales.get(i));
            }
            else{
                closeBales.add(bales.get(i));
            }
        }

        this.farTractor = Math.round(farBales.size() / 10) + 1;
        this.closeTractor = n - farTractor;
        this.circleArc = 90/farTractor;

        for (int i=0; i<n;i++){
            if (i<closeTractor){
                close_tractor.add(i);
                tractor_mode.put(i,0);
                // state 0, needs to detach
            }
            else{
                away_tractor.add(i);
                tractor_mode.put(i, 1);
                // state 1, ready to go
            }
        }

        // System.out.println("how many bales: " + bales.size());
        // System.out.println("how many far bales: " + farBales.size());
        // System.out.println("how many close bales: " + closeBales.size());
        // System.out.println("how many far tractors: " + farTractor);
        // System.out.println("how many close tractors: " + closeTractor);
        // System.out.println("away tractor numbers");
        // for (Integer tractor : away_tractor) {
        //     System.out.println(tractor); 
        // }

        // ##############################################################
        // # Sort the bale points according to distance from the origin #
        // ##############################################################

        //balePointsSorted = new ArrayList<Point>();
        balePointsForSorting = new HashMap<Point, Double>();
        double dist = 0.0;
        for (Point p : bales) {
            dist = Math.hypot(p.x - 0.0, p.y - 0.0);
            balePointsForSorting.put(p, dist); //assuming there cannot be more than 1 bale of hay in a position
            balePointsSorted = balePointsForSorting
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .collect(
                    toMap(e -> e.getKey(), e -> e.getValue(), (e1,e2) -> e2,
                        LinkedHashMap::new));
            balePointsSorted = balePointsForSorting
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(
                    toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e2,
                        LinkedHashMap::new));
        } //https://www.javacodegeeks.com/2017/09/java-8-sorting-hashmap-values-ascending-descending-order.html
        //System.out.println(balePointsSorted);

    }

    Map<Integer,Integer> dropPlaceNum = new HashMap<Integer,Integer>();
    
    public Command getCommand(Tractor tractor){

        System.out.println("how many bales left: " + bales.size());
        System.out.println("how many far bales left: " + farBales.size());
        System.out.println("how many close bales left: " + closeBales.size());
        System.out.println("how many far tractors: " + away_tractor.size());
        System.out.println("how many close tractors: " + close_tractor.size());

        int id = tractor.getId();
        drop = dropTrailers();

        // System.out.println("all points:");
        // for (Point p : bales) {
        //     System.out.print(p.x + ", ");
        //     System.out.println(p.y);    
        // }

        // System.out.println("trailer drop points:");
        // for (Point p : drop) {
        //     System.out.print(p.x + ", ");
        //     System.out.println(p.y);    
        // }
        
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
                tractor_mode.put(id,7);
                if (bales.size() != 0) {
                    randNum = rand.nextInt(closeBales.size());
                    p = closeBales.remove(randNum);
                    bales.remove(randNum);
                    return Command.createMoveCommand(p);
                }
                // to counter error at the end
                else {
                    return new Command(CommandType.DETATCH);
                }

            }
            else{ // for far tractors
                tractor_mode.put(id,2);
                dropPlaceNum.put(id,0);  // this point has 0 bales initially
                p_drop = drop.get(id-closeTractor);
                return Command.createMoveCommand(p_drop); //go to the point that drops trailer
            }

            case 2:
            tractor_mode.put(id,3);
            return new Command(CommandType.DETATCH); 

            //go to location of bale in far area
            case 3:
            tractor_mode.put(id,7);
            randNum = rand.nextInt(farBales.size());
            p = farBales.remove(randNum);
            bales.remove(randNum);
            return Command.createMoveCommand(p);

            //return to the trailer and bales amount at this point +1
            case 4:
            tractor_mode.put(id,5);
            return Command.createMoveCommand(drop.get(id-closeTractor));

            //stack bale into trailer, increment trailer count
            case 5:
            tractor_mode.put(id,6);
            // System.out.println("checking if tractor " + id + " has bale at case 5: " + tractor.getHasBale());
            dropPlaceNum.put(id, dropPlaceNum.get(id)+1);    
            return new Command(CommandType.STACK);    


            case 6:
            // if full, attach
            if ((dropPlaceNum.get(id)>9) || (farBales.size() == 0))
            {
                tractor_mode.put(id,8); 
                return new Command(CommandType.ATTACH);
            }
            else{   // if not, find more bales
                // System.out.println("checking if tractor has bale at case 6: " + tractor.getHasBale());
                tractor_mode.put(id,7);
                randNum = rand.nextInt(farBales.size());
                p = farBales.remove(randNum);
                bales.remove(randNum);
                return Command.createMoveCommand(p);
            }

            //load
            case 7:
            if (close_tractor.contains(id)) {
                tractor_mode.put(id,8);
                return new Command(CommandType.LOAD);
            }
            else {
                tractor_mode.put(id,4);
                return new Command(CommandType.LOAD);
            }
            
        
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
                tractor_mode.put(id,10);
                return new Command(CommandType.DETATCH);
            }

            case 10:
            if (dropPlaceNum.get(id) > 0) {
                tractor_mode.put(id, 11);
                dropPlaceNum.put(id, dropPlaceNum.get(id) - 1);
                return new Command(CommandType.UNSTACK);
            }
            else {
                tractor_mode.put(id, 1);
                close_tractor.add(id);
                away_tractor.remove(Integer.valueOf(id));
                return Command.createMoveCommand(new Point(0,0));
            }

            case 11:
            tractor_mode.put(id,10);
            return new Command(CommandType.UNLOAD);

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
