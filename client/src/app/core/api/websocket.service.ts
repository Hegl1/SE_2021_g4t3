import { Injectable } from '@angular/core';
import { ConfigService } from '../config/config.service';

declare var SockJS: any;
declare var Stomp: any;

@Injectable({
  providedIn: 'root',
})
export class WebsocketService {
  private stompClient: any | null = null;
  private connected: Promise<void> | null = null;

  constructor(private config: ConfigService) {}

  /**
   * Connects to the websocket and returns itself for fluent-api
   *
   * @return this
   */
  connect(debug = false) {
    if (this.stompClient !== null) return this;

    const ws = new SockJS(this.config.get('websocket_url', 'http://localhost:8080/websocket'));
    this.stompClient = Stomp.over(ws);

    if (!debug) {
      this.stompClient.debug = null;
    }

    // TODO: handle error
    this.connected = new Promise((res) => {
      this.stompClient.connect({}, () => {
        res();
      });
    });

    return this;
  }

  // TODO: add function to close websocket

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
  async subscribeQueue(queue: string, callback: (message: any) => void) {
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
}
