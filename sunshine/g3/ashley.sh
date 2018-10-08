#!/usr/bin/env bash


javac Util_ashley.java ../sim/*.java Util.java
printf "\nSuccesful make!\nResults:\n\n"

cd ../..
java sunshine.g3.Util_ashley