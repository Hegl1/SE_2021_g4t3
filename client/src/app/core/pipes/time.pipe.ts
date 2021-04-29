import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'time',
})
export class TimePipe implements PipeTransform {
  transform(time: number): string {
    return new Date(time * 1000).toISOString().substr(14, 5);
  }
}
