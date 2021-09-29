Remote Barcode Reader suite
=====

Android app and Python library for turning mobile phone into a remotely controllable Barcode/QR code reader.

It exposes a simple JSON API via WebSocket server running on the mobile device.

## API

App -> Server examples:

```json
{"text":"V1.00051.31","format":"QR_CODE","raw_bytes":"..."}
```

Server -> App examples:

```json lines
{ "cmd": "show_toast", "text": "toast text" }
{ "cmd": "play_sound", "name": "default", "volume": 100 }
{ "cmd": "set_text", "text": "plain text" }
{ "cmd": "set_html", "html": "ID: <b>rich text</b>" }
```

## Tools

### reader.py

#### Example

```shell
python -m tools.reader --host 192.168.1.14 --extract 3:-3 --mode type-enter-up
```

It connects to mobile app and waits for scans. Each scan is trimmed by 3 characters from the beginning and the end and code is typed to currently active application (eg. Microsoft Excel). Then, it presses Enter key followed by Up key (so it stays in the same cell).

#### Usage

```
python -m tools.reader [-h] --host IP [--extract DEF] [--mode MODE] [--sound NAME] [--sound-volume VOLUME]

optional arguments:
  -h, --help            show this help message and exit
  --host IP             Mobile phone IP address
  --extract DEF         Extract part of the scanned code. X:Y where X and Y are respectively start and end positions
  --mode MODE           Mode (print, type-enter or type-enter-up)
  --sound NAME          Name of the sound effect to be played after successful scan, default: default
  --sound-volume VOLUME
                        Sound volume (0-100), default: 100
```
