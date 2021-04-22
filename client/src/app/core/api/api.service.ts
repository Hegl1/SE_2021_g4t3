import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Expression } from '@angular/compiler';
import { Injectable } from '@angular/core';
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
  TopGameStats,
  User,
  UserStats,
} from './ApiInterfaces';
import { ApiResponse } from './ApiResponse';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  constructor(private http: HttpClient, private config: ConfigService, private user: UserService) {}

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
   * Handles the received http-response-promise and returns the appropriate ApiResponse instance
   *
   * @param prom the http-response-promise to handle
   * @returns the created ApiResponse instance
   */
  private async handleResponse<T>(prom: Promise<HttpResponse<T | null>>): Promise<ApiResponse<T>> {
    let status: number;
    let value: T | null;

    try {
      let ret = await prom;

      status = ret.status;
      value = ret.body;
    } catch (e) {
      status = e.status;
      value = e.error;

      if (status != 404) {
        if (status == 401 && this.user.isLoggedin) {
          // TODO: fetch new token
          this.user.logoutReason('unauthorized');
        } else if (status == 403 && this.user.isLoggedin) {
          // TODO: inform user that he is not allowed to perform this action
        }

        return new ApiResponse<T>(e.status);
      }
    }

    return new ApiResponse<T>(status, value);
  }

  // User

  loginUser(username: string, password: string) {
    return this.handleResponse<{ user: User; token: string }>(
      this.http
        .post<{ user: User; token: string }>(
          `${this.URL}/users/login`,
          { username: username, password: password },
          this.httpOptions
        )
        .toPromise()
    );
  }

  registerUser(username: string, password: string) {
    return this.handleResponse<{ user: User; token: string }>(
      this.http
        .post<{ user: User; token: string }>(
          `${this.URL}/users/register`,
          { username: username, password: password },
          this.httpOptions
        )
        .toPromise()
    );
  }

  getUser(id: number) {
    return this.handleResponse<User>(this.http.get<User>(`${this.URL}/users/${id}`, this.httpOptions).toPromise());
  }

  getAllUsers() {
    return this.handleResponse<User[]>(this.http.get<User[]>(`${this.URL}/users`, this.httpOptions).toPromise());
  }

  updateUser(id: number, values: { old_password?: string; username?: string; password?: string; role?: Role }) {
    return this.handleResponse<{}>(this.http.put<{}>(`${this.URL}/users/${id}`, values, this.httpOptions).toPromise());
  }

  createUser(username: string, password: string, role: Role) {
    return this.handleResponse<User>(
      this.http
        .post<User>(`${this.URL}/users`, { username: username, password: password, role: role }, this.httpOptions)
        .toPromise()
    );
  }

  deleteUser(id: number) {
    return this.handleResponse<{}>(this.http.delete<{}>(`${this.URL}/users/${id}`, this.httpOptions).toPromise());
  }

  // Statistics

  getUserStats(id: number) {
    return this.handleResponse<UserStats>(
      this.http.get<UserStats>(`${this.URL}/stats/users/${id}`, this.httpOptions).toPromise()
    );
  }

  getGlobalStats() {
    return this.handleResponse<GlobalStats>(
      this.http.get<GlobalStats>(`${this.URL}/stats/global`, this.httpOptions).toPromise()
    );
  }

  getCategoryStats() {
    return this.handleResponse<CategoryStats[]>(
      this.http.get<CategoryStats[]>(`${this.URL}/stats/categories`, this.httpOptions).toPromise()
    );
  }

  getTopGamesStats() {
    return this.handleResponse<TopGameStats[]>(
      this.http.get<TopGameStats[]>(`${this.URL}/stats/topGames`, this.httpOptions).toPromise()
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
      this.http
        .post<number>(
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
        .toPromise()
    );
  }

  getAllGames() {
    return this.handleResponse<RunningGame[]>(
      this.http.get<RunningGame[]>(`${this.URL}/games`, this.httpOptions).toPromise()
    );
  }

  getGame(code: number) {
    return this.handleResponse<RunningGame>(
      this.http.get<RunningGame>(`${this.URL}/games/${code}`, this.httpOptions).toPromise()
    );
  }

  getGameExists(code: number) {
    return this.handleResponse<{}>(this.http.get<{}>(`${this.URL}/games/${code}/exists`, this.httpOptions).toPromise());
  }

  deleteGame(code: number) {
    return this.handleResponse<{}>(this.http.delete<{}>(`${this.URL}/games/${code}`, this.httpOptions).toPromise());
  }

  // Expression

  getExpressionsForCategory(category_id: number) {
    return this.handleResponse<Expression[]>(
      this.http.get<Expression[]>(`${this.URL}/categories/${category_id}/expressions`, this.httpOptions).toPromise()
    );
  }

  createExpressionForCategory(category_id: number, expression: string) {
    return this.handleResponse<Expression>(
      this.http
        .post<Expression>(
          `${this.URL}/categories/${category_id}/expressions`,
          {
            name: expression,
          },
          this.httpOptions
        )
        .toPromise()
    );
  }

  importExpressionsForCategory(category_id: number, expressions: string[]) {
    return this.handleResponse<Expression[]>(
      this.http
        .post<Expression[]>(`${this.URL}/categories/${category_id}/expressions/import`, expressions, this.httpOptions)
        .toPromise()
    );
  }

  importExpressions(expressions: { category: string; expressions: string[] }[]) {
    return this.handleResponse<{ category: Category; expressions: Expression[] }[]>(
      this.http
        .post<{ category: Category; expressions: Expression[] }[]>(
          `${this.URL}/expressions/import`,
          expressions,
          this.httpOptions
        )
        .toPromise()
    );
  }

  deleteExpression(id: number) {
    return this.handleResponse<{}>(this.http.delete<{}>(`${this.URL}/expressions/${id}`, this.httpOptions).toPromise());
  }

  // Category

  getAllCategories() {
    return this.handleResponse<Category[]>(
      this.http.get<Category[]>(`${this.URL}/categories`, this.httpOptions).toPromise()
    );
  }

  getAllCategoriesInfo() {
    return this.handleResponse<CategoryInfo[]>(
      this.http.get<CategoryInfo[]>(`${this.URL}/categories/info`, this.httpOptions).toPromise()
    );
  }

  getCategory(id: number) {
    return this.handleResponse<Category>(
      this.http.get<Category>(`${this.URL}/categories/${id}`, this.httpOptions).toPromise()
    );
  }

  createCategory(name: string) {
    return this.handleResponse<Category>(
      this.http
        .post<Category>(
          `${this.URL}/categories`,
          {
            name: name,
          },
          this.httpOptions
        )
        .toPromise()
    );
  }

  deleteCategory(id: number) {
    return this.handleResponse<{}>(this.http.delete<{}>(`${this.URL}/categories/${id}`, this.httpOptions).toPromise());
  }
}
