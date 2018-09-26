all:
	java sunshine.sim.Simulator -t 1000 -n 10 -p random -m 100 --gui --fps 10

compile:
	javac sunshine/sim/*.java

clean:
	rm sunshine/*/*.class

