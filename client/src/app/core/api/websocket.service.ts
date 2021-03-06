import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserService } from '../auth/user.service';
import { ConfigService } from '../config/config.service';
import { WebsocketResponse } from './ApiInterfaces';

declare var SockJS: any;
declare var Stomp: any;

@Injectable({
  providedIn: 'root',
})
export class WebsocketService {
  private stompClient: any | null = null;
  private connected: Promise<void> | null = null;

  constructor(private config: ConfigService, private snackBar: MatSnackBar, private user: UserService) {}

  /**
   * Connects to the websocket and returns itself for fluent-api
   *
   * @return this
   */
  connect(debug = false) {
    if (this.stompClient !== null) return this;

    const ws = new SockJS(this.config.get('websocket_url', 'http://localhost:8080/websocket'));
    this.stompClient = Stomp.over(ws);

    ws.addEventListener('close', (e: any) => {
      this.stompClient = null;
      this.connected = null;

      if (!e.wasClean) {
        setTimeout(() => {
          this.snackBar.open('The connection to the server was interrupted. Please reload the page!', 'OK', {
            panelClass: 'action-warn',
          });
        }, 200);
      }
    });

    if (!debug) {
      this.stompClient.debug = null;
    }

    this.connected = new Promise((res, rej) => {
      this.stompClient.connect({ token: this.user.token }, res, rej);
    });

    return this;
  }

  /**
   * Closes the websocket if it is connected
   */
  disconnect() {
    if (this.stompClient !== null && this.stompClient.connected) {
      this.stompClient.ws.close();
    }
  }

  /**
   * Subscribes to the supplied message queue, calling the callback
   * with the content of a new message every time
   *
   * If the connection was not yet established (but started), the
   * function waits
   *
   * @param queue the message queue to subscribe to
   * @param callback the callback-function that receives the messages
   */
  async subscribeQueue(queue: string, callback: (message: WebsocketResponse) => void) {
    if (this.stompClient === null) {
      throw new Error('Not connected to websocket');
    }

    await this.connected;

    this.stompClient.subscribe(queue, (message: any) => {
      if (message.body) {
        callback(JSON.parse(message.body));
      }
    });
  }

  /**
   * Returns once the connection was established
   *
   * @throws if the connection was not yet started
   *
   * @returns a promise
   */
  async afterConnected() {
    if (this.connected === null) {
      throw new Error('No connection started yet');
    }

    await this.connected;
  }

  /**
   * Returns whether the websocket is connected or not
   */
  get isConnected(): boolean {
    return this.stompClient && this.stompClient.connected;
  }
}
