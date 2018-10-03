package sunshine.g1;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    private int num_tractors;
    private double time_budget;

    
    List<Point> bales, copy_bales;
    ArrayList<List<Point>> equizones; 
    ArrayList<Integer> Tasks;
    int curr_idx;
    double dim;

    public Player() {
        rand = new Random(seed);

    }
    
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = bales;
        copy_bales = new ArrayList<Point>();
        for(int i=0;i<bales.size();i++)
            copy_bales.add(bales.get(i));

        num_tractors = n;
        time_budget = t;
        Tasks = new ArrayList<Integer>();
        dim =m;
        for(int i=0;i<n;i++)
            Tasks.add(-1);

        curr_idx=0;

        partition_uniform();
    }

    public void partition()
    {
        Collections.sort(bales,new Comparator <Point>() {

        public int compare(Point o1, Point o2) {
        return -1*Double.compare(o1.x/o1.y + o1.x/dim,o2.x/o2.y + o2.x/dim);
        }
        });


        equizones = new ArrayList<List<Point>>();
        int zone_count = (bales.size() % 11 == 0) ? bales.size()/11 : bales.size()/11 + 1;
        System.out.println("zone count is" + zone_count + "\n");
        for(int i=0;i<zone_count;i++)
        {
            ArrayList<Point> zone = new ArrayList<Point>();
            for(int j = i*11; j< Math.min((i+1)*11,bales.size()); j++)
            {
                zone.add(bales.get(j));
            }
            equizones.add(zone);
        }
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

        // int count =0;
        // for(int i=0;i<equizones.size();i++)
        // {
        //    for(int j=0;j<equizones.get(i).size();j++)
        //    {
        //         Point tmp = equizones.get(i).get(j);
        //         System.out.print(tmp.x + "," + tmp.y +"\t");
        //         count++;
        //    }
        //    System.out.println();
        // }
        // System.out.println("Points printed are" + count);
        
    
    
    // public Command getCommand(Tractor tractor)
    // {
    //     if (tractor.getHasBale())
    //     {
    //         if (tractor.getLocation().equals(new Point(0.0, 0.0)))
    //         {
    //             return new Command(CommandType.UNLOAD);
    //         }
    //         else
    //         {
    //             return Command.createMoveCommand(new Point(0.0, 0.0));
    //         }
    //     }
    //     else
    //     {
    //         if (tractor.getLocation().equals(new Point(0.0, 0.0)))
    //         {
    //             if (rand.nextDouble() > 0.5)
    //             {
    //                 if (tractor.getAttachedTrailer() == null)
    //                 {
    //                     return new Command(CommandType.ATTACH);
    //                 }
    //                 else
    //                 {
    //                     return new Command(CommandType.DETATCH);
    //                 }
    //             }
    //             else if (bales.size() > 0)
    //             {
    //                 Point p = bales.remove(rand.nextInt(bales.size()));
    //                 return Command.createMoveCommand(p);
    //             }
    //             else
    //             {
    //                 return null;
    //             }
    //         }
    //         else
    //         {
    //             return new Command(CommandType.LOAD);
    //         }
    //     }
    // }



    public Command getCommand(Tractor tractor)
    {
        int idx = tractor.getId();

        if(Tasks.get(idx)==-1 && curr_idx<equizones.size())
        {
            Tasks.set(idx,curr_idx);
            Point p = equizones.get(curr_idx).get(0);
            curr_idx++;
            return Command.createMoveCommand(p);
        }

        else if (tractor.getLocation().equals(new Point(0.0,0.0)))
        {
            if(!tractor.getHasBale() && tractor.getAttachedTrailer().getNumBales() == 0 && curr_idx<equizones.size())
            {
                Tasks.set(idx,curr_idx);
                Point p = equizones.get(curr_idx).get(0);
                curr_idx++;
                 return Command.createMoveCommand(p);
            }

            else if (tractor.getHasBale())
            {
                return new Command(CommandType.UNLOAD);
            }

            else if (tractor.getAttachedTrailer().getNumBales()>0)
            {
                return new Command(CommandType.UNSTACK);
            }
            else
            {
                return null;
            }

        }

        else if (tractor.getHasBale() && tractor.getAttachedTrailer().getNumBales()<10) //trailer has space
        {
                System.out.println("tractor location is" + tractor.getLocation().x + "," + tractor.getLocation().y);
                System.out.println("trailer location is "  + tractor.getAttachedTrailer().getLocation().x + " , " + tractor.getAttachedTrailer().getLocation().y);
                return new Command(CommandType.STACK);
        }

        else if(tractor.getHasBale())   ////all 11 collected
        {
            return Command.createMoveCommand(new Point(0.0,0.0));
        }

        else if(equizones.get(Tasks.get(idx)).size()>0 && tractor.getLocation().equals(equizones.get(Tasks.get(idx)).get(0))) /// reached next bale pickup
        {
            equizones.get(Tasks.get(idx)).remove(0);
            return new Command(CommandType.LOAD);
        }

        else if (equizones.get(Tasks.get(idx)).size()>0) /// move to next bale
        {
            Point p = new Point(0.0,0.0);
            if(equizones.get(Tasks.get(idx)).size()>0)
            p = equizones.get(Tasks.get(idx)).get(0);
            return Command.createMoveCommand(p);
        }

        else /// all done return home
        {
           return Command.createMoveCommand(new Point(0.0,0.0));
        }
        
    }




}
