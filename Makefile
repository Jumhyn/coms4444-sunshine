all:
	java sunshine.sim.Simulator --time_step 1.0 -t 10000 --num_tractors 1 --player g2 -m 100 --gui --fps 50

compile:
	javac sunshine/sim/*.java

clean:
	rm sunshine/*/*.class

