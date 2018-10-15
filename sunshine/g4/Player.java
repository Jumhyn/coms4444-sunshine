package sunshine.g4;

import java.util.*;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.Trailer;
import sunshine.sim.CommandType;
import sunshine.sim.Point;


public class Player implements sunshine.sim.Player {
    
    List<Point> bales;
    
    public double num = 0.0;
    private Map<Integer, Integer> tractors;
    private Map<Integer, List<Point>> todo;
    private Map<Integer, Point> pair;

    private Map<Point, Integer> trailers;
    private Map<Integer, Double> time;
    private int threshold = 350;

    public int b = 0;

    public Player() {
        trailers = new HashMap<Point, Integer>();
        tractors = new HashMap<Integer, Integer>();
        time = new HashMap<Integer, Double>();
    }

    public Point approx(Point loc, Point dest){
      double slope = (dest.y-loc.y)/(dest.x-loc.x);
      double dx = Math.sqrt(0.9/(1+slope*slope));
      double dy = dx*slope;
      if (dy < 0){
        dy = dy*(-1);
      }
      if (dest.x > loc.x){
        dx = dest.x - dx;
      }
      else{
        dx = dest.x + dx;
      }
      if (dest.y > loc.y){
        dy = dest.y - dy;
      }
      else{
        dy = dest.y + dy;
      }
      return new Point(dx, dy);
    }

