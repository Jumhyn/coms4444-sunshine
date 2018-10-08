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
        rand = new Random(0);
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


    public List<Point> cluster(Point p) {
        List<Double> distances = new ArrayList<Double>();
        List<Double> clone = new ArrayList<Double>();
        for (int index = 0; index < far_bales.size(); index++) {
            double distance = dist(p.x, p.y, far_bales.get(index).x, far_bales.get(index).y);
            distances.add(distance);
            clone.add(distance);
        }
        Collections.sort(distances);
        int K = Math.min(far_bales.size(), 10);
        List<Double> Neighbor = distances.subList(0, K);
        List<Point> res = new ArrayList<Point>();
        res.add(p);
        for (double element : Neighbor) {
            Integer ind = clone.indexOf(element);
            res.add(far_bales.get(ind));
            far_bales.remove(ind);
        }
        return res;
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

        int tractor_threshold = n * close_bales.size() / bales.size();
        for (int i = 0; i < n; i++) {
            if (i < tractor_threshold) {
                close_tractor.add(i);
                tractor_mode.put(i, 0);
                // state 0, needs to detach
            } else {
                Point p = far_bales.remove(rand.nextInt(far_bales.size()));
                away_tractor.put(i, cluster(p));
                tractor_mode.put(i, 1);
                // state 1, ready to go
            }
        }
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
                        System.out.println(1);
                        tractor_mode.put(id, 2);
                        return Command.createMoveCommand(close_bales.remove(0));
                    } else {
                        if (far_bales.size() > 0) {
                            System.out.println("all close bales collected");
                            tractor_mode.put(id, 2);
                            return Command.createMoveCommand(far_bales.remove(0));
                        }
                    }
                } else {
                    System.out.println(3);
                    if (far_bales.size() > 0) {
                        Point p = far_bales.remove(rand.nextInt(far_bales.size()));
                        tractor_mode.put(id, 3);
//                        away_tractor.put(id, cluster(p));
                        Cluster myCluster = getClusters(far_bales, 11);
                        away_tractor.put(id, myCluster.nodes);
                        for(int i = 0; i < far_bales.size(); i++){
                            if(myCluster.nodes.contains(far_bales.get(i))){
                                far_bales.remove(i);
                            }
                        }
                        return Command.createMoveCommand(center(away_tractor.get(id)));
                    } else {
                        System.out.println(4);
                        away_tractor.remove(id);
                        close_tractor.add(id);
                        tractor_mode.put(id, 2);
                        return Command.createMoveCommand(close_bales.remove(0));
                    }
                }

                // load up close bay
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
                /*System.out.println(String.format("%f = %f",tractor.getLocation().x, tractor.getLocation().y));
                for (Map.Entry<Integer, Point> pair : trailer_pos.entrySet()) {
                    System.out.println(String.format("%d = %f %f",pair.getKey(), pair.getValue().x, pair.getValue().y));
                }*/
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
                    if (tractor.getAttachedTrailer() == null) {
                        tractor_mode.put(id, 11);
                    } else {
                        tractor_mode.put(id, 10);
                    }
                    return new Command(CommandType.UNLOAD);
                }

                // detach the trailer
            case 10:
                tractor_mode.put(id, 11);
                trailer_with_bales.put(id, 10);
                return new Command(CommandType.DETATCH);

            case 11:
                if (trailer_with_bales.get(id) > 1) {
                    tractor_mode.put(id, 9);
                    trailer_with_bales.put(id, trailer_with_bales.get(id) - 1);
                    return new Command(CommandType.UNSTACK);
                } else {
                    tractor_mode.put(id, 12);
                    trailer_with_bales.remove(id);
                    return new Command(CommandType.UNSTACK);
                }

            case 12:
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
            return (int) (((p1.x * p1.x + p1.y * p1.y) - (p2.x * p2.x + p2.y * p2.y)) * 1000);
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
            return (int) ((((p1.x - p.x) * (p1.x - p.x) + (p1.y - p.y) * (p1.y - p.y)) - ((p2.x - p.x) * (p2.x - p.x) + (p2.y - p.y) * (p2.y - p.y))) * -1000);
        }
    }

    private double Euclidean(Point p1, Point p2) {
        return (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
    }

    //return the next cluster list and center
    private Cluster getClusters(List<Point> inputBales, int k) {  // k bales per cluster
        List<Point> bales = new ArrayList<>(inputBales);
        List<Point> result = new ArrayList<>();
        if (bales.isEmpty()) return null;
        Collections.sort(bales, new EuclideanDescComparator()); // change to max() for optimization
        Point pivot = bales.get(0);
        Collections.sort(bales, new RelativeEuclideanAscComparator(pivot));
        for (int i = 0; i < k && i < bales.size(); i++){
            result.add(bales.get(i));
        }
        return new Cluster(result, null);
    }

    // Determine what the threshold should be.
    private int getThreshold() {
        return threshold;
    }
}
