import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'gameCode',
})
export class GameCodePipe implements PipeTransform {
  transform(value: string | number, pad = true): string {
    let code = value.toString();

    if (pad) {
      code = code.padStart(8, '0');
    }

    let parts: string[] = [];

    for (let i = 0; i < code.length; i += 2) {
      parts.push(code.substr(i, 2));
    }

    return parts.join('-');
  }
}