    public double dist(double x1, double y1, double x2, double y2) {
      return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public double dist(Point a, Point b) {
      return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }
/*
    private Point closestTrailer(Point loc, boolean ishome){
        Point closest = null;
        double minDist = Double.POSITIVE_INFINITY;
        for (Point trailer : trailers.keySet())
        {
          if (!ishome && dist(trailer,new Point(0,0))<1){
            continue;
          }
          if (ishome && dist(trailer,new Point(0,0))>1){
            continue;
          }
          double dist = dist(trailer,loc);
          if (dist < minDist )
          {
            minDist = dist;
            closest = trailer;
          }
        }
        return closest;
    }
*/

    private Point closest_bale(Point loc){
      double bdist = 10000;
      Point bp = null;  
      for (Point bale: bales){
        if (dist(bale, loc)<bdist){
          bdist = dist(bale, loc);
          bp = bale;
        }
      }
      return bp;
    }




    public void init(List<Point> bales, int n, double m, double t) {
        this.bales = bales;
        todo = new HashMap<Integer, List<Point>>();
        pair = new HashMap<Integer, Point>();
        Collections.sort(bales, new EuclideanDescComparator());
        this.num = n;
        for (int i = 0; i < n; i++) {
            tractors.put(i,0);
            time.put(i,0.0);
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
        

        
        Point dest = null;
        //System.out.printf("Tractor %d state %d\n", id, tractors.get(id));  
        
        switch (tractors.get(id)) {
            // at 0,0, ready to go
            case 0:
                if (tractor.getHasBale()){
                    time.put(id, time.get(id)+10);
                    //System.out.printf("tractor %d UNLOADING at %.2f\n", id, time.get(id));
                    b = b-1;
                    return new Command(CommandType.UNLOAD);
                }
                else{
                    if (tractor.getAttachedTrailer() == null){
                      b = b-1;
                      return new Command(CommandType.UNLOAD);
                    }
                    if (tractor.getAttachedTrailer().getNumBales()!=0){
                      //System.out.printf("ERROR WITH a trailer with %d\n", tractor.getAttachedTrailer().getNumBales());

                    }
                    if (bales.size()==0){
                      return new Command(CommandType.UNLOAD);
                    }
                    Cluster myCluster = getClusters(bales, 10-tractor.getAttachedTrailer().getNumBales());
                    todo.put(id, myCluster.nodes);
                    
                    for (Point node: myCluster.nodes){
                        bales.remove(node);
                    }
                    Collections.sort(bales, new EuclideanDescComparator());
                    tractors.put(id, 1);
                    time.put(id, time.get(id)+dist(tractor.getLocation(), center(myCluster.nodes))/4);
                    //System.out.printf("tractor %d depart completed at %.2f\n", id, time.get(id));
                    return Command.createMoveCommand(center(myCluster.nodes));                           
                }

            // detaching
            case 1:
                trailers.put(tractor.getLocation(), 0);
                tractors.put(id,2);
                pair.put(id, tractor.getLocation());
                time.put(id, time.get(id)+60);                    
                //System.out.printf("tractor %d detaching completed at %.2f\n", id, time.get(id));
                return new Command(CommandType.DETATCH);


            // load up close bay
            case 2:
                if (tractor.getHasBale()){
                  //System.out.printf("ERROR tractor %d has bale and try to look for more \n", id);  
                }
                if (todo.get(id).size() == 0){
                    dest = pair.get(id);
                    tractors.put(id, 5);
                    time.put(id, time.get(id)+dist(tractor.getLocation(), dest)/10);
                    
                    //System.out.printf("tractor %d find trailers at %.2f, %.2f\n", id, dest.x,dest.y);
                    //System.out.printf("tractor %d move completed at %.2f\n", id, time.get(id));
                    return Command.createMoveCommand(dest); 
               
                }
                else{
                    b = b+1;
                    tractors.put(id,3);
                    Point temp = approx(tractor.getLocation(), todo.get(id).get(0));
                    if (closest_bale(temp) == todo.get(id).get(0)){
                      dest = temp;
                      todo.get(id).remove(0);
                    }
                    else{
                      dest = todo.get(id).remove(0);
                    }
                    //dest = todo.get(id).remove(0);
                    time.put(id, time.get(id)+dist(tractor.getLocation(), dest)/10);
                    //System.out.printf("tractor %d go to %.2f, %.2f collect balescompleted at %.2f\n", id, dest.x, dest.y, time.get(id));
                    return Command.createMoveCommand(dest);
                }

            // detatch the trailer and keep it in trailers
            case 3:
                if (tractor.getHasBale()){
                  //System.out.printf("ERROR tractor %d has bale and try to load again\n", id);  
                }
                tractors.put(id, 4);
                time.put(id, time.get(id)+10);
                //System.out.printf("tractor %d load completed at %.2f\n", id, time.get(id));
                return new Command(CommandType.LOAD);

            // try to find the nearest trailer
            case 4:
                if (!tractor.getHasBale()){
                  //System.out.printf("ERROR tractor %d fail to load bale\n", id);  
                }

                if (pair.containsKey(id)){
                  tractors.put(id, 5);
                  dest = pair.get(id);
                  time.put(id, time.get(id)+dist(tractor.getLocation(), dest)/10);
                  //System.out.printf("tractor %d find trailers at %.2f, %.2f\n", id, dest.x,dest.y);
                  //System.out.printf("tractor %d move completed at %.2f\n", id, time.get(id));
                  return Command.createMoveCommand(dest);
                }
                else{
                  tractors.put(id, 0);
                  dest = new Point(0,0);
                  time.put(id, time.get(id)+dist(tractor.getLocation(), dest)/10);
                  //System.out.printf("tractor %d all trailers gone at %.2f\n", id,time.get(id));
                  //System.out.printf("tractor %d load completed at %.2f\n", id,time.get(id));
                  return Command.createMoveCommand(dest);
                }

            // try to stack up the bay
            case 5:
                if (!tractor.getHasBale()){
                  //System.out.printf("ERROR tractor %d come stacking empty\n", id);  
                }
                if (dist(pair.get(id),tractor.getLocation())<=1){
                  if (trailers.get(tractor.getLocation()) == 10){
                      tractors.put(id, 6);
                      time.put(id, time.get(id)+60);
                      b=b+10;
                      trailers.remove(tractor.getLocation());
                      pair.remove(id);
                      //System.out.printf("tractor %d Attaching Trailer at (%.2f, %.2f) , %.2f\n", id,tractor.getLocation().x,tractor.getLocation().y,time.get(id));
                      return new Command(CommandType.ATTACH);
                  }
                  else{
                      tractors.put(id, 2);
                      trailers.put(tractor.getLocation(),trailers.get(tractor.getLocation())+1);
                      time.put(id, time.get(id)+10);
                      if (tractor.getHasBale()==false){
                        //System.out.printf("ERROR : NOTHING TO STACK");
                      }
                      //System.out.printf("tractor %d stacking at %.2f\n", id, time.get(id));
                      b = b-1;
                      return new Command(CommandType.STACK);
                  }
                }
                else{
                  //System.out.printf("ERROR\n");
                  //System.out.printf("%.2f %.2f not equal to %.2f %.2f\n", tractor.getLocation().x, tractor.getLocation().y, pair.get(id).x, pair.get(id).y);
                  tractors.put(id,4);
                  return Command.createMoveCommand(pair.get(id));
                }

                
            // return to the origin
            case 6:
                if (tractor.getAttachedTrailer()!=null){
                  //System.out.printf("trailer taken home stacking at %.2f\n", time.get(id));
                }
                else{
                  //System.out.println("ERROR");
                }
                if (tractor.getAttachedTrailer().getNumBales()!=10){
                  //System.out.printf("ERROR trailer %d not full taken home\n", id);
                }
                

                dest = new Point(id/(2*num),Math.sqrt(0.9-id/(2*num)));
                time.put(id, time.get(id)+dist(tractor.getLocation(), dest)/4);

                if (tractor.getHasBale()){
                  tractors.put(id, 7);  
                }
                else{
                  tractors.put(id, 8);
                }
                //System.out.printf("tractor %d return to (%.2f, %.2f) , %.2f\n", id, dest.x,dest.y,time.get(id));
                return Command.createMoveCommand(dest); 
            
            // unload first
            case 7:
                if (!tractor.getHasBale()){
                  //System.out.printf("ERROR 2 tractor %d has no bale\n", id);  
                
                }
                tractors.put(id, 8);
                if (tractor.getAttachedTrailer() == null){
                  tractors.put(id, 9);
                }

                time.put(id, time.get(id)+10);
                b = b-1;
                //System.out.printf("unload at %.2f\n",time.get(id));
                return new Command(CommandType.UNLOAD);

            // detach
            case 8:
                if (tractor.getHasBale()){
                  //System.out.printf("ERROR 3 tractor %d has bale\n", id);  
                
                }
                tractors.put(id, 9);
                b=b-10;
                trailers.put(tractor.getLocation(), 10);
                time.put(id, time.get(id)+60);
                //System.out.printf("detach at %.2f\n", time.get(id));    
                return new Command(CommandType.DETATCH);

            // unstack
            case 9:
                if (trailers.get(tractor.getLocation()) == 0)
                {
                  if (bales.size() == 0){
                    return new Command(CommandType.UNLOAD);
                  }
                  if (bales.size() > num*10 && dist(new Point(0,0),bales.get(0)) > threshold){
                    tractors.put(id, 0);
                    time.put(id, time.get(id)+60);
                    //System.out.printf("Attaching Trailer at (%.2f, %.2f) , %.2f\n", tractor.getLocation().x,tractor.getLocation().y,time.get(id));
                    return new Command(CommandType.ATTACH);
                  }
                  else{
                    if (bales.size()==0){
                      tractors.put(id,13);
                      return new Command(CommandType.UNLOAD);
                    }
                    if (tractor.getHasBale()){
                      //System.out.printf("ERROR Tractor %d UNLOAD FAILED", id);
                    }
                    Point temp = approx(tractor.getLocation(), bales.get(0));
                    if (closest_bale(temp) == bales.get(0)){
                      dest = temp;
                      bales.remove(0);
                    }
                    else{
                      dest = bales.remove(0);
                    }
                    
                    //dest = bales.remove(0);
                    tractors.put(id, 10);

                    time.put(id, time.get(id)+dist(tractor.getLocation(),dest)/10);
                    //System.out.printf("moving at %.2f\n", time.get(id));
                    //System.out.printf("tractor %d go to %.2f, %.2f collect bales completed at %.2f\n", id, dest.x, dest.y, time.get(id));
                    return Command.createMoveCommand(dest); 
                  }
                }
                else{
                  trailers.put(tractor.getLocation(), trailers.get(tractor.getLocation())-1);
                  tractors.put(id, 7);
                  time.put(id, time.get(id)+10);
                  b = b+1;
                  //System.out.printf("unstack at %.2f\n", time.get(id));                 
                  return new Command(CommandType.UNSTACK);
                }

            case 10:
                b = b+1;
                tractors.put(id, 11);
                return new Command(CommandType.LOAD);
            case 11:
                tractors.put(id, 12);
                if (!tractor.getHasBale()){
                  //System.out.printf("ERROR tractor %d empty home\n", id);
                }
                dest = new Point(id/(2*num),Math.sqrt(0.9-id/(2*num)));
                return Command.createMoveCommand(dest); 
            case 12:
                tractors.put(id, 9);
                b = b-1;
                return new Command(CommandType.UNLOAD);
            case 13:
                return new Command(CommandType.DETATCH);
            
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
        for (int i = 1; i < k+1 && i < inputBales.size(); i++) {
            result.add(inputBales.get(i));
        }
        return new Cluster(result, null);
    }

}
