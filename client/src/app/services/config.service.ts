import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

const URL = 'assets/config.json';

@Injectable({
  providedIn: 'root',
})
export class ConfigService {
  private data = {};

  constructor(private http: HttpClient) {}

  /**
   * Get a value from the config
   *
   * @param path the path of the value, separated by "."
   * @param def the default value to return, if the value is not found
   * @returns the found value
   */
  get(path: string, def: any = null): any {
    let val: any = this.data;
    let segments = path.split('.');

    for (let index in segments) {
      let segment = segments[index];

      if (typeof val[segment] === 'undefined') {
        return def;
      }

      val = val[segment];
    }

    return val;
  }

  async load() {
    this.data = await this.http.get<{}>(URL).toPromise();
  }
}
