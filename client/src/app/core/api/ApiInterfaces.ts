export interface Category {
  id: number;
  name: string;
}

export interface Expression {
  id: number;
  name: string;
}

export enum Role {
  Admin = 'admin',
  Gamemanager = 'gamemanager',
  Player = 'player',
}

export interface User {
  id: number;
  username: string;
  role: Role;
}

export interface RunningGame {
  code: number;
  max_score: number;
  teams: {
    name: string;
    players: User[];
    score: number;
  }[];
  host: User;
  category: Category;
}

export interface UserStats {
  played_games: {
    category: Category;
    won: number;
    lost: number;
  }[];
  played_with: User[];
}

export interface GlobalStats {
  totalGames: number;
  number_correct: number;
  number_incorrect: number;
  mostPlayedCategory: Category;
  mostGamesWon: User[];
}

export interface TopGameStats {
  teams: {
    score: number;
    number_correct: number;
    number_incorrect: number;
  }[];
  category: Category;
  score_per_time: number;
  duration: number;
}

export interface CategoryStats {
  category: Category;
  number_correct: number;
  number_incorrect: number;
}

export interface DiceMapping {
  action: string;
  time: number;
  points: number;
}
