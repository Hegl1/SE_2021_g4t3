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
import { GameCodePipe } from './core/pipes/gamecode.pipe';
import { RoleIconPipe } from './core/pipes/role-icon.pipe';
import { TextPipe } from './core/pipes/text.pipe';
import { SettingsDialogComponent } from './layout/components/settings-dialog/settings-dialog.component';
import { LayoutComponent } from './layout/layout.component';
import { AddExpressionsDialogComponent } from './pages/admin/expressions/components/add-expressions-dialog/add-expressions-dialog.component';
import { SelectCategoryDialogComponent } from './pages/admin/expressions/components/select-category-dialog/select-category-dialog.component';
import { ShowExpressionsOverlayComponent } from './pages/admin/expressions/components/show-expressions-overlay/show-expressions-overlay.component';
import { ExpressionsComponent } from './pages/admin/expressions/expressions.component';
import { GameCardComponent } from './pages/admin/games/components/running-game-card/running-game-card.component';
import { GamesComponent } from './pages/admin/games/games.component';
import { EditUserDialogComponent } from './pages/admin/users/components/edit-user-dialog/edit-user-dialog.component';
import { UsersComponent } from './pages/admin/users/users.component';
import { GameFinishedComponent } from './pages/game/components/game-finished/game-finished.component';
import { GameRunningComponent } from './pages/game/components/game-running/game-running.component';
import { GameWaitingComponent } from './pages/game/components/game-waiting/game-waiting.component';
import { GameComponent } from './pages/game/game.component';
import { CategoryStatisticsCardComponent } from './pages/home/components/category-statistics-card/category-statistics-card.component';
import { CreateGameDialogComponent } from './pages/home/components/create-game-card/components/create-game-dialog/create-game-dialog.component';
import { CreateGameCardComponent } from './pages/home/components/create-game-card/create-game-card.component';
import { GlobalStatisticsCardComponent } from './pages/home/components/global-statistics-card/global-statistics-card.component';
import { JoinGameCardComponent } from './pages/home/components/join-game-card/join-game-card.component';
import { PersonalStatisticsCardComponent } from './pages/home/components/personal-statistics-card/personal-statistics-card.component';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { LogoutComponent } from './pages/logout/logout.component';
import { AddPlayerToTeamDialogComponent } from './pages/game/components/game-waiting/components/add-player-to-team-dialog/add-player-to-team-dialog.component';
import { TimePipe } from './core/pipes/time.pipe';
import { PlayerNameComponent } from './components/player-name/player-name.component';
import ThemeService from './core/theme/theme.service';
import { HelpDialogComponent } from './components/help-dialog/help-dialog.component';
import { HelpImportExpressionsComponent } from './components/help-dialog/components/help-import-expressions/help-import-expressions.component';
import { HelpDiceConnectionComponent } from './components/help-dialog/components/help-dice-connection/help-dice-connection.component';
import { HelpDiceMappingComponent } from './components/help-dialog/components/help-dice-mapping/help-dice-mapping.component';
import { PlayerListDialogComponent } from './components/player-list-dialog/player-list-dialog.component';

export function setupConfig(service: ConfigService) {
  return () => service.load();
}
export function setupTheme(service: ThemeService) {
  return () => service.applyTheme();
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
    GameCardComponent,
    GameCodePipe,
    GameWaitingComponent,
    GameRunningComponent,
    GameFinishedComponent,
    AddPlayerToTeamDialogComponent,
    TimePipe,
    PlayerNameComponent,
    HelpDialogComponent,
    HelpImportExpressionsComponent,
    HelpDiceConnectionComponent,
    HelpDiceMappingComponent,
    PlayerListDialogComponent,
  ],
  imports: [BrowserModule, AppRoutingModule, BrowserAnimationsModule, MaterialModule, HttpClientModule],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: setupConfig,
      deps: [ConfigService],
      multi: true,
    },
    {
      provide: APP_INITIALIZER,
      useFactory: setupTheme,
      deps: [ThemeService],
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
