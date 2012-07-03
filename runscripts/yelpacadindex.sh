#! /bin/sh
classpath=$CLASSPATH":/usa/arao/jar/*:/usa/arao/projects/gaterelated/dist/lib/*"

#classpath=$CLASSPATH":/usa/arao/jar/*:/usa/arao/projects/gaterelated/dist/lib/*"

echo $1 $2
#echo $classpath
# the fist argument is the name of the json file (yelp acad dataset)
#the second argument specified to script is the name of directory where indexing will be done
java -classpath $classpath json.YelpAcadJson $1 $2
