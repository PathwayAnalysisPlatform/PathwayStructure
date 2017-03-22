PathwayQuery
===========
[![GitHub license](http://dmlc.github.io/img/apache2.svg)](./LICENSE)

The objective of this project is to extract graphs related to proteins or reactions from the data contained in the Reactome Graph database.

There are queries performed in R and Java. These are located in the respective folders for R and Java.

## Java 

The program that performs the queries is a single executable ".jar" file that that creates a graph of protein interactions in a resulting ".sif" file, where the vertices are proteins and the edges are interactions between proteins. The graph represents the whole proteome available in Reactome or the interactions of a set of proteins. s

For more information check out the [Wiki pages](https://github.com/bramburger/PathwayProjectQueries/wiki).

### Set up

* Make sure java is installed as stated [here.](https://www.java.com/en/download/help/version_manual.xml)
* Download and install [Neo4j](https://neo4j.com/download/)
* Download and set up Reactome in Neo4j available [here.](http://reactome.org/pages/documentation/developer-guide/graph-database/).
* Download the latest [release](https://github.com/bramburger/PathwayProjectQueries/releases) of PathwayQuery. It is a *.jar* file. The name is *PathwayQuery-X.X.jar* where *X.X* are wild cards for the version number of the program.

### How to use

* Open a command prompt located in the folder that contains the "PathwayQuery-X.X.jar" file.
* (optionally) Create a file called "config.txt" in the same folder, and set the desired values for the configuration parameters.
* Execute the following instruction in the command prompt: 
~~~~
java -jar PathwayQuery-X.X.jar
~~~~
* The result ".sif" file will be in the same location as the ".jar" file by default, or in the folder specified in the configuration file. 

