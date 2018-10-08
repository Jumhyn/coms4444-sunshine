package sunshine.g8;

import java.lang.Math;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Comparator;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import sunshine.sim.Trailer;


public class Player implements sunshine.sim.Player 
{
	// Random seed of 42.
	private int seed = 42;
	private Random rand;

	// List of Bales, one batch for trailers and the other for tractor ONLY
	private List<Point> tractor_bales;
	private List<Point> trailer_bales;
	
	// Make list of centroids
	private List<Point> centroids = new List<Point>;
	
	private boolean is_not_removed = true;

	// Get number of tractors, change stategy as needed
	private int n_tractors = 0;
	private double dimensions = 0;
	private int hay_stack = 0;

	public Player() 
	{
		rand = new Random(seed);
	}

	private double distance(Point p1, Point p2) 
	{
		return Math.sqrt(Math.pow(p1.x-p2.x,2)+Math.pow(p1.y-p2.y,2));
	}

	public Comparator<Point> pointComparator = new Comparator<Point>() 
	{
		public int compare(Point p1, Point p2) 
		{
			Point origin = new Point(0.0,0.0);
			double d1 = distance(origin, p1);
			double d2 = distance(origin, p2);
			if (d1 < d2) 
			{
				return -1;
			} 
			else if (d1 > d2) 
			{
				return 1;
			} 
			else 
			{
				return 0;
			}
		}
	};

	public void init(List<Point> bales, int n, double m, double t)
	{
		// Organize how bales will be selected by tractors
		Collections.sort(bales, pointComparator);
		// Break this up
		// 1- 1/2 closest to tractor
		// 2- 1/2 closest to trailer
		this.tractor_bales = bales;
		this.trailer_bales = null;
		
		this.n_tractors = n;
		this.dimensions = m;
		
		//System.out.println(n); //30 - number of tractors
		//System.out.println(m); //500 - length of field
		//System.out.println(t); //10000 - time
	}

	public Command getCommand(Tractor tractor)
	{
		// Andrew will be testing some stuff with just 1 tractor
		if(n_tractors == 1)
		{
			// OPTON 1: USE NO TRAILER
			/*
			if(is_not_removed)
			{
				is_not_removed = false;
				return new Command(CommandType.DETATCH);
			}
			
			// Option 1: Abandon Trailer, grab everything!
			if (tractor.getHasBale()) 
			{
				// Unload the bale at the barn
				if (tractor.getLocation().equals(new Point(0.0, 0.0))) 
				{
					return new Command(CommandType.UNLOAD);
				}
				// Move back to the barn!
				else 
				{
					return Command.createMoveCommand(new Point(0.0, 0.0));
				}
			}
			// There is no bale!
			else 
			{
				if(tractor.getLocation().equals(new Point(0.0, 0.0)))
				{
					Point p = tractor_bales.remove(tractor_bales.size()-1);
					return Command.createMoveCommand(p);
				}
				else
				{
					return new Command(CommandType.LOAD);
				}
			}
			*/

			// Option 2: Use Trailer for everything
			// If at origin
			if (tractor.getLocation().equals(new Point(0.0, 0.0)))
			{
				if(tractor.getAttachedTrailer() != null)
				{
					// It is time to empty everything
					if(tractor.gethasBale())
					{
						this.haystack = tractor.getAttachedTrailer();
						return new Command(CommandType.DETACH);
					}
					// Everything is empty, Go now!
					else
					{
						Point p = tractor_bales.remove(tractor_bales.size()-1);
						return Command.createMoveCommand(p);
					}
				}
				// It is detached! Empty EVERYTHING!
				else
				{
					if(tractor.getHasBale())
					{
						return new Command(CommandType.UNLOAD);
					}
					// BEWARE OF EMPTY TRAILER!
					else
					{
						if(haystack > 0)
						{
							--haystack;
							return new Command(CommandType.UNSTACK);
						}
						else
						{
							return new Command(CommandType.ATTACH);
						}
					}
				}
				
				// If not empty, GO TO LOCATION
			}
			// Move back to the barn!
			else
			{
				// 2- unattach trailer
				// 3- Stack 10 times, 1 more for hay
				// 4- Attach
				// 5- go to barn
				// BEWARE OF LIST OF POINTS FOR TRAILER BEING EMPTY!
				
				if(tractor.getAttachedTrailer() != null)
				{
					// Just in case the bails for trailer are done! Go Back NOW!
					if(trailer_bails == null || trailer_bails.isEmpty())
					{
						return Command.createMoveCommand(new Point(0.0, 0.0)));
					}
					
					// Do you have 11th bale? If so, time to go?
					if(tractor.gethasBale())
					{
						return Command.createMoveCommand(new Point(0.0, 0.0)));
					}
					// Everything is empty, Go now!
					else
					{
						this.haystack = tractor.getAttachedTrailer();
						return new Command(CommandType.DETACH);
					}
				}
				else
				{
					// It is detached, so right now just stack everything now!
					if(haystack == 10)
					{
						++haystack;
						return new Command(CommandType.STACK);
					}
					else
					{
						return new Command(CommandType.ATTACH);
					}
				}
				return Command.createMoveCommand(new Point(0.0, 0.0));
			}
    		
			
			// Option 3: Use Trailer ONLY ON QUADRAN 1 (Closest to Barn)

			// Option 4: Use the Trailer ONLY ON QUADTRANT 5 (Furthest from Barn)
		}
		// Leaving room open for different tractor strategy
		else if(n_tractors > 1)
		{
			if (tractor.getHasBale()) 
			{
				// Unload the bale at the barn
				if (tractor.getLocation().equals(new Point(0.0, 0.0))) 
				{
					return new Command(CommandType.UNLOAD);
				}
				// Move back to the barn!
				else 
				{
					return Command.createMoveCommand(new Point(0.0, 0.0));
				}
			}
			// There is no bale!
			else 
			{
				if (tractor.getLocation().equals(new Point(0.0, 0.0))) 
				{
					if (tractor.getAttachedTrailer() == null) 
					{
						Point p = tractor_bales.remove(tractor_bales.size()-1);
						return Command.createMoveCommand(p);
					} 
					else 
					{
						return new Command(CommandType.DETATCH);
					}
				}
				else 
				{
					return new Command(CommandType.LOAD);
				}
			}
		}
		else
		{
			if (tractor.getHasBale()) 
			{
				// Unload the bale at the barn
				if (tractor.getLocation().equals(new Point(0.0, 0.0))) 
				{
					return new Command(CommandType.UNLOAD);
				}
				// Move back to the barn!
				else 
				{
					return Command.createMoveCommand(new Point(0.0, 0.0));
				}
			}
			// There is no bale!
			else 
			{
				if (tractor.getLocation().equals(new Point(0.0, 0.0))) 
				{
					if (tractor.getAttachedTrailer() == null) 
					{
						Point p = tractor_bales.remove(tractor_bales.size()-1);
						return Command.createMoveCommand(p);
					} 
					else 
					{
						return new Command(CommandType.DETATCH);
					}
				}
				else 
				{
					return new Command(CommandType.LOAD);
				}
			}
		}
	}
}
