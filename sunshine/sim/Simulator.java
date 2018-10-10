package sunshine.sim;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Random;
import java.util.PriorityQueue;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import sunshine.sim.Point;
import sunshine.sim.Harvester;
import sunshine.sim.Tractor;
import sunshine.sim.Trailer;
import sunshine.sim.Command;
import sunshine.sim.CommandType;

public class Simulator {
    
    private static class MutableTractor implements Tractor
    {
        public int id;
        public Point location;
        public boolean hasBale;
        public MutableTrailer attachedTrailer;
        
        public MutableTractor(int id)
        {
            this.id = id;
            this.location = new Point(0, 0);
            this.hasBale = false;
            this.attachedTrailer = new MutableTrailer();
        }
        
        public int getId()
        {
            return id;
        }
        
        public Point getLocation()
        {
            return new Point(location.x, location.y);
        }
        
        public boolean getHasBale() {
            return hasBale;
        }
        
        public Trailer getAttachedTrailer()
        {
            return attachedTrailer;
        }
    }
    
    private static class MutableTrailer implements Trailer
    {
        public Point location;
        public int numBales;
        
        public MutableTrailer()
        {
            this.location = new Point(0, 0);
            this.numBales = 0;
        }
        
        public Point getLocation()
        {
            return new Point(location.x, location.y);
        }
        
        public int getNumBales()
        {
            return numBales;
        }
    }
    
    private static class CommandWrapper implements Comparable<CommandWrapper>
    {
        public MutableTractor tractor;
        public double completionTime;
        public double startTime;
        public Command command;
        
        public CommandWrapper(MutableTractor tractor, double completionTime, Command command)
        {
            this.tractor = tractor;
            this.completionTime = completionTime;
            this.startTime = elapsedSeconds;
            this.command = command;
        }
        
        public int compareTo(CommandWrapper other)
        {
            return (new Double(this.completionTime)).compareTo(other.completionTime);
        }
        
        public String toString()
        {
            return "COMMAND: Tractor " + this.tractor.id + " " + this.command.getType().name() + (command.getLocation() == null ? " " : " (" + command.getLocation().x + "," + command.getLocation().y + ")") + "- completing at " + this.completionTime;
        }
    }
    
    private static final String root = "sunshine";
    private static final String statics_root = "statics";

    private static boolean gui = false;

    private static double fps = 1;
    private static int n_runs = 1;
    private static int num_tractors;
    private static double t;
    private static int m;
    private static String playerName;
    private static int seed = -1;
    private static Random rand;

    private static PlayerWrapper player;
    
    private static ArrayList<Point> baleLocations;
    private static int numBales = -1;
    
    private static ArrayList<MutableTractor> tractors;
    private static ArrayList<MutableTrailer> trailers;
    
    private static PriorityQueue<CommandWrapper> pendingCommands;
    
    private static double elapsedSeconds = 0.0;
    private static double timeThreshold = 0.0;
    private static double timeStep = -1.0;

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        parseArgs(args);

        if (seed == -1)
        {
            rand = new Random();
        } else
        {
            rand = new Random(seed);
        }
        
        baleLocations = Harvester.harvest(rand, m);
        numBales = baleLocations.size();
        
        tractors = new ArrayList<MutableTractor>();
        trailers = new ArrayList<MutableTrailer>();
        pendingCommands = new PriorityQueue<CommandWrapper>();
        
        for (int i = 0; i < num_tractors; i++)
        {
            tractors.add(new MutableTractor(i));
        }
        
        try {
            player = loadPlayerWrapper(playerName);
        } catch (Exception ex) {
            System.out.println("Unable to load player. " + ex.getMessage());
            System.exit(0);
        }
        
        player.init((List<Point>) baleLocations.clone(), num_tractors, m, t);
        
        for (MutableTractor tractor : tractors)
        {
            Command command = player.getCommand(tractor);
            if (command == null)
            {
                continue;
            }
            CommandWrapper wrapper = new CommandWrapper(tractor, getDuration(command, tractor), command);
            System.out.println(wrapper.toString());
            pendingCommands.add(wrapper);
        }

        HTTPServer server = null;
        if (gui) {
            server = new HTTPServer();
            Log.record("Hosting HTTP Server on " + server.addr());
            if (!Desktop.isDesktopSupported())
                Log.record("Desktop operations not supported");
            else if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                Log.record("Desktop browse operation not supported");
            else {
                try {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + server.port()));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            
            gui(server, state(fps));
        }

