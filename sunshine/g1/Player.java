package sunshine.g1;

import java.util.List;
import java.util.Collections;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import sunshine.g1.weiszfeld.WeightedPoint;
import sunshine.g1.weiszfeld.WeiszfeldAlgorithm;
import sunshine.g1.weiszfeld.Input;
import sunshine.g1.weiszfeld.Output;

public class Player extends sunshine.queuerandom.QueuePlayer {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    private int num_tractors;
    private double time_budget;

    List<Point> bales, copy_bales;
    ArrayList<List<Point>> scan_zones;
    ArrayList<List<Point>> equizones;
    ArrayList<Integer> Tasks;
    int curr_idx, cutoff_thresh;
    double dim;
    Point seed_cluster;
    final static Point ORIGIN = new Point(0.0d, 0.0d);

    public class trailer{
        public Point location;
        public Integer numBales;

        public trailer()
        {
            numBales =0;
            location = new Point(0,0);
        }
    }

    ArrayList<trailer> availableTrailers;
    ArrayList<Point> centers;
    private boolean[] greedyMode;


    public Player() {
        rand = new Random(seed);

    }

    public void init(List<Point> bales, int n, double m, double t)
    {
        super.init(bales, n, m, t);
        this.bales = bales;
        copy_bales = new ArrayList<Point>();
        for(int i=0;i<bales.size();i++)
            copy_bales.add(bales.get(i));

        num_tractors = n;
        greedyMode = new boolean[n];
        time_budget = t;
        Tasks = new ArrayList<Integer>();
        dim =m;
        for(int i=0;i<n;i++)
            Tasks.add(-1);

        curr_idx=0;
        seed_cluster = new Point(0.0,0.0);

        availableTrailers = new ArrayList<trailer>();
        for(int i=0;i<n;i++)
        {
            trailer tmp =new trailer();
            availableTrailers.add(tmp);
        }

        centers = new ArrayList<Point>(n);
        for (int i = 0; i < n; i++) {
        	centers.add(null);
        }
        // partition_uniform();
        partition();
        System.out.println("partition completed");
        cluster_points();
        getCutoff();


    }

    // public void partition()
    // {
    //     Collections.sort(bales,new Comparator <Point>() {

    //     public int compare(Point o1, Point o2) {
    //     return -1*Double.compare(o1.x/o1.y + o1.x/dim,o2.x/o2.y + o2.x/dim);
    //     }
    //     });


    //     equizones = new ArrayList<List<Point>>();
    //     int zone_count = (bales.size() % 11 == 0) ? bales.size()/11 : bales.size()/11 + 1;
    //     System.out.println("zone count is" + zone_count + "\n");
    //     for(int i=0;i<zone_count;i++)
    //     {
    //         ArrayList<Point> zone = new ArrayList<Point>();
    //         for(int j = i*11; j< Math.min((i+1)*11,bales.size()); j++)
    //         {
    //             zone.add(bales.get(j));
    //         }
    //         equizones.add(zone);
    //     }
    // }



        public void partition()
        {
            double k=25;
            scan_zones =  new ArrayList<List<Point>>();

            while((dim*2)/k >=1)
            {
                ArrayList<Point> curr_zone = new ArrayList<Point>();
                for(int i=0;i<copy_bales.size();i++)
                {
                    Point tmp = copy_bales.get(i);

                    if(tmp.x + tmp.y <= k)
                    {
                        curr_zone.add(tmp);
                        copy_bales.remove(i);
                        i--;
                    }
                }

                scan_zones.add(curr_zone);

               k+=25;
            }

            int counter =0;
            for(int i=0;i<scan_zones.size();i++)
            {
                for(int j=0;j<scan_zones.get(i).size();j++)
                {
                    counter++;
                }
            }

            System.out.println("partitioned point count is " + counter);


        }

