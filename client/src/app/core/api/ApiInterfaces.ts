export interface Category {
  id: number;
  name: string;
}

export interface CategoryInfo extends Category {
  deletable: boolean;
  expressions_amount: number;
}

export interface Expression {
  id: number;
  name: string;
}

export enum Role {
  Admin = 'ADMIN',
  Gamemanager = 'GAMEMANAGER',
  Player = 'PLAYER',
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

export enum GameStatus {
  Waiting = 'WAITING',
  Running = 'RUNNING',
  Finished = 'FINISHED',
}

export interface RunningGameState extends RunningGame {
  status: GameStatus;
  waiting_data: {
    unassigned_players: User[];
    ready_players: User[];
  } | null;
  running_data: {
    round: number;
    running: boolean;
    current_team: number;
    current_player: User;
    points: number;
    total_time: number;
    round_start_time: number;
    expression: string | null;
    action: string;
  } | null;
}

export interface UserStats {
  won_games: {
    category: Category;
    amount: number;
  }[];
  lost_games: {
    category: Category;
    amount: number;
  }[];
  most_played_category: Category | null;
  played_games: number;
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
