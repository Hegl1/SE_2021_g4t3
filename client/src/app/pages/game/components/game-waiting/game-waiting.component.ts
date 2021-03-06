import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from 'src/app/core/api/api.service';
import { UserService } from 'src/app/core/auth/user.service';
import { GameService } from 'src/app/core/game/game.service';
import { AddPlayerToTeamDialogComponent } from './components/add-player-to-team-dialog/add-player-to-team-dialog.component';

@Component({
  selector: 'tg-game-waiting',
  templateUrl: './game-waiting.component.html',
  styleUrls: ['./game-waiting.component.scss'],
})
export class GameWaitingComponent {
  constructor(
    public game: GameService,
    private user: UserService,
    private dialog: MatDialog,
    private api: ApiService,
    private snackBar: MatSnackBar
  ) {}

  get teams() {
    return this.game.currentState?.teams || null;
  }

  get hostId() {
    return this.game.currentState?.host.id;
  }

  get currentUserId() {
    return this.user.user?.id;
  }

  get isCurrentUserHost() {
    if (!this.user.user || !this.game.currentState) return false;

    return this.user.user.id === this.game.currentState.host.id;
  }

  get startable() {
    return this.game.currentState?.waiting_data?.startable || false;
  }

  /**
   * Checks whether the authenticated user is in the team
   * with the given index
   *
   * @param index the teams index
   * @returns whether the authenticated user is in the team
   */
  isCurrentUsersTeam(index: number) {
    if (this.user.user?.id == null) return false;

    if (index === -1) {
      return (
        this.game.currentState?.waiting_data?.unassigned_players.some((player) => player.id === this.user.user?.id) ||
        false
      );
    }

    return this.game.isUsersTeam(this.user.user?.id, index);
  }

  isReadyPlayer(id: number) {
    if (!this.game.currentState || !this.game.currentState.waiting_data) return false;

    return this.game.currentState?.waiting_data?.ready_players.some((player) => player.id === id);
  }

  get amountReadyPlayers() {
    return this.game.currentState?.waiting_data?.ready_players.length || 0;
  }

  /**
   * Opens a login form to add a user to the team
   * with the selected index
   *
   * @param index the teams index
   */
  async addPlayerToTeam(index: number) {
    if (!this.teams || !this.teams[index]) return;

    await this.dialog
      .open(AddPlayerToTeamDialogComponent, {
        data: {
          index: index,
          name: this.teams[index].name,
          code: this.game.code,
        },
        width: '350px',
      })
      .afterClosed()
      .toPromise();
  }

  /**
   * Switches the users team to the one with
   * the supplied index
   *
   * @param index the teams index
   */
  async switchTeam(index: number) {
    if (this.isCurrentUsersTeam(index)) return;
    if (this.currentUserId != null && this.isReadyPlayer(this.currentUserId)) return;
    if (this.hostId != null && this.isReadyPlayer(this.hostId)) return;

    let res = await this.api.joinIngameTeam(this.game.code, index);

    if (!res.isOK()) {
      this.snackBar.open('Error switching team!', 'OK', {
        duration: 10000,
        panelClass: 'action-warn',
      });
    }
  }

  /**
   * Toggles the ready state for the user
   */
  async toggleReady() {
    if (this.currentUserId === undefined) return;

    let res = await this.api.setIngameReady(this.game.code, !this.isReadyPlayer(this.currentUserId));

    if (!res.isOK()) {
      this.snackBar.open('Error setting ready state!', 'OK', {
        duration: 10000,
        panelClass: 'action-warn',
      });
    }
  }

  /**
   * Leaves the current game
   */
  async leaveGame() {
    this.game.leave();
  }

  /**
   * Selectes the content of the code input
   * and copys it to the clipboard
   */
  copyCode(input: HTMLInputElement) {
    input.select();
    input.setSelectionRange(0, 99);
    document.execCommand('copy');

    this.snackBar.open('Code copied to clipboard!', 'OK', {
      duration: 2000,
    });
  }
}
