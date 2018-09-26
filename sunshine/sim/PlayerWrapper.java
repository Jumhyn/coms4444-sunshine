package sunshine.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import sunshine.sim.Tractor;

public class PlayerWrapper {
    private Timer thread;
    private Player player;
    private String name;

    public PlayerWrapper(Player player, String name) {
        this.player = player;
        this.name = name;
    }

    public void init(List<Point> bales, int n, double m, double t) {
        Log.record("Initializing player " + this.name);
        // Initializing ID mapping array
        
        this.player.init(bales, n, m, t);
    }

    public Command getCommand(Tractor tractor) {
        Log.record("Getting command for tractor " + tractor.getId());
        Command command = this.player.getCommand(tractor);
        return command;
    }

    public String getName() {
        return name;
    }
}
