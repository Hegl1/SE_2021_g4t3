import { Pipe, PipeTransform } from '@angular/core';
import { Role } from '../api/ApiInterfaces';
import { UserService } from '../auth/user.service';

@Pipe({
  name: 'roleIcon',
})
export class RoleIconPipe implements PipeTransform {
  transform(role: string): string {
    switch (UserService.parseRole(role)) {
      case Role.Admin:
        return 'security';
      case Role.Gamemanager:
        return 'admin_panel_settings';
      case Role.Player:
        return 'perm_identity';
      default:
        return 'no_accounts';
    }
  }
}