        public void cluster_points()
        {
             List<Point> initial = scan_zones.get(0);
            Collections.sort(initial,new Comparator <Point>() {

            public int compare(Point o1, Point o2) {
            return Double.compare(Math.sqrt(Math.pow(o1.x,2)+ Math.pow(o1.y,2)),Math.sqrt(Math.pow(o2.x,2)+ Math.pow(o2.y,2)));
            }
            });


            seed_cluster = initial.get(0);
            equizones = new ArrayList<List<Point>>();
            int zone_count = (bales.size() % 11 == 0) ? bales.size()/11 : bales.size()/11 + 1;

            int j=0;
            while( j<scan_zones.size())
                {
                    ArrayList<Point> curr_cluster = new ArrayList<Point>();

                    while(curr_cluster.size()<11 && j<scan_zones.size() )
                    {
                        List<Point> curr_zone = scan_zones.get(j);
                        Collections.sort(curr_zone,new Comparator <Point>() {

                        public int compare(Point o1, Point o2) {
                        return Double.compare(Math.sqrt(Math.pow(o1.x - seed_cluster.x,2)+ Math.pow(o1.y - seed_cluster.y,2)),Math.sqrt(Math.pow(o2.x-seed_cluster.x,2)+ Math.pow(o2.y-seed_cluster.y,2)));
                         }
                        });

                        for(int i=0;i<curr_zone.size() && curr_cluster.size()<11;i++)
                        {
                            curr_cluster.add(curr_zone.get(0));
                            curr_zone.remove(0);
                        }

                        seed_cluster = curr_cluster.get(curr_cluster.size()-1);

                         scan_zones.set(j,curr_zone);
                         if(curr_zone.size() == 0)
                            j++;

                    }

                    equizones.add(curr_cluster);
                }


                Collections.reverse(equizones);

                int count =0;
                for(int i=0;i<equizones.size();i++)
                {
                    for(int z=0;z<equizones.get(i).size();z++)
                    {
                        Point tmp = equizones.get(i).get(z);
                        System.out.print(tmp.x + "," + tmp.y +"\t");
                        count++;
                    }
                    System.out.println();
                }
                    System.out.println("Points printed are" + count);

        }




        public void partition_uniform()
        {
            Collections.sort(copy_bales,new Comparator <Point>() {

            public int compare(Point o1, Point o2) {
            return -1*Double.compare(Math.sqrt(Math.pow(o1.x,2)+ Math.pow(o1.y,2)),Math.sqrt(Math.pow(o2.x,2)+ Math.pow(o2.y,2)));
            }
            });

            equizones = new ArrayList<List<Point>>();
            int zone_count = (bales.size() % 11 == 0) ? bales.size()/11 : bales.size()/11 + 1;

            for(int i=0;i<zone_count;i++)
            {
                ArrayList<Point> zone = new ArrayList<Point>();

                Point pivot = copy_bales.get(0);

                Collections.sort(copy_bales,new Comparator <Point>() {

                public int compare(Point o1, Point o2) {
                return Double.compare(Math.sqrt(Math.pow(o1.x - pivot.x,2)+ Math.pow(o1.y - pivot.y,2)),Math.sqrt(Math.pow(o2.x-pivot.x,2)+ Math.pow(o2.y-pivot.y,2)));
                }
                });


                for(int j = 0; j< Math.min(11,copy_bales.size()); j++)
                {
                    zone.add(copy_bales.get(j));
                }

                int del_counter = 0;
                while(del_counter<Math.min(11,copy_bales.size()))
                {
                    copy_bales.remove(0);
                    del_counter++;
                }
                equizones.add(zone);
            }

            int count =0;
        for(int i=0;i<equizones.size();i++)
        {
           for(int j=0;j<equizones.get(i).size();j++)
           {
                Point tmp = equizones.get(i).get(j);
                System.out.print(tmp.x + "," + tmp.y +"\t");
                count++;
           }
           System.out.println();
        }
        System.out.println("Points printed are" + count);


        }


        public Point getClusterCenter(List<Point> cluster)
        {
            // We use Weiszfeld Algorithm to find the weighted geometric median
        	List<WeightedPoint> weightedPoints = new LinkedList<WeightedPoint>();
        	for (Point p : cluster) {
        		weightedPoints.add(new WeightedPoint(0.2D, p.x, p.y));
        	}

        	weightedPoints.add(new WeightedPoint(0.5D, 0.0D, 0.0D));

            Input input = new Input();
            input.setDimension(2);
            input.setPoints(weightedPoints);
            input.setPermissibleError(0.00001);

            double[] centerCoordinate = WeiszfeldAlgorithm.process(input).getPointCoordinate();

            Point center = new Point(centerCoordinate[0], centerCoordinate[1]);
            Point origin = ORIGIN;
            double efficiency = 0.0D; // The time saved from using trailer strategy

            for (Point p : cluster) {
            	// For each point, we go back to the trailer instead of the origin
            	efficiency += 0.2D * (distance(p, origin) - distance(p, center));
            }
            efficiency -= 0.5D * distance(center, origin) // Carrying trailer from and back to the origin
            		+ (cluster.size() - 1) * 20.0D // Stacking & unstacking cost
            		+ 240.0D; // attaching & detatching cost


            /// hack to not leave behind any bale
            center.x += 0.001;
            center.y += 0.001;
            if (efficiency > 0.0D)
            	return center;
            else
            	return null;

        	/*
            Point center = new Point(0,0);

            for(int i=0;i<cluster.size();i++)
            {
                center.x += cluster.get(i).x;
                center.y += cluster.get(i).y;
            }

            if(cluster.size()>0)
            {
                center.x/=cluster.size();
                center.y/=cluster.size();
            }

            return center;
            */
        }

