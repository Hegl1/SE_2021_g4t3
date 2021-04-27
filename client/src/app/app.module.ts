import { HttpClientModule } from '@angular/common/http';
import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ConfirmDialogComponent } from './components/confirm-dialog/confirm-dialog.component';
import { InputDialogComponent } from './components/input-dialog/input-dialog.component';
import { ProfileDialogComponent } from './components/profile-dialog/profile-dialog.component';
import { ConfigService } from './core/config/config.service';
import { MaterialModule } from './core/material/material.module';
import { RoleIconPipe } from './core/pipes/role-icon.pipe';
import { TextPipe } from './core/pipes/text.pipe';
import { SettingsDialogComponent } from './layout/components/settings-dialog/settings-dialog.component';
import { LayoutComponent } from './layout/layout.component';
import { AddExpressionsDialogComponent } from './pages/admin/expressions/components/add-expressions-dialog/add-expressions-dialog.component';
import { SelectCategoryDialogComponent } from './pages/admin/expressions/components/select-category-dialog/select-category-dialog.component';
import { ShowExpressionsOverlayComponent } from './pages/admin/expressions/components/show-expressions-overlay/show-expressions-overlay.component';
import { ExpressionsComponent } from './pages/admin/expressions/expressions.component';
import { GamesComponent } from './pages/admin/games/games.component';
import { EditUserDialogComponent } from './pages/admin/users/components/edit-user-dialog/edit-user-dialog.component';
import { UsersComponent } from './pages/admin/users/users.component';
import { GameComponent } from './pages/game/game.component';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { LogoutComponent } from './pages/logout/logout.component';
import { JoinGameCardComponent } from './pages/home/components/join-game-card/join-game-card.component';
import { CreateGameCardComponent } from './pages/home/components/create-game-card/create-game-card.component';
import { CreateGameDialogComponent } from './pages/home/components/create-game-card/components/create-game-dialog/create-game-dialog.component';
import { GlobalStatisticsCardComponent } from './pages/home/components/global-statistics-card/global-statistics-card.component';
import { PersonalStatisticsCardComponent } from './pages/home/components/personal-statistics-card/personal-statistics-card.component';
import { CategoryStatisticsCardComponent } from './pages/home/components/category-statistics-card/category-statistics-card.component';

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
    ConfirmDialogComponent,
    RoleIconPipe,
    InputDialogComponent,
    AddExpressionsDialogComponent,
    SelectCategoryDialogComponent,
    ShowExpressionsOverlayComponent,
    JoinGameCardComponent,
    CreateGameCardComponent,
    CreateGameDialogComponent,
    GlobalStatisticsCardComponent,
    PersonalStatisticsCardComponent,
    CategoryStatisticsCardComponent,
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
