import json
import os
import apiai
import requests
from algoliasearch import algoliasearch

# Algolia
app_id = os.environ.get("ALGOLIA_APPLICATION_ID")
api_key = os.environ.get("ALGOLIA_API_KEY")

client = algoliasearch.Client(app_id, api_key)
index = client.init_index("songs")

# API.AI
token_dev = '***REMOVED***'
base_url = "https://api.api.ai/v1"
headers = {"Authorization": "Bearer " + token_dev,
           "Content-Type": "application/json"}

ai = apiai.ApiAI(token_dev)


def update_entities():
    res = index.browse_all()
    song_names = set()
    song_synonyms = {}
    artist_names = set()

    # Store all songs (with synonyms) and artists
    for hit in res:
        song_name = hit["trackName"]
        if "(" in song_name:
            split = song_name.split("(")
            song_name = split[0]
            song_synonym = split[1].replace(")", "")
            if song_name in song_synonyms:
                song_synonyms[song_name].append(song_synonym)
            else:
                song_synonyms[song_name] = [song_synonym]

        song_names.add(song_name)
        artist_names.add(hit["artistName"])

    data_songs = {'name': 'Song', 'entries': []}
    for song in song_names:
        entry = {'value': song}
        if song in song_synonyms:
            entry['synonyms'] = song_synonyms[song]
        data_songs['entries'].append(entry)
    json_songs = json.dumps(data_songs)
    print("Songs payload: %s" % json_songs)

    print("Headers: %s" % headers)
    res = requests.put(base_url + "/entities", data=json_songs, headers=headers)
    print(res.json())


if __name__ == "__main__":
    update_entities()
