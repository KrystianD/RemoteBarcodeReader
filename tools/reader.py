import re
import sys
import time

import argparse
import traceback

from pynput.keyboard import Key, Controller

from lib.remote_barcode_reader_client import RemoteBarcodeReaderClient


def main():
    argparser = argparse.ArgumentParser(prog="python -m tools.reader")
    argparser.add_argument('--host', type=str, metavar="IP", required=True, help="Mobile phone IP address")
    argparser.add_argument('--extract', type=str, default="", metavar="DEF", help="Extract part of the scanned code. X:Y where X and Y are respectively start and end positions")
    argparser.add_argument('--mode', type=str, metavar="MODE", help="Mode (print, type-enter or type-enter-up)",
                           default="print", choices=("print", "type-enter", "type-enter-up"))

    argparser.add_argument('--sound', type=str, metavar="NAME", default="default", help="Name of the sound effect to be played after successful scan, default: default")
    argparser.add_argument('--sound-volume', type=int, metavar="VOLUME", default=100, help="Sound volume (0-100), default: 100")

    args = argparser.parse_args()

    keyboard = Controller()

    cl = RemoteBarcodeReaderClient(args.host)

    extractor_fn = lambda x: x
    if len(args.extract) > 0:
        m = re.match(r"([\d-]+):([\d-]+)", args.extract)
        if m is None:
            print("Invalid cut definition", file=sys.stderr)
            exit(1)

        extractor_fn = lambda x: x[int(m.group(1)):int(m.group(2))]

    def on_message(msg):
        try:
            code = msg["text"]
            code = extractor_fn(code)
            cl.show_toast("OK")
            if len(args.sound) > 0:
                cl.play_sound(args.sound, args.sound_volume)
            cl.set_html(f"""ID: {code}""")

            print(code)

            if args.mode == "type-enter":
                keyboard.type(code)
                keyboard.press(Key.enter)
                keyboard.release(Key.enter)
            elif args.mode == "type-enter-up":
                keyboard.type(code)
                keyboard.press(Key.enter)
                keyboard.release(Key.enter)
                keyboard.press(Key.up)
                keyboard.release(Key.up)
        except:
            traceback.print_exc()

    cl.on_message = on_message
    cl.start()
    while True:
        time.sleep(100)


main()
