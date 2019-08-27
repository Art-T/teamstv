import json
from datetime import datetime
# Here we import local caldav so that we are not dependent on buggy version of the pip installation
from lib.caldav.davclient import DAVClient
import pytz
import re
import requests
from dateutil import parser


def increment_dead_kittens():
    with open("dead_kittens", "r+") as f:
        read = f.read()
        if not read:
            dead_kittens = 1
        else:
            dead_kittens = int(read) + 1
        f.seek(0)
        f.write(str(dead_kittens))
        f.truncate()
    print("{0} kittens died".format(dead_kittens))


def extract_rss_feeds_from_html(description):
    # if there are no signs of tags, do the usual thing
    if ("<" not in description) and ">" not in description:
        return json.loads(description)
    # Ok, if we got here, the json contents are totally fucked up...
    # There are mixed overlapping quotes and weird tags. We are trying to fix this
    # The only solution I can imagine for it...
    # REGEXPS. Now you folks have problems...
    print("Tags found in rss description/configurations. attempting to extract links from tags. " \
          "\nThis is plain stupid..." \
          "\nDon't do this. A kitten dies every time this happens" \
          "\nYou know what? I'm counting...")
    increment_dead_kittens()
    # all contents from start of <a> tag to it's close,
    # except if there are many <a> tags, they must delimited with comma
    # and we don't want to grap from start of the first one to the end of the last one
    tag_regex = "<a[^,]*</a>"
    tags = re.findall(tag_regex, description)
    # positive look-behind of 'blocks":[',then any numbers delimited by comma and positive lookahead of ']'
    blocks_regex = "(?<=blocks\"\:\[)[\d,]*(?=\])"
    blocks_match = re.search(blocks_regex, description)
    if blocks_match is None:
        raise Exception("can't fucking parse fucking tags in fucking rss description")
    blocks_str = blocks_match.group()
    content = {"blocks": [int(t) for t in blocks_str.split(",")],
               "data": {
                   "feeds": []
               }}
    # positive look-behind of 'href="',then anything and positive lookahead of '">'
    # So we match everything in between 'href="' and '">'
    url_regex = "(?<=href=\").*(?=\">)"
    for tag in tags:
        content["data"]["feeds"].extend(re.findall(url_regex, tag))
    return content


def connect(user, password, url):
    """Connects to the caldav service provider with given credentials, returns list of available calendars
    :param user (str) : username
    :param password (str) : password
    :param url (str) : url without protocol/credentials, e.g. caldav.yandex.ru
    :return ([calendar]): list of available calendars
    """
    full_url = "https://{0}:{1}@{2}".format(user, password, url)
    client = DAVClient(full_url)
    principal = client.principal()
    calendars = principal.calendars()
    return calendars


def get_events(calendars, time_from, time_to):
    """gets available events from all given calendars
    :param calendars([calendar]):list of available calendars, active
    :param time_from(datetime):time to search from
    :param time_to(datetime): time to search to
    :return ([{event}]): list of obtained events for given period of time
    """
    res = list()
    for calendar in calendars:
        search_result = calendar.date_search(time_from, time_to)
        for event in search_result:
            event.load()
            e = event.instance.vevent
            summary_value = e.summary.value
            summary = summary_value.split(':')[0] if ":" in summary_value else summary_value
            start = e.dtstart.value
            end = e.dtend.value
            try:
                description = e.description.value
                if summary == "rss":
                    contents = extract_rss_feeds_from_html(description)
                else:
                    contents = json.loads(description)
            except Exception as err:
                contents = {"blocks": [], "data": {}}
            blocks = contents["blocks"]
            data = contents["data"]
            uid = e.uid.value
            last_modified = e.last_modified.value
            res.append({
                "sdate": start,
                "edate": end,
                "blocks": blocks,
                "widget": summary,
                "data": data,
                "uid": uid[:-3],  # YANDEX GENERATES UID WITH .RU AND WE CAN'T REALLY use it as tag id in html...
                "last_modified": last_modified
            })
    return res


def get_next_events():
    raise NotImplementedError("This feature in not yet implemented properly.")


def get_now(time_source):
    """
    :param time_source (str):LOCAL or url to get time from
    :return:
    """
    if time_source == "LOCAL":
        return datetime.now(pytz.utc)
    else:
        try:
            response = requests.get(time_source)
            time_source_utc = parser.parse(response.content).astimezone(pytz.utc) # CalDav drops the timezone, so the time should be in UTC
            return time_source_utc
        except requests.exceptions.BaseHTTPError as e:
            now = datetime.now(pytz.utc)
            print("something went wrong during getting time from {0}:\n{1}\nRETURNING LOCAL {2}".format(time_source,
                                                                                                        e, now))
            return now
        except ValueError as e:
            now = datetime.now(pytz.utc)
            print(
                "Could not parse external time\n"
                "something went wrong during getting time from {0}:"
                "\n{1}\nRETURNING LOCAL {2}".format(time_source, e, now))
            return now


def get_current_events(calendars, time_source):
    """Returns
    :param source_setting (str): LOCAL or url to get time from
    :param calendars([calendar]):list of available calendars, active
    :return ([{event}]): list of obtained events for current moment
    """

    return get_events(calendars, get_now(time_source), get_now(time_source))
