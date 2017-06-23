import json
import uuid
import sys

from apis import index, ai


if len(sys.argv) != 2:
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

if action == "search.artist":
    artist = result['parameters']['artist']
    print("Searching for %s..." % artist)
    hits = index.search(artist  )['hits']
    songs = [hit['trackName'] for hit in hits]
    print("Found %s songs: %s." % (len(hits), ", ".join(songs)))
else:
    print("Weird action: %s." % result['action'])
