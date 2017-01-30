![Build Status](https://travis-ci.org/RationaleEmotions/just-ask.svg?branch=master)

# just-ask

As the name suggests, this library is eventually going to end up becoming a simple prototype that can be enhanced to 
represent an "On-demand Grid" (which explains the reason behind the name **just-ask** ).
 
## Why an On-Demand Grid ?

A static Grid (i.e., a hub and a fixed number of nodes) is a good start for setting up a remote execution infrastructure. 
But once the usage of the hub starts going up, problems start creeping up. 
Nodes go stale and start causing false failures for the tests and require constant maintenance (restart).  

Some of it can be solved by embedding a ["self healing"](https://rationaleemotions.wordpress.com/2013/01/28/building-a-self-maintaining-grid-environment/) mechanism into the hub, 
but when it comes to scaling the Grid infrastructure this also does not help a lot.

**just-ask** is an on-demand grid,  wherein there are no fixed nodes attached to the grid. 
As and when tests make hit the hub, a node is spun off, the test is routed to the node and after usage the node is 
cleaned up. The on-demand node can be a docker container that hosts a selenium node.


## Building the code on your own

In order to get started with using this library here are the set of instructions that can be followed :
 
 * Build the code using `mvn clean package`
 * Drop the built jar (you will find two jars, so please make sure you pick up the uber jar which would have its name
  around something like this `just-ask-<VERSION>-jar-with-dependencies.jar` ) in the directory that contains the 
  selenium server standalone.
 * Start the selenium hub using the command `java -cp selenium-server-standalone-3.0.1.jar:just-ask-<VERSION>-jar-with-dependencies.jar org.openqa.grid.selenium.GridLauncherV3 -role hub -servlets com.rationaleemotions.servlets.EnrollServlet`
 * Explicitly register the *ghost node* by loading the URL `http://localhost:4444/grid/admin/EnrollServlet` in a browser.
 
 Now the **On-demand Grid** is ready for use.
