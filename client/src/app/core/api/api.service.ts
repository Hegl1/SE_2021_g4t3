import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Expression } from '@angular/compiler';
import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable } from 'rxjs';
import { UserService } from '../auth/user.service';
import { ConfigService } from '../config/config.service';
import {
  Category,
  CategoryInfo,
  CategoryStats,
  DiceMapping,
  GlobalStats,
  Role,
  RunningGame,
  RunningGameState,
  TopGameStats,
  User,
  UserStats,
} from './ApiInterfaces';
import { ApiResponse } from './ApiResponse';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  constructor(
    private http: HttpClient,
    private config: ConfigService,
    private user: UserService,
    private snackBar: MatSnackBar
  ) {}

  /**
   * The http-options containing the token, if a user is logged-in
   */
  private get httpOptions(): { observe: 'response'; headers: HttpHeaders } {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    let token = this.user.token;

    if (this.user.isLoggedin && token) {
      headers = headers.append('Authorization', token);
    }

    return {
      observe: 'response',
      headers: headers,
    };
  }

  /**
   * The api url from the config
   */
  get URL() {
    return this.config.get('api_url', 'http://localhost:8080');
  }

  /**
   * Handles the received http-response-observable and returns the appropriate ApiResponse instance
   *
   * @param observable the http-response-observable to handle
   * @returns the created ApiResponse instance
   */
  private async handleResponse<T>(observable: Observable<HttpResponse<T | null>>): Promise<ApiResponse<T>> {
    let prom = observable.toPromise();

    let status: number;
    let value: T | null;

    try {
      let ret = await prom;

      status = ret.status;
      value = ret.body;
    } catch (e) {
      status = e.status;
      let error = e.error;

      if (status != 404) {
        if (status == 401 && this.user.isLoggedin) {
          this.user.logoutReason('unauthorized');
        } else if (status == 403) {
          if (this.user.isLoggedin) {
            this.snackBar.open('You are not authorized to perform this action!', 'OK', {
              duration: 10000,
              panelClass: 'action-warn',
            });
          }

          error = null;
        }

        return new ApiResponse<T>(e.status, null, error);
      }
      value = error;
    }

    return new ApiResponse<T>(status, value);
  }

  // User

  loginUser(username: string, password: string) {
    return this.handleResponse<{ user: User; token: string }>(
      this.http.post<{ user: User; token: string }>(
        `${this.URL}/users/login`,
        { username: username, password: password },
        this.httpOptions
      )
    );
  }

  registerUser(username: string, password: string) {
    return this.handleResponse<{ user: User; token: string }>(
      this.http.post<{ user: User; token: string }>(
        `${this.URL}/users/register`,
        { username: username, password: password },
        this.httpOptions
      )
    );
  }

  checkAuthentication() {
    return this.handleResponse<{}>(this.http.get(`${this.URL}/users/auth`, this.httpOptions));
  }

  getUser(id: number) {
    return this.handleResponse<User>(this.http.get<User>(`${this.URL}/users/${id}`, this.httpOptions));
  }

  getAllUsers() {
    return this.handleResponse<User[]>(this.http.get<User[]>(`${this.URL}/users`, this.httpOptions));
  }

  updateUser(id: number, values: { old_password?: string; username?: string; password?: string; role?: Role }) {
    return this.handleResponse<{}>(this.http.put<{}>(`${this.URL}/users/${id}`, values, this.httpOptions));
  }

  createUser(username: string, password: string, role: Role) {
    return this.handleResponse<User>(
      this.http.post<User>(
        `${this.URL}/users`,
        { username: username, password: password, role: role },
        this.httpOptions
      )
    );
  }

  deleteUser(id: number) {
    return this.handleResponse<{}>(this.http.delete<{}>(`${this.URL}/users/${id}`, this.httpOptions));
  }

  isUserIngame(id: number) {
    return this.handleResponse<boolean>(this.http.get<boolean>(`${this.URL}/users/${id}/ingame`, this.httpOptions));
  }

  // Statistics

  getUserStats(id: number) {
    return this.handleResponse<UserStats>(this.http.get<UserStats>(`${this.URL}/stats/users/${id}`, this.httpOptions));
  }

  getGlobalStats() {
    return this.handleResponse<GlobalStats>(this.http.get<GlobalStats>(`${this.URL}/stats/global`, this.httpOptions));
  }

  getCategoryStats() {
    return this.handleResponse<CategoryStats[]>(
      this.http.get<CategoryStats[]>(`${this.URL}/stats/categories`, this.httpOptions)
    );
  }

  getTopGamesStats() {
    return this.handleResponse<TopGameStats[]>(
      this.http.get<TopGameStats[]>(`${this.URL}/stats/topGames`, this.httpOptions)
    );
  }

  // Game

  createGame(
    dice_code: string,
    category_id: number,
    max_score: number,
    number_of_teams: number,
    mapping: DiceMapping[] | null
  ) {
    return this.handleResponse<number>(
      this.http.post<number>(
        `${this.URL}/games`,
        {
          dice_code: dice_code,
          category_id: category_id,
          max_score: max_score,
          number_of_teams: number_of_teams,
          mapping: mapping,
        },
        this.httpOptions
      )
    );
  }

  getAllGames() {
    return this.handleResponse<RunningGame[]>(this.http.get<RunningGame[]>(`${this.URL}/games`, this.httpOptions));
  }

  getGame(code: number) {
    return this.handleResponse<RunningGame>(this.http.get<RunningGame>(`${this.URL}/games/${code}`, this.httpOptions));
  }

  getGameExists(code: number) {
    return this.handleResponse<{}>(this.http.get<{}>(`${this.URL}/games/${code}/exists`, this.httpOptions));
  }

  joinGame(code: number) {
    return this.handleResponse<{}>(this.http.post<{}>(`${this.URL}/games/${code}/join`, null, this.httpOptions));
  }

  deleteGame(code: number) {
    return this.handleResponse<{}>(this.http.delete<{}>(`${this.URL}/games/${code}`, this.httpOptions));
  }

  // Ingame

  getIngame() {
    return this.handleResponse<boolean>(this.http.get<boolean>(`${this.URL}/ingame`, this.httpOptions));
  }

  leaveIngame(code: number) {
    return this.handleResponse<{}>(this.http.delete<{}>(`${this.URL}/ingame/${code}/leave`, this.httpOptions));
  }

  getIngameState() {
    return this.handleResponse<RunningGameState>(
      this.http.get<RunningGameState>(`${this.URL}/ingame/state`, this.httpOptions)
    );
  }

  setIngameReady(code: number, state: boolean) {
    return this.handleResponse<{}>(this.http.post<{}>(`${this.URL}/ingame/${code}/ready`, state, this.httpOptions));
  }

  joinIngameTeam(code: number, team: number) {
    return this.handleResponse<{}>(
      this.http.post<{}>(`${this.URL}/ingame/${code}/teams/${team}/join`, null, this.httpOptions)
    );
  }

  addIngamePlayerToTeam(code: number, team: number, username: string, password: string) {
    return this.handleResponse<{}>(
      this.http.post<{}>(
        `${this.URL}/ingame/${code}/teams/${team}/players`,
        {
          username: username,
          password: password,
        },
        this.httpOptions
      )
    );
  }

  confirmIngame(code: number, confirmation: 'CORRECT' | 'WRONG' | 'INVALID') {
    return this.handleResponse<{}>(
      this.http.post<{}>(`${this.URL}/ingame/${code}/confirm`, confirmation, this.httpOptions)
    );
  }

  // Expression

  getExpressionsForCategory(category_id: number) {
    return this.handleResponse<Expression[]>(
      this.http.get<Expression[]>(`${this.URL}/categories/${category_id}/expressions`, this.httpOptions)
    );
  }

  createExpressionForCategory(category_id: number, expression: string) {
    return this.handleResponse<Expression>(
      this.http.post<Expression>(
        `${this.URL}/categories/${category_id}/expressions`,
        {
          name: expression,
        },
        this.httpOptions
      )
    );
  }

  importExpressionsForCategory(category_id: number, expressions: string[]) {
    return this.handleResponse<Expression[]>(
      this.http.post<Expression[]>(
        `${this.URL}/categories/${category_id}/expressions/import`,
        expressions,
        this.httpOptions
      )
    );
  }

  importExpressions(expressions: { category: string; expressions: string[] }[]) {
    return this.handleResponse<{ category: Category; expressions: Expression[] }[]>(
      this.http.post<{ category: Category; expressions: Expression[] }[]>(
        `${this.URL}/expressions/import`,
        expressions,
        this.httpOptions
      )
    );
  }

  deleteExpression(id: number) {
    return this.handleResponse<{}>(this.http.delete<{}>(`${this.URL}/expressions/${id}`, this.httpOptions));
  }

  // Category

  getAllCategories() {
    return this.handleResponse<Category[]>(this.http.get<Category[]>(`${this.URL}/categories`, this.httpOptions));
  }

  getAllCategoriesInfo() {
    return this.handleResponse<CategoryInfo[]>(
      this.http.get<CategoryInfo[]>(`${this.URL}/categories/info`, this.httpOptions)
    );
  }

  getCategory(id: number) {
    return this.handleResponse<Category>(this.http.get<Category>(`${this.URL}/categories/${id}`, this.httpOptions));
  }

  createCategory(name: string) {
    return this.handleResponse<Category>(
      this.http.post<Category>(
        `${this.URL}/categories`,
        {
          name: name,
        },
        this.httpOptions
      )
    );
  }

  deleteCategory(id: number) {
    return this.handleResponse<{}>(this.http.delete<{}>(`${this.URL}/categories/${id}`, this.httpOptions));
  }

  // Dice

  getDiceAvailable(id: string) {
    return this.handleResponse<{}>(this.http.get<{}>(`${this.URL}/dice/${id}/available`, this.httpOptions));
  }
}
