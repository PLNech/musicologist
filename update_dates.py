import json
import requests

from apis import index
from dateutil import parser

def update_all():
    res = index.browse_all()

    # Store all songs and artists
    for hit in res:
        song_date = hit["releaseDate"].split(" ")[0]
        timestamp = int(parser.parse(song_date).timestamp())
        print("Date: %s -> Timestamp %s.\nUpdating..." % (song_date, timestamp))
        index.partial_update_object({"releaseTimestamp": timestamp,
                                     "objectID": hit["objectID"]})

if __name__ == "__main__":
    update_all()
