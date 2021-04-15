import { HttpClientModule } from '@angular/common/http';
import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ProfileDialogComponent } from './components/profile-dialog/profile-dialog.component';
import { ConfigService } from './core/config/config.service';
import { MaterialModule } from './core/material/material.module';
import { TextPipe } from './core/pipes/text.pipe';
import { SettingsDialogComponent } from './layout/components/settings-dialog/settings-dialog.component';
import { LayoutComponent } from './layout/layout.component';
import { ExpressionsComponent } from './pages/admin/expressions/expressions.component';
import { GamesComponent } from './pages/admin/games/games.component';
import { UsersComponent } from './pages/admin/users/users.component';
import { GameComponent } from './pages/game/game.component';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { LogoutComponent } from './pages/logout/logout.component';
import { EditUserDialogComponent } from './pages/admin/users/components/edit-user-dialog/edit-user-dialog.component';

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
    TextPipe,
    EditUserDialogComponent,
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
