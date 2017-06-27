#! /usr/bin/env python3
import datetime
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
    print('Q: %s' % query)

req = ai.text_request()
req.session_id = str(uuid.uuid4())
req.query = query

response = req.getresponse().read().decode('utf-8')
json_obj = json.loads(response)
result = json_obj['result']
action = result['action']

if action == "search":
    artist = result['parameters']['artist']
    period = result['parameters']['period']
    if isinstance(period, list):
        period = " / ".join(period)
    period_original = result['contexts'][0]['parameters']['period.original']
    hits = {}
    songs = []
    if artist and not period:
        print("Searching for %s..." % artist)
        hits = index.search(artist)['hits']
        songs = [hit['trackName'] for hit in hits]
    elif period and not artist:
        (start, end) = period.split("/")
        date_start = parse(start)
        date_end = parse(end)
        print("Searching for songs released %s..." % period_original)
        filters = 'release_timestamp: ' + str(date_start.timestamp()) + ' TO ' + str(date_end.timestamp())
        hits = index.search(artist, {'filters': filters})['hits']
        songs = [hit['trackName'] for hit in hits]
    else:
        print("NOTHING!!1! (%s)" % result['parameters'])
    if len(songs):
        print("Found %s songs: %s." % (len(hits), ", ".join(songs)))
    else:
        print("I'm afraid I know no song like this :(")
else:
    print("Weird action: %s." % result['action'])
