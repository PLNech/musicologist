#! /usr/bin/env python3
import json
import uuid
import sys
from dateutil.parser import parse

from apis import index, ai

if len(sys.argv) != 2:
    print("Unrecognized argument: %s." % " ".join(sys.argv[1:]))
    print("Usage: %s QUERY")
    exit(1)
else:
    query = sys.argv[1]
    print('Q: "%s"' % query)

req = ai.text_request()
req.session_id = str(uuid.uuid4())
req.query = query

response = req.getresponse().read().decode('utf-8')
json_obj = json.loads(response)
result = json_obj['result']
action = result['action']

if action == "search":
    search_term = None
    params = {}
    hits = {}
    songs = []
    artist = result['parameters']['artist']
    if len(artist) is 0:
        artist = None
    period = result['parameters']['period']
    if isinstance(period, list):
        period = " / ".join(period)
    period_original = result['contexts'][0]['parameters']['period.original']

    if not artist and not period:
        print("NOTHING!!1! (%s)" % result['parameters'])
    else:
        if artist:
            search_term = artist
        if period:
            (start, end) = period.split("/")
            date_start = parse(start)
            date_end = parse(end)
            params['filters'] = 'release_timestamp: ' + str(date_start.timestamp()) + ' TO ' + str(date_end.timestamp())

        artist_text = " by %s" % artist if artist is not None else ""
        period_text = " %s" % period_original if period is not None else ""
        print("Searching for songs" + artist_text + period_text)
        hits = index.search(search_term, params)['hits']
        songs = [hit['trackName'] for hit in hits]

    if len(songs):
        print("Found %s songs: %s." % (len(hits), ", ".join(songs)))
    else:
        print("I'm afraid I know no song like this :(")
else:
    print("Weird action: %s." % result['action'])
