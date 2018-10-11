package sunshine.g4;

import java.util.*;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.Trailer;
import sunshine.sim.CommandType;
import sunshine.sim.Point;


public class Player implements sunshine.sim.Player {

    List<Point> bales;
    List<Point> todo;

    public double num = 0.0;
    private Map<Integer, Integer> tractors;
    private Map<Point, Integer> trailers;
    private Map<Integer, Double> time;
    public int b = 0;

    public Player() {
        trailers = new HashMap<Point, Integer>();
        tractors = new HashMap<Integer, Integer>();
        time = new HashMap<Integer, Double>();
    }

    public double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public double dist(Point a, Point b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }

    public Point NN(Point c) {
        double min_dist = 10000;
        Point best = null;
        for (Point k : todo) {
            if (dist(k, c) < min_dist) {
                min_dist = dist(k, c);
                best = k;
            }
        }
        return best;
    }

    private Point closestTrailer(Point loc, boolean ishome) {
        Point closest = null;
        double minDist = Double.POSITIVE_INFINITY;
        for (Point trailer : trailers.keySet()) {
            if (!ishome && dist(trailer, new Point(0, 0)) < 1) {
                continue;
            }
            if (ishome && dist(trailer, new Point(0, 0)) > 1) {
                continue;
            }
            double dist = dist(trailer, loc);
            if (dist < minDist) {
                minDist = dist;
                closest = trailer;
            }
        }
        return closest;
    }

