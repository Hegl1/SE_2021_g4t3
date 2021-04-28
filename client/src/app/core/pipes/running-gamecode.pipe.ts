import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'runningGameCode',
})
export class RunningGameCodePipe implements PipeTransform {
  transform(value: number): string {
    let code = value.toString().padStart(8, '0');

    let parts: string[] = [];

    for (let i = 0; i < code.length; i += 2) {
      parts.push(code.substr(i, 2));
    }

    return parts.join('-');
  }
}
