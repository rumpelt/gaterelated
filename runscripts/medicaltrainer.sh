#! /bin/sh
#args="--mt  --lm  --minngram=1  --cltype=tournamentmodel  --ifile /usa/arao/xyz/goldset.csv    --tcol 3 --agecol 2 --idcol=1  --lcol  4  --lage 0.0 --uage 1.0 --idname=uid --ngrams 1 --removeCommonCounters --rulefile=/usa/arao/xyz/goldsetrules0-1.csv --samplesize=150 --numi=50 "
classpath=$CLASSPATH":/usa/arao/jar/*:/usa/arao/projects/gaterelated/dist/lib/*"


args="--mt    --minngram=1  --cltype=logistic  --ifile /usa/arao/xyz/goldset.csv    --tcol 3 --agecol 2 --idcol=1 --lcol=4  --lage 0.0 --uage 1.0 --idname=uid --ngrams 1 --removeCommonCounters --rulefile=/usa/arao/xyz/goldsetrules0-1.csv --samplesize=150 --numi=50 "

#classpath=$CLASSPATH":/usa/arao/jar/*:/usa/arao/projects/gaterelated/dist/lib/*"

#echo $args
#echo $classpath
java -classpath $classpath weka.MainDriver $args