        while (elapsedSeconds < t) {
            if (pendingCommands.size() == 0)
            {
                elapsedSeconds = t;
                break;
            }

            if (timeStep < 0.0) {
                timeThreshold = pendingCommands.peek().completionTime;
            } else {
                timeThreshold += timeStep;
            }
            while (pendingCommands.peek().completionTime <= timeThreshold) {
                CommandWrapper command = pendingCommands.poll();
                if (command.completionTime > t)
                {
                    elapsedSeconds = t;
                    break;
                }
                elapsedSeconds = command.completionTime;
                handleCompletedCommand(command, command.tractor);
                if (numBales == 0)
                {
                    break;
                }
                Command newCommand = player.getCommand(command.tractor);
                if (newCommand == null)
                {
                    continue;
                }
                CommandWrapper wrapper = new CommandWrapper(command.tractor, command.completionTime + getDuration(newCommand, command.tractor), newCommand);
                System.out.println(wrapper.toString());
                pendingCommands.add(wrapper);
            }
            if (gui) {
                gui(server, state(fps));
            }
            if (numBales == 0 || elapsedSeconds >= t)
            {
                break;
            }
        }
        if (numBales == 0)
        {
            System.out.println("Gathered all bales in " + elapsedSeconds + " seconds!");
        }
        else {
            System.out.println("Time's up!");
        }
        if (gui)
        {
            gui(server, state(fps));
            while (true)
            {

            }
        }
    }
    
    private static void handleCompletedCommand(CommandWrapper command, MutableTractor tractor)
    {
        System.out.println("(COMPLETED) " + command.toString());
        switch (command.command.getType())
        {
            case MOVE_TO:
                Point dest = command.command.getLocation();
                tractor.location = new Point(dest.x, dest.y);
                break;
            case DETATCH:
                if (tractor.attachedTrailer != null)
                {
                    tractor.attachedTrailer.location = new Point(tractor.location.x, tractor.location.y);
                    trailers.add(tractor.attachedTrailer);
                    tractor.attachedTrailer = null;
                }
                break;
            case ATTACH: {
                if (tractor.attachedTrailer == null)
                {
                    MutableTrailer closest = closestTrailer(tractor, false);
                    if (closest != null)
                    {
                        trailers.remove(closest);
                        tractor.attachedTrailer = closest;
                    }
                }
                break;
            }
            case LOAD: {
                if (tractor.hasBale) {
                    break;
                }
                Point closest = null;
                double minDist = Double.POSITIVE_INFINITY;
                for (Point p : baleLocations)
                {
                    double dist = dist(p, tractor.location);
                    if (dist < 1.0 && dist < minDist)
                    {
                        minDist = dist;
                        closest = p;
                    }
                }
                if (closest != null) {
                    baleLocations.remove(closest);
                    tractor.hasBale = true;
                }
                break;
            }
            case UNLOAD:
                if (tractor.hasBale)
                {
                    tractor.hasBale = false;
                    if (Math.sqrt(tractor.location.x * tractor.location.x + tractor.location.y * tractor.location.y) < 1.0)
                    {
                        numBales--;
                    }
                    else
                    {
                        baleLocations.add(tractor.location);
                    }
                }
                break;
            case STACK:
                if (tractor.hasBale) {
                    MutableTrailer closest = closestTrailer(tractor, false);
                    if (closest != null)
                    {
                        if (closest.numBales < 10)
                        {
                            tractor.hasBale = false;
                            closest.numBales++;
                        }
                    }
                }
                break;
            case UNSTACK: {
                MutableTrailer closest = closestTrailer(tractor, true);
                if (closest != null)
                {
                    if (closest.numBales > 0)
                    {
                        closest.numBales--;
                        tractor.hasBale = true;
                    }
                }
                break;
            }
        }
    }
    
    private static double getDuration(Command command, MutableTractor tractor)
    {
        switch (command.getType())
        {
            case MOVE_TO:
                Point end = command.getLocation();
                Point start = tractor.location;
                double speed = 10.0;
                if (tractor.getAttachedTrailer() != null)
                {
                    speed = 4.0;
                }
                double dist = dist(start, end);
                return dist / speed;
            case DETATCH:
            case ATTACH:
                return 60.0;
            case LOAD:
            case UNLOAD:
            case STACK:
            case UNSTACK:
                return 10.0;
        }
        return Double.NEGATIVE_INFINITY;
    }
    
    private static MutableTrailer closestTrailer(MutableTractor tractor, boolean needsBales)
    {
        MutableTrailer closest = null;
        double minDist = Double.POSITIVE_INFINITY;
        for (MutableTrailer trailer : trailers)
        {
            double dist = dist(trailer.location, tractor.location);
            if (dist < 1.0 && dist < minDist && (!needsBales || trailer.numBales > 0))
            {
                minDist = dist;
                closest = trailer;
            }
        }
        return closest;
    }
    
    private static double dist(Point a, Point b)
    {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    private static PlayerWrapper loadPlayerWrapper(String name) throws Exception {
        Log.record("Loading player " + name);
        Player p = loadPlayer(name);
        if (p == null) {
            Log.record("Cannot load player " + name);
            System.exit(1);
        }

        return new PlayerWrapper(p, name);
    }

    // The state that is sent to the GUI. (JSON)
    private static String state(double fps) {
        String json = "{ \"refresh\":" + (1000.0/fps) + ",\"m\":" + (double)m + ",\"t\":" + t + ",\"elapsed\":" + elapsedSeconds + ",\"remaining_bales\":" + numBales + ",";
        
        json += "\"bales\" : [";
        for (int i = 0; i < baleLocations.size(); i++)
        {
            Point p = baleLocations.get(i);
            json += "{\"x\" : " + p.x + ",\"y\" : " + p.y + "}";
            if (i != baleLocations.size() - 1)
            {
                json += ",";
            }
        }
        json += "],";
        
        json += "\"trailers\" : [";
        for (int i = 0; i < trailers.size(); i++)
        {
            MutableTrailer trailer = trailers.get(i);
            json += "{\"x\":" + trailer.location.x + ",\"y\":" + trailer.location.y + "}";
            if (i != trailers.size() - 1)
            {
                json += ",";
            }
        }
        json += "],";
        
        json += "\"tractors\" : [";
        for (int i = 0; i < tractors.size(); i++)
        {
            MutableTractor tractor = tractors.get(i);
            json += "{\"x\":" + tractor.location.x + ",\"y\":" + tractor.location.y + ",\"trailer\":" + (tractor.attachedTrailer != null) + ",\"bale\":" + tractor.hasBale + ",\"dest\":";
            boolean destNull = true;
            for (CommandWrapper c : pendingCommands)
            {
                if (c.tractor == tractor && c.command.getLocation() != null)
                {
                    double percentComplete = (c.startTime == elapsedSeconds) ? 0.0 : (elapsedSeconds - c.startTime) / (c.completionTime - c.startTime);
                    json += "{\"x\":" + c.command.getLocation().x + ",\"y\":" + c.command.getLocation().y + ",\"percent_complete\":" + percentComplete + "}";
                    destNull = false;
                }
            }
            if (destNull) {
                json += "null";
            }
            json += "}";
            if (i != tractors.size() - 1)
            {
                json += ",";
            }
        }
        json += "]}";
        
        return json;
    }

    private static String join(String joins, List<Integer> list) {
        return list.stream().map(Object::toString).collect(Collectors.joining(joins));
    }

    private static void gui(HTTPServer server, String content) {
        if (server == null) return;
        String path = null;
        for (;;) {
            for (;;) {
                try {
                    path = server.request();
                    break;
                } catch (IOException e) {
                    Log.record("HTTP request error " + e.getMessage());
                }
            }
            if (path.equals("data.txt")) {
                try {
                    server.reply(content);
                } catch (IOException e) {
                    Log.record("HTTP dynamic reply error " + e.getMessage());
                }
                return;
            }
            if (path.equals("")) path = "webpage.html";
            else if (!Character.isLetter(path.charAt(0))) {
                Log.record("Potentially malicious HTTP request \"" + path + "\"");
                break;
            }

            File file = new File(statics_root + File.separator + path);
            if (file == null) {
                Log.record("Unknown HTTP request \"" + path + "\"");
            } else {
                try {
                    server.reply(file);
                } catch (IOException e) {
                    Log.record("HTTP static reply error " + e.getMessage());
                }
            }
        }
    }

    private static void parseArgs(String[] args) {
        int i = 0;
        List<String> playerNames = new ArrayList<String>();
        for (; i < args.length; ++i) {
            switch (args[i].charAt(0)) {
                case '-':
                    if (args[i].equals("-p") || args[i].equals("--player")) {
                        while (i + 1 < args.length && args[i + 1].charAt(0) != '-') {
                            ++i;
                            playerNames.add(args[i]);
                        }

                        if (playerNames.size() != 1) {
                            throw new IllegalArgumentException("Invalid number of players, you need 1 player to start a game.");
                        }

                        playerName = playerNames.get(0);
                    } else if (args[i].equals("-g") || args[i].equals("--gui")) {
                        gui = true;
                    } else if (args[i].equals("-l") || args[i].equals("--logfile")) {
                        if (++i == args.length) {
                            throw new IllegalArgumentException("Missing logfile name");
                        }
                        Log.setLogFile(args[i]);
                    } else if (args[i].equals("--fps")) {
                        if (++i == args.length) {
                            throw new IllegalArgumentException("Missing frames per second.");
                        }
                        fps = Double.parseDouble(args[i]);
                    } else if (args[i].equals("-r") || args[i].equals("--runs")) {
                        if (++i == args.length) {
                            throw new IllegalArgumentException("Missing number of runs.");
                        }
                        n_runs = Integer.parseInt(args[i]);
                    } else if (args[i].equals("-v") || args[i].equals("--verbose")) {
                        Log.activate();
                    } else if (args[i].equals("-n") || args[i].equals("--num_tractors")) {
                        if (++i == args.length) {
                            throw new IllegalArgumentException("Missing number of tractors.");
                        }
                        num_tractors = Integer.parseInt(args[i]);
                    } else if (args[i].equals("-m")) {
                        if (++i == args.length) {
                            throw new IllegalArgumentException("Missing field size.");
                        }
                        m = Integer.parseInt(args[i]);
                    } else if (args[i].equals("-t")) {
                        if (++i == args.length) {
                            throw new IllegalArgumentException("Missing time limit.");
                        }
                        t = Double.parseDouble(args[i]);
                    } else if (args[i].equals("-s") || args[i].equals("--seed")) {
                        if (++i == args.length) {
                            throw new IllegalArgumentException("Missing random seed.");
                        }
                        seed = Integer.parseInt(args[i]);
                    } else if (args[i].equals("--time_step")) {
                        if (++i != args.length) {
                            timeStep = 1.0;
                        }
                        timeStep = Double.parseDouble(args[i]);
                    } else {
                        throw new IllegalArgumentException("Unknown argument '" + args[i] + "'");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument '" + args[i] + "'");
            }
        }

        //Log.record("Players: " + playerName.toString());
        Log.record("GUI " + (gui ? "enabled" : "disabled"));

        if (gui)
            Log.record("FPS: " + fps);
    }

    private static Set<File> directory(String path, String extension) {
        Set<File> files = new HashSet<File>();
        Set<File> prev_dirs = new HashSet<File>();
        prev_dirs.add(new File(path));
        do {
            Set<File> next_dirs = new HashSet<File>();
            for (File dir : prev_dirs)
                for (File file : dir.listFiles())
                    if (!file.canRead()) ;
                    else if (file.isDirectory())
                        next_dirs.add(file);
                    else if (file.getPath().endsWith(extension))
                        files.add(file);
            prev_dirs = next_dirs;
        } while (!prev_dirs.isEmpty());
        return files;
    }

    public static Player loadPlayer(String name) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String sep = File.separator;
        Set<File> player_files = directory(root + sep + name, ".java");
        File class_file = new File(root + sep + name + sep + "Player.class");
        long class_modified = class_file.exists() ? class_file.lastModified() : -1;
        if (class_modified < 0 || class_modified < last_modified(player_files) ||
                class_modified < last_modified(directory(root + sep + "sim", ".java"))) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null)
                throw new IOException("Cannot find Java compiler");
            StandardJavaFileManager manager = compiler.
                    getStandardFileManager(null, null, null);
//            long files = player_files.size();
            Log.record("Compiling for player " + name);
            if (!compiler.getTask(null, manager, null, null, null,
                    manager.getJavaFileObjectsFromFiles(player_files)).call())
                throw new IOException("Compilation failed");
            class_file = new File(root + sep + name + sep + "Player.class");
            if (!class_file.exists())
                throw new FileNotFoundException("Missing class file");
        }
        ClassLoader loader = Simulator.class.getClassLoader();
        if (loader == null)
            throw new IOException("Cannot find Java class loader");
        @SuppressWarnings("rawtypes")
        Class raw_class = loader.loadClass(root + "." + name + ".Player");
        return (Player)raw_class.newInstance();
    }

    private static long last_modified(Iterable<File> files) {
        long last_date = 0;
        for (File file : files) {
            long date = file.lastModified();
            if (last_date < date)
                last_date = date;
        }
        return last_date;
    }
}
