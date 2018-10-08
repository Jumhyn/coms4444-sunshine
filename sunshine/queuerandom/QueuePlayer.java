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


public abstract class QueuePlayer implements sunshine.sim.Player {
   
    private ArrayList<ArrayList<Command>> tractorQueues;
    public abstract ArrayList<Command> getMoreCommands(Tractor tractor);
    
    public void init(List<Point> bales, int n, double m, double t)
    {
        tractorQueues = new ArrayList<ArrayList<Command>>();
        for (int i = 0; i < n; i++)
        {
            tractorQueues.add(new ArrayList<Command>());
        }
    }
    
    public Command getCommand(Tractor tractor)
    {
        ArrayList<Command> pendingCommands = tractorQueues.get(tractor.getId());
        if (pendingCommands.size() == 0) {
            pendingCommands = this.getMoreCommands(tractor);
            if (pendingCommands == null || pendingCommands.size() == 0)
            {
                return null;
            }
            tractorQueues.set(tractor.getId(), pendingCommands);
        }
        return pendingCommands.remove(0);
    }
}
