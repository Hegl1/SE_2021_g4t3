import { Injectable } from '@angular/core';
import StorageNames from '../StorageNames';

export enum ThemeType {
  Dark = 'theme_dark',
  Light = 'theme_light',
  Default = 'theme_default',
}

@Injectable({
  providedIn: 'root',
})
export default class ThemeService {
  private theme: ThemeType = ThemeType.Default;
  private systemTheme: ThemeType = ThemeType.Default;

  private isPreferredSet = false;

  constructor() {
    let preferredTheme = localStorage.getItem(StorageNames.Theme);

    if (preferredTheme !== null) {
      this.isPreferredSet = true;

      switch (preferredTheme) {
        case ThemeType.Dark:
          this.theme = ThemeType.Dark;
          break;
        case ThemeType.Light:
          this.theme = ThemeType.Light;
          break;
        default:
          this.isPreferredSet = false;
      }
    }

    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches) {
      this.systemTheme = ThemeType.Light;
    } else {
      this.systemTheme = ThemeType.Dark;
    }

    window.matchMedia('(prefers-color-scheme: light)').addEventListener('change', (e) => {
      this.systemTheme = e.matches ? ThemeType.Light : ThemeType.Dark;

      if (!this.isPreferredSet) {
        this.applyTheme();
      }
    });
  }

  /**
   * Sets the preferred theme
   *
   * @param theme the theme to set
   */
  setTheme(theme: ThemeType | string) {
    if (typeof theme === 'string') {
      switch (theme) {
        case ThemeType.Dark:
          this.theme = ThemeType.Dark;
          break;
        case ThemeType.Light:
          this.theme = ThemeType.Light;
          break;
        default:
          this.theme = ThemeType.Default;
      }
    } else {
      this.theme = theme;
    }

    if (theme !== ThemeType.Default) {
      localStorage.setItem(StorageNames.Theme, theme.toString());
      this.isPreferredSet = true;
    } else {
      localStorage.removeItem(StorageNames.Theme);
      this.isPreferredSet = false;
    }

    this.applyTheme();
  }

  /**
   * @returns the current set theme
   */
  get currentTheme() {
    return this.isPreferredSet ? this.theme : ThemeType.Default;
  }

  /**
   * Applies the current theme
   */
  applyTheme() {
    document.documentElement.className =
      this.theme === ThemeType.Default ? this.systemTheme.toString() : this.theme.toString();
  }
}
