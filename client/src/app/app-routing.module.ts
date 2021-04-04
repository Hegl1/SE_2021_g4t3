import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/auth/auth.guard';
import { LayoutComponent } from './layout/layout.component';
import { ExpressionsComponent } from './pages/admin/expressions/expressions.component';
import { GamesComponent } from './pages/admin/games/games.component';
import { UsersComponent } from './pages/admin/users/users.component';
import { GameComponent } from './pages/game/game.component';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { LogoutComponent } from './pages/logout/logout.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: LoginComponent },
  { path: 'logout', component: LogoutComponent },
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: 'home', component: HomeComponent, canActivate: [AuthGuard] },
      {
        path: 'admin',
        children: [
          { path: 'users', component: UsersComponent, canActivate: [AuthGuard] },
          { path: 'expressions', component: ExpressionsComponent, canActivate: [AuthGuard] },
          { path: 'games', component: GamesComponent, canActivate: [AuthGuard] },
        ],
      },
      { path: 'game', component: GameComponent, canActivate: [AuthGuard] },
      { path: 'game/:id', component: GameComponent, canActivate: [AuthGuard] },
      { path: '**', redirectTo: '/home' },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
