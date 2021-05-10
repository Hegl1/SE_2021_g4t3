# TimeGuess on the Raspberry
You can find step-by-step instructions on how to get the raspberry side of the TimeGuess game running below. Please read the manual carefully before blindly executing any scripts, as this guide contains **crucial** information. 

The last subsection, "Gameflow", is an optional read and not needed to make the program work.

## Installation

Locate the file `installScript.sh` in the `raspberry` folder. 

Run it: ```sudo bash installScript.sh```

If any problems occur, please have a look at [Java und Bluetooth Low Energy auf dem Raspberry Pi](https://git.uibk.ac.at/csat2410/skeleton-bleclient/tree/master) and follow their installation guide.

## Build

Locate the file `buildScript.sh` in the `raspberry` folder. 

Run it: ```sudo bash buildScript.sh```

### Alternative Build

If you decide, for whatever reason, to build it with tests and javadoc, you need to make sure that:
- the central backend is up and running
- the file `timeGuessBackendUrl.txt` in the folder `bleclient` contains the **correct URL of the backend**

Those steps are also **essential** steps before running the execution script.

Then, for building with tests and javadoc, locate the file `buildScriptWithTests.sh` in the `raspberry` folder.

Run it: ```sudo bash buildScriptWithTests.sh```

## Execution

Make sure that the **backend is running** and that you write the **correct URL of the backend** into the file `timeGuessBackendUrl.txt` in the folder `bleclient` or create such a file if it doesn't exist, **before** running the execution script. You can get the IP of the backend by executing the following command on the computer you are running the backend on: 
- windows: `ipconfig`
- linux: `ifconfig`

The port for the backend is `8080`. Don't forget to include that information in the URL in the file `timeGuessBackendUrl.txt`. 

Also make sure to **delete the file `timeGuessDiceId.txt`** (if existent) in the `bleclient` folder before execution of the program, **if it's going to be the first execution** of the program on the raspberry **after a fresh start of the central backend**. The ID which is saved in that file is only valid and registered with the backend as long as the backend is running.

Before executing the program, the dice should be assembled and lying on a straight surface.

Locate the file `executeScript.sh` in the `raspberry` folder. 

Run it: ```sudo bash executeScript.sh```

I strongly recommend to have a **look at your terminal** to check if everythings works as it's supposed to, and also to **read out the ID** which you need when creating a new game, for indicating which raspberry/dice to use. You can also read the ID from the file `timeGuessDiceId.txt` in the `bleclient` folder after starting the program. The ID will be valid as long as the backend is running.

To stop the program at any time, just press `CTRL+C`.

Example of an ideal output:
```
The discovery started: true
...
Found 1 TimeFlip device(s).
Found TimeFlip device with address 0C:61:CF:C7:CF:01 and RSSI -48
Connection established
URL file exists
URL successfully read from file
URL of backend: http://192.168.0.220:8080
ID file doesn't exist
Requesting new ID
GET request response status: 200
ID saved to file
~~~~~~ ID: -yqq407k ~~~~~~
TimeFlip password input successful
Connection thread started
Battery thread started
Battery level: 89
POST request response status: 200
Facet notifications are enabled
```
After this point you can start throwing your dice. I recommend **cleanly putting the dice on each of its facets** (for a few seconds) **at least once**, ideally on a straight surface. That way the mapping of the facets of the TimeFlip dice will work the best.

### Insight on our facet calibration/mapping

We decided against a "perfect" manual mapping with GUI interaction. Each facet just gets a unique random number from 0 to 11 and each of those numbers is mapped to one of the requested activities/points/times from the project description, just not exactly as depicted. 

If the dice lands on an edge somehow, there will be more than 12 different values received from the dice. For every received "edge case" value, i.e. if we get more than 12 different values from the dice, we assume that the facet mapping process (performed as recommended above) is completed  and we return the value modulo 12. That way the throw can still be used for the game and doesn't interrupt the flow.

### You're having problems?

If no TimeFlip device is found during discovery, please try to change the battery (take the battery out of the dice and put it back in). This is a universal fix for any dice related problem.

If there occurs a fatal error after starting the program for the first time, our suggested solution is: 
- just ignore the fatal error
- change the battery of the TimeFlip device (The program didn't stop properly so the dice had no chance to disconnect. Battery change is the universal fix for this.) 
- wait a few seconds after the battery change
- re-run the program (it should *really* work this time...)

If any other exception is thrown after execution, please try to just re-run the program. It usually just magically works the second time.

If you make it to the point where it says "notifications should be turned on now", and you're getting response code 200 for all REST calls (battery, facet change etc.), you should be good to go.

If nothing helps and all fails, please don't hestitate to contact me, Diana Gründlinger (diana.gruendlinger@student.uibk.ac.at), and I will do my best to make it work for you! :)

## Gameflow

Interesting details concerning the dice mechanisms while in-game.

### Battery level 

The battery level of TimeFlip device is checked every ~3 minutes and the system doesn't really care about the dice if that dice is not currently used in a game. That means that it can take up to 3 minutes until the battery level is displayed in-game.

### Playing with the TimeFlip device connection

The program checks the connection to the TimeFlip device every ~15 seconds. Therefore, if you make the TimeFlip device lose connection, e.g. by taking out the battery, it will take up to ~15 seconds until the system registers the incident.

The program then will try to reconnect to the TimeFlip device every ~10 seconds for the next ~3 minutes before it pauses for another ~3 minutes. Assuming that you had enough time to put the battery back in, get a new battery from the closet, finish your coffee or rethink your life choices, it then continues with its attempts to re-establish a connection and repeats the afore-mentioned steps.
