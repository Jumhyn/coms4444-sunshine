package sunshine.g4;

import java.util.*;
import java.util.List;
import java.lang.Math;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;


public class Player implements sunshine.sim.Player {

    List<Point> bales;
    List<Point> far_bales;
    List<Point> close_bales;

    private Map<Integer, Integer> tractor_mode;
    private Map<Integer, List<Point>> away_tractor;
    private List<Integer> close_tractor;
    private Map<Integer, Point> trailer_pos;
    private Map<Integer, Integer> trailer_with_bales;

    private int threshold = 450;
    private Random rand;

    public Player() {
        close_bales = new ArrayList<Point>();
        far_bales = new ArrayList<Point>();

        close_tractor = new ArrayList<Integer>();
        away_tractor = new HashMap<Integer, List<Point>>();

        trailer_pos = new HashMap<Integer, Point>();
        tractor_mode = new HashMap<Integer, Integer>();
        trailer_with_bales = new HashMap<Integer, Integer>();
    }

    public double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }


    public void init(List<Point> bales, int n, double m, double t) {
        this.bales = bales;
        for (int i = 0; i < bales.size(); i++) {
            if (dist(bales.get(i).x, bales.get(i).y, 0, 0) > threshold) {
                far_bales.add(bales.get(i));
            } else {
                close_bales.add(bales.get(i));
            }
        }

        Collections.sort(close_bales, new EuclideanDescComparator());
        Collections.sort(far_bales, new EuclideanDescComparator());
        System.out.println(far_bales.size());
        System.out.println(close_bales.size());
        
        int tractor_threshold = n * close_bales.size() / bales.size();
        for (int i = 0; i < n; i++) {
            if (i < tractor_threshold) {
                close_tractor.add(i);
                tractor_mode.put(i, 0);
                // state 0, needs to detach
            } else {
                tractor_mode.put(i, 1);
                // state 1, ready to go
            }
        }
        System.out.println(far_bales.size());
        System.out.println(close_bales.size());
        
    }


    public Point center(List<Point> a) {
        double x_s = 0;
        double y_s = 0;

        for (int i = 0; i < a.size(); i++) {
            x_s += a.get(i).x;
            y_s += a.get(i).y;
        }
        return new Point(x_s / new Double(a.size()), y_s / new Double(a.size()));
    }

    public Command getCommand(Tractor tractor) {
        int id = tractor.getId();
        
        switch (tractor_mode.get(id)) {
            // at 0,0, need to detatch
            case 0:
                tractor_mode.put(id, 1);
                return new Command(CommandType.DETATCH);

            // at 0,0, ready to go do task
            case 1:
                if (close_tractor.contains(id)) {
                    if (close_bales.size() > 0) {
                        tractor_mode.put(id, 2);
                        return Command.createMoveCommand(close_bales.remove(0));
                    } else {
                        if (far_bales.size() > 0) {
                            tractor_mode.put(id, 2);
                            return Command.createMoveCommand(far_bales.remove(0));
                        }
                        else{
                            return new Command(CommandType.UNLOAD);
                        }
                    }
                } else {
                    if (far_bales.size() > 0) {
                        tractor_mode.put(id, 3);
                        Cluster myCluster = getClusters(far_bales, 10-tractor.getAttachedTrailer().getNumBales());
                        if (tractor.getHasBale()){
                            System.out.println("error");
                            return new Command(CommandType.UNLOAD);
                        }

                        if (myCluster.nodes == null){
                            return new Command(CommandType.UNSTACK);
                        }
                        away_tractor.put(id, myCluster.nodes);
                        for (Point node : myCluster.nodes) {
                            if (far_bales.contains(node)) {
                                far_bales.remove(node);
                            }
                        }
                        Collections.sort(far_bales, new EuclideanDescComparator());
//                        return Command.createMoveCommand(center(away_tractor.get(id)));
                        return Command.createMoveCommand(myCluster.center);
                    } else {
                        if (close_bales.isEmpty()) return new Command(CommandType.UNSTACK);
                        away_tractor.remove(id);
                        close_tractor.add(id);
                        tractor_mode.put(id, 2);
                        return Command.createMoveCommand(close_bales.remove(0));
                    }
                }

                // load up close bayz
            case 2:
                tractor_mode.put(id, 8);
                return new Command(CommandType.LOAD);

            // detatch the trailer and keep it in trailer_pos
            case 3:
                tractor_mode.put(id, 4);
                trailer_pos.put(id, tractor.getLocation());
                return new Command(CommandType.DETATCH);

            // try to find the next bay
            case 4:
                tractor_mode.put(id, 5);
                return Command.createMoveCommand(away_tractor.get(id).remove(0));


            // try to load up the bay
            case 5:
                tractor_mode.put(id, 6);
                return new Command(CommandType.LOAD);

            // return to the trailer
            case 6:
                tractor_mode.put(id, 7);
                return Command.createMoveCommand(trailer_pos.get(id));

            // stack up
            case 7:
                if (away_tractor.get(id).size() == 0) {
                    trailer_pos.remove(id);
                    tractor_mode.put(id, 8);
                    return new Command(CommandType.ATTACH);
                } else {
                    tractor_mode.put(id, 4);
                    return new Command(CommandType.STACK);
                }

                // return to origin
            case 8:
                tractor_mode.put(id, 9);
                return Command.createMoveCommand(new Point(0, 0));

            // unload first
            case 9:
                if (close_tractor.contains(id)) {
                    tractor_mode.put(id, 1);
                    return new Command(CommandType.UNLOAD);
                } else {    
                    tractor_mode.put(id, 10);
                    return new Command(CommandType.UNLOAD);
                }

                // detach the trailer
            case 10:
                tractor_mode.put(id, 11);
                trailer_with_bales.put(id, 10);
                return new Command(CommandType.DETATCH);

            case 11:
                tractor_mode.put(id, 12);
                return new Command(CommandType.UNSTACK);
                
            case 12:
                if (trailer_with_bales.get(id)>1){
                    tractor_mode.put(id, 11);
                    trailer_with_bales.put(id, trailer_with_bales.get(id) - 1);
                }
                else{
                    tractor_mode.put(id, 13);
                    trailer_with_bales.remove(id);
                }
                return new Command(CommandType.UNLOAD);

            case 13:
                tractor_mode.put(id, 1);
                return new Command(CommandType.ATTACH);

        }
        return Command.createMoveCommand(new Point(0, 0));
    }

    /**
     * cluster helper functions
     **/

    private class EuclideanDescComparator implements Comparator<Point> {
        @Override
        public int compare(Point p1, Point p2) {
            return (int) (((p1.x * p1.x + p1.y * p1.y) - (p2.x * p2.x + p2.y * p2.y)) * -10000);
        }
    }

    private class RelativeEuclideanAscComparator implements Comparator<Point> {

        private Point p;

        public RelativeEuclideanAscComparator(Point pivot) {
            super();
            p = pivot;
        }

        @Override
        public int compare(Point p1, Point p2) {
            return (int) ((((p1.x - p.x) * (p1.x - p.x) + (p1.y - p.y) * (p1.y - p.y)) - ((p2.x - p.x) * (p2.x - p.x) + (p2.y - p.y) * (p2.y - p.y))) * 10000);
        }
    }

    //return the next cluster list and center
    private Cluster getClusters(List<Point> inputBales, int k) {  // k bales per cluster
        List<Point> result = new ArrayList<>();
        if (inputBales.isEmpty()) return null;
        Point pivot = inputBales.get(0);
        result.add(pivot);
        Collections.sort(inputBales, new RelativeEuclideanAscComparator(pivot));
        for (int i = 1; i < k+1 && i < inputBales.size(); i++) {
            result.add(inputBales.get(i));
        }
        return new Cluster(result, null);
    }

    // Determine what the threshold should be.
    private int getThreshold() {
        return threshold;
    }
}
