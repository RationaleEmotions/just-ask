![Build Status](https://travis-ci.org/RationaleEmotions/just-ask.svg?branch=master)

# just-ask

As the name suggests, this library is eventually going to end up becoming a simple prototype that can be enhanced to 
represent an "On-demand Grid" (which explains the reason behind the name **just-ask** ).
 
 In order to get started with using this library here are the set of instructions that can be followed :
 
 * Build the code using `mvn clean package`
 * Drop the built jar in the directory that contains the selenium server standalone.
 * Start the selenium hub using the command `java -cp selenium-server-standalone-3.0.1.jar:just-ask-1.0-SNAPSHOT.jar org.openqa.grid.selenium.GridLauncherV3 -role hub -servlets com.rationaleemotions.servlets.EnrollServlet`
 * Explicitly register the *ghost node* by loading the URL `http://localhost:4444/grid/admin/EnrollServlet` in a browser.
 
 Now the **On-demand Grid** is ready for use.
