#! /bin/sh
classpath="/usa/arao/jar/lucene4/lucene-core-4.0.0-ALPHA.jar:/usa/arao/jar/lucene4/lucene-analyzers-common-4.0.0-ALPHA.jar:/usa/arao/jar/gson-2.2.1.jar:/usa/arao/projects/gaterelated/dist/lib/yelpacad.jar"

#classpath=$CLASSPATH":/usa/arao/jar/*:/usa/arao/projects/gaterelated/dist/lib/"

echo $1 $2
echo $classpath
# the fist argument is the name of the json file (yelp acad dataset)
#the second argument specifOAied to script is the name of directory where indexing will be done
java -classpath $classpath yelpacad.YelpAcadJson $1 $2
