# TimeGuess
This project was created for the course "Software engineering" at the University of Innsbruck in the summer term of 2021.

This project models a game similar to "Activity" with a webinterface and an IoT dice, called "TimeFlip".

## Team members
- Diana Gründlinger
- Marcel Alexander Huber
- Thomas Klotz - *Project leader*
- Aaron Targa
- Matthias Thalmann

## Project structure
```
/
│
└─ client - Contains the Angular frontend project
│
└─ server - Contains the Spring backend with REST Api and Websocket server
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

### Scope (optional)
This defines the changed component/part of the project. E.g. `userManagement`

### Short summary
Shortly describe the changes and **reference related issues**. E.g. `added possibility to remove users (#2)` where `#2` references the commit with the id 2

## Issues
When creating issues please use the following naming schema:
```
<scope>: <short summary>
```
Where the scope defines the changed component/part of the project.

Add the appropriate labels to the issue (e.g. backend, bug) and assign the issue to a user (if wanted).