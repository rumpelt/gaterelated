#! /bin/sh
args="--mt  --lm --minngram=1  --cltype=simplelogistic  --ifile /usa/arao/xyz/feedtype.csv    --tcol 3 --agecol 2 --idcol 1 --lcol  14  --lage 0.0 --uage 1.0 --idname=mrn --ngrams 1:2:3"
classpath=$CLASSPATH":/usa/arao/jar/*:/usa/arao/projects/gaterelated/dist/lib/*"
echo $classpath
java -classpath $classpath weka.MainDriver $args
