import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'time',
})
export class TimePipe implements PipeTransform {
  transform(time: number): string {
    let isoString = new Date(time * 1000).toISOString();

    if (time >= 60 * 60) {
      return isoString.substr(11, 8);
    } else {
      return isoString.substr(14, 5);
    }
  }
}
