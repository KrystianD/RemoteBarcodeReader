import json
import threading
import time
from typing import Optional

import websocket


class RemoteBarcodeReaderClient:
    def __init__(self, host: str, port: int = 9999):
        self.ws = websocket.WebSocketApp(f"ws://{host}:{port}/",
                                         on_open=self._on_open,
                                         on_message=self._on_message,
                                         on_error=self._on_error,
                                         on_close=self._on_close)

        self.on_message = None

        self.last_msg_hash: Optional[int] = None
        self.last_msg_time: Optional[int] = None

    def start(self):
        def th():
            while True:
                self.ws.run_forever()
                time.sleep(1)

        t = threading.Thread(target=th)
        t.daemon = True
        t.start()

    def show_toast(self, text: str):
        self.ws.send(json.dumps({
            "cmd": "show_toast",
            "text": text,
        }))

    def play_sound(self, name: str, volume: int = 100):
        self.ws.send(json.dumps({
            "cmd": "play_sound",
            "name": name,
            "volume": volume,
        }))

    def set_text(self, text: str):
        self.ws.send(json.dumps({
            "cmd": "set_text",
            "text": text,
        }))

    def set_html(self, html: str):
        self.ws.send(json.dumps({
            "cmd": "set_html",
            "html": html,
        }))

    def _on_message(self, ws, message):
        data = json.loads(message)
        msg_hash = hash((data["text"], data["format"], data["raw_bytes"]))
        if msg_hash != self.last_msg_hash or time.time() - self.last_msg_time > 5:
            self.on_message(data)
            self.last_msg_hash = msg_hash
            self.last_msg_time = time.time()

    # noinspection PyMethodMayBeStatic
    def _on_error(self, ws, error):
        # print(f"Connection error {error}")
        pass

    # noinspection PyMethodMayBeStatic
    def _on_close(self, ws, close_status_code, close_msg):
        # print(f"Connection closed: {close_status_code} / {close_msg}")
        pass

    def _on_open(self, ws):
        pass
