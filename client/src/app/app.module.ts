import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { LayoutComponent } from './layout/layout.component';
import { MaterialModule } from './core/material/material.module';
import { HomeComponent } from './pages/home/home.component';
import { UsersComponent } from './pages/admin/users/users.component';
import { ExpressionsComponent } from './pages/admin/expressions/expressions.component';
import { GamesComponent } from './pages/admin/games/games.component';
import { LoginComponent } from './pages/login/login.component';
import { GameComponent } from './pages/game/game.component';
import { LogoutComponent } from './pages/logout/logout.component';
import { SettingsDialogComponent } from './layout/components/settings-dialog/settings-dialog.component';
import { ProfileDialogComponent } from './components/profile-dialog/profile-dialog.component';
import { ConfigService } from './core/config/config.service';
import { HttpClientModule } from '@angular/common/http';

export function setupConfig(service: ConfigService) {
  return () => service.load();
}

@NgModule({
  declarations: [
    AppComponent,
    LayoutComponent,
    HomeComponent,
    UsersComponent,
    ExpressionsComponent,
    GamesComponent,
    LoginComponent,
    GameComponent,
    LogoutComponent,
    SettingsDialogComponent,
    ProfileDialogComponent,
  ],
  imports: [BrowserModule, AppRoutingModule, BrowserAnimationsModule, MaterialModule, HttpClientModule],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: setupConfig,
      deps: [ConfigService],
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
