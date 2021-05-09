# TimeGuess

This project was created for the course "Software engineering" at the University of Innsbruck in the summer term of 2021.

This project models a game similar to "Activity" with a webinterface and an IoT dice, called "TimeFlip".

## Team members

- Diana Gründlinger
- Marcel Alexander Huber
- Thomas Klotz - _Project leader_
- Aaron Targa
- Matthias Thalmann

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

## Deployment using docker

To run this application you need to execute the following in the root:

```bash
docker-compose up [-d] # -d if you want to detach
```

### Frontend configuration

If you want to define a frontend configuration file, you need to copy the `client/src/assets/config.example.json` to `client/src/assets/config.json`
and set the values. Afterwards you need to rebuild the client.

If you don't define a configuration file the example file will be used (which is fine if you only want to run it on localhost).

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
