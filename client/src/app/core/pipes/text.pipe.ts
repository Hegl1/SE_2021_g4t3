import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'text',
})
export class TextPipe implements PipeTransform {
  transform(value: string): string {
    return value.substring(0, 1).toUpperCase() + value.substring(1);
  }
}
