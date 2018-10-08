package sunshine.queuerandom;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;


public class Player extends sunshine.queuerandom.QueuePlayer {
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
        super.init(bales, n, m, t);
    }
    
    public ArrayList<Command> getMoreCommands(Tractor tractor)
    {
        if (bales.size() == 0)
        {
            return null;
        }
        Point bale = bales.remove(rand.nextInt(bales.size()));
        ArrayList<Command> toReturn = new ArrayList<Command>();
        toReturn.add(Command.createMoveCommand(bale));
        toReturn.add(new Command(CommandType.LOAD));
        toReturn.add(Command.createMoveCommand(new Point(0.0, 0.0)));
        toReturn.add(new Command(CommandType.UNLOAD));
        
        return toReturn;
    }
}
