import json
import requests

from apis import index, apiai_url, headers


def update_all():
    res = index.browse_all()
    song_names = set()
    song_synonyms = {}
    artist_names = set()

    # Store all songs and artists
    for hit in res:
        song_name = hit["trackName"]
        if "(" in song_name:
            # Store eventual synonyms
            if song_name.startswith("("):
                split = song_name.split(")")
                song_name = split[1]
                song_synonym = split[0].replace("(", "")
            else:
                split = song_name.split("(")
                song_name = split[0]
                song_synonym = split[1].replace(")", "")
            if song_name in song_synonyms:
                song_synonyms[song_name].append(song_synonym)
            else:
                song_synonyms[song_name] = [song_synonym]

        song_names.add(song_name)
        artist_names.add(hit["artistName"])

    update_entity('Song', song_names, song_synonyms)
    update_entity('Artist', artist_names)


def update_entity(entity, names, synonyms=None):
    res = requests.delete(apiai_url + "/entities/%s" % entity, headers=headers)
    print("Delete existing %ss: %s." % (entity.lower(), res.json()['status']['errorType']))
    data = {'name': entity, 'entries': []}
    for name in names:
        entry = {'value': name}
        if synonyms and name in synonyms:
            entry['synonyms'] = synonyms[name]
        data['entries'].append(entry)
    json_data = json.dumps(data)
    res = requests.post(apiai_url + "/entities", data=json_data, headers=headers)
    print("Send new %ss: %s.\n" % (entity, res.json()['status']['errorType']))


if __name__ == "__main__":
    update_all()
