# TimeGuess Client

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 11.2.6.

- Use `npm run open` to open the development server
- Use `npm run build-prod` to build a production version (output: `dist/`)
- Use `npm run mock-api` to mock the api using the OpenAPI documentation and [Prism](https://meta.stoplight.io/docs/prism/docs/getting-started/01-installation.md)

## Structure

```
src/
│
└─ app/
│  │
│  └─ components/ - Reusable components
│  │
│  └─ core/ - Services and other helper classes
│  │  │
│  │  └─ api/ - Api- and Websocket-Service
│  │  │
│  │  └─ auth/ - Authentication
│  │  │
│  │  └─ config/ - Configuration Service
│  │  │
│  │  └─ files/ - File helper (reader)
│  │  │
│  │  └─ forms/ - Custom validators
│  │  │
│  │  └─ game/ - Game service (game logic)
│  │  │
│  │  └─ material/ - Angular Material Import-Module
│  │  │
│  │  └─ pipes/ - Custom pipes
│  │  │
│  │  └─ theme/ - Theme service
│  │
│  └─ layout/ - The main layout component of the page (navigation etc.)
│  │
│  └─ pages/ - The pages of the site
│
└─ assets/ - Static files
│  │
│  └─ examples/ - Example files (used for Help Dialogs)
│  │
│  └─ images/
│  │
│  └─ lib/ - Libraries used
│  │
│  └─ config.example.json - Configuaration example file (see Config section)
│  │
│  └─ config.json - The actually used config file (needs to be created)
│
└─ index.html
│
└─ styles.scss - Default stylings for the whole project
│
└─ variables.scss - Theme, color and size variables
```

## Config

The config file needs to be created in order to use the project:

- Copy the `src/assets/config.example.json` to `src/assets/config.json`
- Set the correct values. It will not be commited to the repo

```json
{
  "api_url": "http://localhost:8080",                   # The url of the backend
  "websocket_url": "http://localhost:8080/websocket",   # The url of the websocket server
  "critical_battery_level": 10                          # Percentage level after which to show a warning
}
```

## Angular Material

This project uses Angular Material to provide standard components: https://material.angular.io

When using a new component, it needs to be imported in the `src/app/core/material/material.module.ts` module.

For icons the Material Icons are used: https://material.io/icons
