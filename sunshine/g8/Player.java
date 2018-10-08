package sunshine.g8;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;

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

    public Player() {
        rand = new Random(seed);
    }
    
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = bales;
        //System.out.println(n); //30 - number of tractors
        //System.out.println(m); //500 - length of field
        //System.out.println(t); //10000 - time


        // PREPROCESS FIELD
        // System.out.println(bales.size()); //number of bales
        //System.out.println("HI"+bales.get(0).x); //point object
        //System.out.println("HI"+bales.get(0).y);
        int cellSize = 200; //TODO


    }
    
    public Command getCommand(Tractor tractor)
    {
        if (tractor.getHasBale())
        {
            if (tractor.getLocation().equals(new Point(0.0, 0.0))) // has bale, at barn
            {
                if (tractor.getAttachedTrailer() != null) 
                { //has bale, at barn, with trailer
                    return new Command(CommandType.DETATCH); 
                } 
                else 
                { // has bale at barn, no trailer
                    if(rand.nextDouble() > 0.2) 
                    {
                        return new Command(CommandType.UNLOAD);
                    }
                    else 
                    {
                        return new Command(CommandType.UNSTACK);
                    }
                }
            }
            else // has bale, not at barn
            {
                if (tractor.getAttachedTrailer() != null) // has trailer, detach or move to barn
                { 
                    //Trailer t = tractor.getAttachedTrailer();
                    if (tractor.getAttachedTrailer().getNumBales() < 10) 
                    {
                        return new Command(CommandType.DETATCH); 
                    } else {
                        return Command.createMoveCommand(new Point(0.0, 0.0)); //move to barn
                    }

                } 
                else 
                { // no trailer, move to barn, stack to nearest, or attach to trailer
                    double r = rand.nextDouble();
                    if (r < 0.1) 
                    {
                        return Command.createMoveCommand(new Point(0.0, 0.0)); //move to barn
                    }
                    else if (r < 0.5) 
                    {
                        return new Command(CommandType.STACK); 
                    }
                    else 
                    {
                        return new Command(CommandType.ATTACH);
                    }
                }

            }
        } 
        else 
        {
            if (tractor.getLocation().equals(new Point(0.0, 0.0))) //at barn
            {
                if (rand.nextDouble() > 0.5)
                {
                    if (tractor.getAttachedTrailer() == null) // attach/detatch trailer half the time
                    {
                        return new Command(CommandType.ATTACH); 
                    }
                    else
                    {
                        return new Command(CommandType.DETATCH);
                    }
                }
                else if (bales.size() > 0)
                {
                    Point p = bales.remove(rand.nextInt(bales.size())); // find a bale to remove
                    return Command.createMoveCommand(p); //move to that point
                }
                else
                {
                    return null;
                }
            }
            else //not at barn
            {
                return new Command(CommandType.LOAD);
            }
        }
    }
}