        private void getCutoff()
        {
            cutoff_thresh = 0;
            while(getClusterCenter(equizones.get(equizones.size()-cutoff_thresh-1))==null)
            {
                cutoff_thresh++;

            }
                /// last series null at equizones.size() - cutoff_thresh

            for(int i=0;i< equizones.size() - cutoff_thresh;i++)
            {
                if(getClusterCenter(equizones.get(i)) == null)
                {
                    while(getClusterCenter(equizones.get(equizones.size()-cutoff_thresh-1))==null)
                        cutoff_thresh++;

                    Collections.swap(equizones,i,equizones.size()-cutoff_thresh-1);
                    cutoff_thresh++;
                }
            }

            for(int i=0;i<equizones.size();i++)
            {
                System.out.println(getClusterCenter(equizones.get(i)));
            }

            System.out.println("cutoff thresh is" + cutoff_thresh + "equizones size is " + equizones.size() + " expected error index is" + (equizones.size() - cutoff_thresh));
        }


    private static double distance(Point p1, Point p2) {
    	return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

@Override
public ArrayList<Command> getMoreCommands(Tractor tractor)
    {
        int idx = tractor.getId();

        // Initialize list of commands to return
        ArrayList<Command> toReturn = new ArrayList<Command>();
        if (curr_idx >= equizones.size())
          return null;
        List<Point> cluster = equizones.get(curr_idx);
        Point center = getClusterCenter(cluster);
        // If we are beneath the threshold
        if(center == null){

          // Individually get furthest bale and LOAD
          Point bale_location = equizones.get(curr_idx).get(0);
          equizones.get(curr_idx).remove(0);
          if(equizones.get(curr_idx).size() == 0){
            curr_idx++;
          }

          toReturn.add(Command.createMoveCommand(bale_location));
          toReturn.add(new Command(CommandType.LOAD));

          // Return to origin and UNLOAD
          toReturn.add(Command.createMoveCommand(ORIGIN));
          toReturn.add(new Command(CommandType.UNLOAD));

        }
        else{
          // Attach if not attached
          if(tractor.getAttachedTrailer() == null){
            toReturn.add(new Command(CommandType.ATTACH));
          }

          // Go to furthest cluster center, and detatch
          int last_idx = curr_idx;
          curr_idx++;
          toReturn.add(Command.createMoveCommand(center));
          toReturn.add(new Command(CommandType.DETATCH));

          // Collect 10 bales and stack
          int balesRemain = equizones.get(last_idx).size();
          int numUnstack = balesRemain;
          while(balesRemain > 1){
            Point next_bale = equizones.get(last_idx).get(0);
            equizones.get(last_idx).remove(0);
            balesRemain--;

            toReturn.add(Command.createMoveCommand(next_bale));
            toReturn.add(new Command(CommandType.LOAD));
            toReturn.add(Command.createMoveCommand(center));
            toReturn.add(new Command(CommandType.STACK));
          }
          // Get last bale on forklift and attach
          Point next_bale = equizones.get(last_idx).get(0);
          equizones.get(last_idx).remove(0);

          toReturn.add(Command.createMoveCommand(next_bale));
          toReturn.add(new Command(CommandType.LOAD));
          toReturn.add(Command.createMoveCommand(center));
          toReturn.add(new Command(CommandType.ATTACH));

          // Go back to origin, UNLOAD forklift and detatch
          toReturn.add(Command.createMoveCommand(ORIGIN));
          toReturn.add(new Command(CommandType.UNLOAD));
          toReturn.add(new Command(CommandType.DETATCH));

          // UNSTACK the rest of the bales
          for(int i = 0; i<numUnstack-1; i++){
            toReturn.add(new Command(CommandType.UNSTACK));
            toReturn.add(new Command(CommandType.UNLOAD));
          }
          // Check if we should switch to greedyMode
          if(curr_idx > equizones.size()-cutoff_thresh){
            greedyMode[idx] = true;
          }
        }
        // Return list of commands
        return toReturn;
      }
}
