# TimeGuess

This project was created for the course "Software engineering" at the University of Innsbruck in the summer term of 2021.

This project models a game similar to "Activity" with a webinterface and an IoT dice, called "TimeFlip".

## Team members

- Diana Gründlinger
- Marcel Alexander Huber
- Thomas Klotz - _Project leader_
- Aaron Targa
- Matthias Thalmann

If anything is unclear, please contact: t.klotz@student.uibk.ac.at

## Project structure

```
/
│
└─ client - Contains the Angular frontend project
│
└─ backend - Contains the Spring backend with REST Api and Websocket server
|
└─ raspberry - Contains the Raspberry communication server
│
└─ docs   - Contains documents and documentation for the project
   │
   └─ designs       - Contains the Adobe XD designs for the frontend (source file and exported versions)
   │
   └─ timetracking  - Contains the Excel sheets with the timetracking information for each member
   │
   └─ uml           - Contains the UML diagrams (source files and exported versions)
```

## Documentation

Source files of the documentation can be opened with:

- `.graphml`: [yEd](https://www.yworks.com/products/yed#yed-support-resources)
- `.uxf`: [www.umlet.com](http://www.umlet.com/umletino/umletino.html) or an UMLet extension for VS Code
- `.xd`: [Adobe XD](https://www.adobe.com/de/products/xd.html)
- `TimeGuess.v1.yaml`:
  - Viewing: [Swagger](https://swagger.io/), [VS Code Plugin](https://marketplace.visualstudio.com/items?itemName=42Crunch.vscode-openapi)
  - Mocking: [Prism](https://stoplight.io/open-source/prism/)



The "Testdrehbuch" can be found in `/docs/Testdrehbuch.docx` .

## Deployment 

In order to run the front- and backend, please install [docker](https://www.docker.com/get-started).

### Before building the docker images

If you only want to run the frontend on localhost, skip this section and continue at [the next section](#Build-and-run-the-docker-containers). 
If you want to be able to properly open the website on multiple devices in your network, follow these steps:

- In ```backend/src/main/resources/application.properties```, change all occurrences of ```localhost``` to the ip of the backend server (the ip of the machine running docker).
- Copy  ```client/src/assets/config.example.json``` and paste it to the same folder. Rename the copy to ```config.json```. Now change all occurences of ```localhost``` in ```client/src/assets/config.json```  to the ip of the backend server again.

### Build and run the docker containers

To run this application you need to execute the following in the root directory of the project:

```bash
docker-compose up [-d] # -d if you want to run docker in detached mode (do not receive outputs on console).
```

### Change the configuration after the containers were built

In order to re-configure the application, simply stop all running containers, delete the images and follow the steps described in [the previous section](# Before-building-the-docker-images).

### Setup the raspberry

For setting up the raspberry, please refer to the [README.md](./raspberry/README.md) in the ```raspberry``` folder.

## Javadocs and Jacoco-Report

After the deployment the javadocs and the jacoco report can be pulled from the backend image by executing the generate.ps1 or generate.sh script.
The files will be copied to the [docs/generated](./docs/generated) folder.

## Commiting

When commiting to this repository please use the following naming schema:

```
<type>(<scope>): <short summary>
```

(see https://github.com/angular/angular/blob/master/CONTRIBUTING.md#-commit-message-format)

### Type

- **feat**: A new feature
- **fix**: A bugfix or similar
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **chore**: Changes not related to functionality changes (e.g. updating libraries)
- **docs**: Changes to documents or documentation
- **tests**: Changes to tests

### Scope (optional)

This defines the changed component/part of the project. E.g. `userManagement`

### Short summary

Shortly describe the changes and **reference related issues**. E.g. `added possibility to remove users (#2)` where `#2` references the commit with the id 2

## Differences Assignment/Implementation

- Not all open games get listed for regular users. Since it was unclear whether this behaviour was intended and we do not think that this functionality
 would be benifitial for people that play a game together, sitting at the same table, we omitted this functionality. We use generated codes for joining games instead. 

- The host does not need to click 'ready'. Instead he can use the 'start game' button once the game is ready to start.

## Known issues

Known issues can be found in the 'closed' section of the issues tab in GitLab. Everything marked with the label `wont-fix` will not be fixed by the time of the project deadline. 