    public void init(List<Point> bales, int n, double m, double t) {
        this.bales = bales;
        todo = new ArrayList<Point>();
        Collections.sort(bales, new EuclideanDescComparator());
        this.num = n;
        for (int i = 0; i < n; i++) {
            tractors.put(i, 0);
            time.put(i, 0.0);
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
        int s = 0;
        for (Point t : trailers.keySet()) {
            if (trailers.get(t) != 0) {
                s += 1;
            }
        }

        System.out.printf("trailers in the field : %d\n", s);


        Point dest = null;

        switch (tractors.get(id)) {
            // at 0,0, ready to go
            case 0:
                if (tractor.getHasBale()) {
                    time.put(id, time.get(id) + 10);
                    System.out.printf("tractor %d UNLOADING at %.2f\n", id, time.get(id));
                    b = b - 1;
                    return new Command(CommandType.UNLOAD);
                } else {
                    if (tractor.getAttachedTrailer() == null) {
                        b = b - 1;
                        return new Command(CommandType.UNLOAD);
                    }
                    if (tractor.getAttachedTrailer().getNumBales() != 0) {
                        System.out.printf("ERROR WITH a trailer with %d\n", tractor.getAttachedTrailer().getNumBales());

                    }
                    if (bales.size() == 0) {
                        return new Command(CommandType.UNLOAD);
                    }
                    Cluster myCluster = getClusters(bales, 10 - tractor.getAttachedTrailer().getNumBales());

                    for (Point node : myCluster.nodes) {
                        todo.add(node);
                        bales.remove(node);
                    }
                    Collections.sort(bales, new EuclideanDescComparator());
                    tractors.put(id, 1);
                    time.put(id, time.get(id) + dist(tractor.getLocation(), center(myCluster.nodes)) / 4);
                    System.out.printf("tractor %d depart completed at %.2f\n", id, time.get(id));
                    return Command.createMoveCommand(center(myCluster.nodes));
                }

                // detaching
            case 1:
                trailers.put(tractor.getLocation(), 0);
                tractors.put(id, 2);
                time.put(id, time.get(id) + 60);
                System.out.printf("tractor %d detaching completed at %.2f\n", id, time.get(id));
                return new Command(CommandType.DETATCH);

            // load up close bay
            case 2:
                if (todo.size() == 0) {
                    if (trailers.size() > 0) {
                        dest = closestTrailer(tractor.getLocation(), false);
                        tractors.put(id, 5);
                        time.put(id, time.get(id) + dist(tractor.getLocation(), dest) / 10);
                        System.out.printf("tractor %d find trailers at %.2f, %.2f\n", id, dest.x, dest.y);
                        System.out.printf("tractor %d move completed at %.2f\n", id, time.get(id));
                        return Command.createMoveCommand(dest);
                    }
                    tractors.put(id, 9);
                    time.put(id, time.get(id) + dist(tractor.getLocation(), new Point(0, 0)) / 10);
                    System.out.printf("tractor %d return completed at %.2f\n", id, time.get(id));
                    return Command.createMoveCommand(new Point(0, 0));
                } else {
                    dest = NN(tractor.getLocation());
                    todo.remove(dest);
                    b = b + 1;
                    tractors.put(id, 3);
                    time.put(id, time.get(id) + dist(tractor.getLocation(), dest) / 10);
                    System.out.printf("tractor %d collect completed at %.2f\n", id, time.get(id));
                    return Command.createMoveCommand(dest);
                }

                // detatch the trailer and keep it in trailers
            case 3:
                tractors.put(id, 4);
                time.put(id, time.get(id) + 10);
                System.out.printf("tractor %d load completed at %.2f\n", id, time.get(id));
                return new Command(CommandType.LOAD);

            // try to find the nearest trailer
            case 4:
                if (trailers.size() > 0) {
                    tractors.put(id, 5);
                    dest = closestTrailer(tractor.getLocation(), false);

                    time.put(id, time.get(id) + dist(tractor.getLocation(), dest) / 10);
                    System.out.printf("tractor %d find trailers at %.2f, %.2f\n", id, dest.x, dest.y);
                    System.out.printf("tractor %d move completed at %.2f\n", id, time.get(id));
                    return Command.createMoveCommand(dest);
                } else {
                    tractors.put(id, 0);
                    dest = new Point(0, 0);
                    time.put(id, time.get(id) + dist(tractor.getLocation(), dest) / 10);
                    System.out.printf("tractor %d all trailers gone at %.2f\n", id, time.get(id));
                    System.out.printf("tractor %d load completed at %.2f\n", id, time.get(id));
                    return Command.createMoveCommand(dest);
                }

                // try to load up the bay
            case 5:
                if (trailers.containsKey(tractor.getLocation())) {
                    if (trailers.get(tractor.getLocation()) == 10) {
                        tractors.put(id, 6);
                        time.put(id, time.get(id) + 60);
                        b = b + 10;
                        trailers.remove(tractor.getLocation());
                        System.out.printf("tractor %d Attaching Trailer at (%.2f, %.2f) , %.2f\n", id, tractor.getLocation().x, tractor.getLocation().y, time.get(id));
                        return new Command(CommandType.ATTACH);
                    } else {
                        tractors.put(id, 2);
                        trailers.put(tractor.getLocation(), trailers.get(tractor.getLocation()) + 1);
                        time.put(id, time.get(id) + 10);

                        System.out.printf("tractor %d stacking at %.2f\n", id, time.get(id));
                        b = b - 1;
                        return new Command(CommandType.STACK);
                    }
                } else {
                    tractors.put(id, 4);
                    return Command.createMoveCommand(tractor.getLocation());
                }


                // return to the origin
            case 6:
                if (tractor.getAttachedTrailer() != null) {
                    System.out.printf("trailer taken home stacking at %.2f\n", time.get(id));
                } else {
                    System.out.println("ERROR");

                }
                dest = new Point(id / (2 * num), id / (2 * num));
                time.put(id, time.get(id) + dist(tractor.getLocation(), dest) / 4);

                if (tractor.getHasBale()) {
                    tractors.put(id, 7);
                } else {
                    tractors.put(id, 8);
                }
                System.out.printf("tractor %d return to (%.2f, %.2f) , %.2f\n", id, dest.x, dest.y, time.get(id));
                return Command.createMoveCommand(dest);

            // unload first
            case 7:
                tractors.put(id, 8);
                if (tractor.getAttachedTrailer() == null) {
                    tractors.put(id, 9);
                }

                time.put(id, time.get(id) + 10);
                b = b - 1;
                System.out.printf("unload at %.2f\n", time.get(id));
                return new Command(CommandType.UNLOAD);

            // detach
            case 8:
                tractors.put(id, 9);
                b = b - 10;
                trailers.put(tractor.getLocation(), 10);
                time.put(id, time.get(id) + 60);
                System.out.printf("detach at %.2f\n", time.get(id));
                return new Command(CommandType.DETATCH);

            // unstack
            case 9:
                dest = closestTrailer(tractor.getLocation(), true);
                if (trailers.get(dest) == 0) {
                    if (bales.size() > num * 10) {
                        tractors.put(id, 0);
                        time.put(id, time.get(id) + 60);
                        System.out.printf("Attaching Trailer at (%.2f, %.2f) , %.2f\n", tractor.getLocation().x, tractor.getLocation().y, time.get(id));
                        return new Command(CommandType.ATTACH);
                    } else {
                        if (bales.size() == 0) {
                            return new Command(CommandType.UNLOAD);
                        }
                        dest = bales.remove(0);
                        tractors.put(id, 10);

                        time.put(id, time.get(id) + dist(tractor.getLocation(), dest) / 10);
                        System.out.printf("moving at %.2f\n", time.get(id));
                        return Command.createMoveCommand(dest);
                    }
                } else {
                    trailers.put(dest, trailers.get(dest) - 1);
                    tractors.put(id, 7);
                    time.put(id, time.get(id) + 10);
                    b = b + 1;
                    System.out.printf("unstack at %.2f\n", time.get(id));
                    return new Command(CommandType.UNSTACK);
                }

            case 10:
                b = b + 1;
                tractors.put(id, 11);
                return new Command(CommandType.LOAD);
            case 11:
                tractors.put(id, 12);
                dest = new Point(0, 0);
                return Command.createMoveCommand(dest);
            case 12:
                tractors.put(id, 9);
                b = b - 1;
                return new Command(CommandType.UNLOAD);

        }
        return new Command(CommandType.UNLOAD);
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

    private double Euclidean(Point p1, Point p2) {
        return (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
    }

    //return the next cluster list and center
    private Cluster getClusters(List<Point> inputBales, int k) {  // k bales per cluster
        List<Point> result = new ArrayList<>();
        if (inputBales.isEmpty()) return null;
        Point pivot = inputBales.get(0);
        result.add(pivot);
        Collections.sort(inputBales, new RelativeEuclideanAscComparator(pivot));
        for (int i = 1; i < k + 1 && i < inputBales.size(); i++) {
            result.add(inputBales.get(i));
        }
        return new Cluster(result, null);
    }

}
