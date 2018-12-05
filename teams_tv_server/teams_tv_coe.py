from __future__ import print_function
import sys


def eprint(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)


import json
from flask import Flask, render_template, send_file, jsonify, request
import os
from lib import calendar, misc
import settings
from lib.telebot import Telebot
from datetime import datetime
import subprocess
import socket
import datetime
import logging
from logging.config import dictConfig

dictConfig({
    'version': 1,
    'formatters': {'default': {
        'format': '[%(asctime)s] %(levelname)s in %(module)s: %(message)s',
    }},
    'handlers': {'file': {
        'class': 'logging.handlers.RotatingFileHandler',
        'formatter': 'default',
        'filename': 'teamstv_flask.log',
        'backupCount': 3
    }},
    'root': {
        'level': 'INFO',
        'handlers': ['file']
    }
})

app = Flask(__name__)
tb = Telebot(settings.BOT_TOKEN)


@app.route('/fin_curr')
def show_fin_curr():
    app.logger.info("Serving fin_curr.html")
    return render_template("fin_curr.html")


@app.route('/news')
def show_news():
    app.logger.info("Serving news.html")
    return render_template("news.html")


@app.route('/clock')
def show_clock():
    app.logger.info("Serving clock.html")
    return render_template("clock.html")


@app.route('/next')
def next_event():
    app.logger.info("passing /next to bot")
    tb.handle({'text': '/next'})
    return jsonify('OK')


@app.route('/resume')
def resume_event():
    app.logger.info("passing /resume to bot")
    tb.handle({'text': '/resume'})
    return jsonify('OK')


@app.route("/telebot", methods=['GET'])
def get_telebot():
    def next():
        calendars = calendar.connect(settings.CALDAV_USER, settings.CALDAV_PASSWORD, settings.CALDAV_URL)
        data = calendar.get_next_events(calendars)
        return json.dumps(data, cls=misc.DateTimeEncoder)

    def resume():
        calendars = calendar.connect(settings.CALDAV_USER, settings.CALDAV_PASSWORD, settings.CALDAV_URL)
        data = calendar.get_current_events(calendars, settings.TIME)
        return json.dumps(data, cls=misc.DateTimeEncoder)

    cmds = {
        'next': next,
        'resume': resume,
    }
    return cmds[tb.get()]()


@app.route("/test_json")
def test_json():
    calendars = calendar.connect(settings.CALDAV_USER, settings.CALDAV_PASSWORD, settings.CALDAV_URL)
    data = calendar.get_current_events(calendars, settings.TIME)
    return json.dumps(data, cls=misc.DateTimeEncoder)


@app.route("/events/current")
def get_current_events():
    app.logger.info("Serving current events")
    try:
        calendars = calendar.connect(settings.CALDAV_USER, settings.CALDAV_PASSWORD, settings.CALDAV_URL)
        app.logger.info("successfully connected to calendar")
    except Exception as e:
        eprint("Couldn't connect to external source: {0}".format(e))
        app.logger.warn("failed to connect to calendar: {0}".format(e))
        return json.dumps(list()), 500
    data = calendar.get_current_events(calendars, settings.TIME)
    app.logger.info("obtained schedule for now")
    return json.dumps(data, cls=misc.DateTimeEncoder)


# --- WEB CONTENT ---
@app.route("/")
@app.route("/index")
def test_index():
    app.logger.info("serving index")
    return render_template("index.html")


# TODO: move abs_path somewhere else out of each method
@app.route("/js/<script>")
def get_some_js(script):
    abs_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), settings.STATIC_FOLDER)
    return send_file(os.path.join(abs_path, "js", script), mimetype="application/json")


@app.route("/css/<style>")
def get_some_css(style):
    abs_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), settings.STATIC_FOLDER)
    return send_file(os.path.join(abs_path, "css", style), mimetype="text/css")


@app.route("/html/<html>")
def get_some_html(html):
    abs_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), settings.STATIC_FOLDER)
    return send_file(os.path.join(abs_path, "html", html), mimetype="text/html")


@app.route("/js/<dir>/<file>")
def get_nested_js(dir, file):
    img_path = os.path.join(settings.STATIC_FOLDER, "js")
    folder_path = os.path.join(img_path, dir)
    abs_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), folder_path)
    filename = os.path.join(abs_path, file)
    return send_file(filename, mimetype='application/json')


@app.route('/images/<folder>')
def get_image_list(folder):
    app.logger.info("Serving images from {0}".format(folder))
    filelist = get_folder_images(folder)
    with_mtime = request.args.get('mtime', None)
    app.logger.info("mtime param is {0}".format(with_mtime))
    order = request.args.get('order', "name")
    app.logger.info("order param is {0}".format(order))

    if order == 'mtime':
        filelist = [item[1] for item in
                    sorted((os.stat(path).st_mtime, path) for path in filelist)]
    else:
        filelist = sorted(filelist)

    if with_mtime is None:
        app.logger.info("Serving simple image list")
        return json.dumps(["images/" + folder + "/" + os.path.basename(file) for file in filelist])
    else:
        mtime = get_last_mtime(filelist)
        app.logger.info("Serving images with mtime")
        return json.dumps({
            "images": ["images/" + folder + "/" + os.path.basename(file) for file in filelist],
            "last_mtime": mtime
        }, cls=misc.DateTimeEncoder)


# TODO: move these two somewhere else
def get_folder_images(folder):
    img_path = os.path.join(settings.STATIC_FOLDER, settings.IMG_FOLDER)
    folder_path = os.path.join(img_path, folder)
    abs_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), folder_path)
    filelist = [os.path.join(abs_path, file) for file in os.listdir(abs_path) if
                file.lower().endswith(".png") or file.lower().endswith(".jpg")]
    return filelist


def get_last_mtime(filelist):
    last_mtime = datetime.datetime.now()
    for file in filelist:
        stat = os.stat(file)
        mtime = datetime.datetime.fromtimestamp(stat.st_mtime)
        if mtime < last_mtime:
            last_mtime = mtime
    return last_mtime


def get_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        # doesn't even have to be reachable
        s.connect(('10.255.255.255', 1))
        IP = s.getsockname()[0]
    except:
        IP = '127.0.0.1'
    finally:
        s.close()
    return IP

@app.route("/address")
def get_local_ip():
    ip = get_ip()
    app.logger.info("Serving ip addr: {0}".format(ip))
    return ip


@app.route('/lmt/images/<folder>/')
def get_image_folder_last_modify(folder):
    filelist = get_folder_images(folder)
    last_mtime = get_last_mtime(filelist)
    return json.dumps(last_mtime, cls=misc.DateTimeEncoder)


@app.route("/images/<folder>/<file>")
def get_image(folder, file):
    img_path = os.path.join(settings.STATIC_FOLDER, settings.IMG_FOLDER)
    folder_path = os.path.join(img_path, folder)
    abs_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), folder_path)
    filename = os.path.join(abs_path, file)
    return send_file(filename, mimetype='image/gif')


# --- WEB CONTENT END ---

if __name__ == '__main__':
    tb.start()
    app.run(threaded=True, host='0.0.0.0')
