import json
import requests

from apis import index, apiai_url, headers


def sanitize(string):
    return string.replace("_", ",").translate(str.maketrans("", "", "¬\'\"")).strip().split("(")[0]

def update_all():
    res = index.browse_all()
    song_names = set()
    song_synonyms = {}
    artist_names = set()

    # Store all songs and artists
    for hit in res:
        song_title = sanitize(hit["title"])
        artist_name = sanitize(hit["artist_name"])
        if "\"" in song_title:
            shitty.append(song_title)
        if "\"" in artist_name:
            shitty.append(artist_name)
        song_name = ""
        if "(" in song_title:
            # Store eventual synonyms
            if song_title.startswith("("):  # Wrapped title, remove parenthesis
                if song_title.endswith(")"):
                    song_name = song_title.replace("(", "").replace(")", "")
                    # print("Unwrapped: %s" % song_name)
                else:  # Prefixed song, synonym is prefix + name
                    split = song_title.split(")")
                    song_name = split[1]
                    song_synonym = split[0].replace("(", "").replace(")", "") + song_name
                    # print("Prefixed: %s (%s)" % (song_name, song_synonym))
                    if song_name in song_synonyms:
                        song_synonyms[song_name].append(song_synonym)
                    else:
                        song_synonyms[song_name] = [song_synonym]
            elif song_title.endswith("("): # Postfixed song, synonym is name + postfix
                print("Postfix: %s" % song_title)


                # ASDASDDSJKFHFKDSJGHSDAKJFHSKJFHDSKJFHKDSJFHDSF
    #         if song_name.startswith("("):
    #             split = song_name.split(")")
    #             song_basename = split[1]
    #             song_synonym = split[0].replace("(", "") + song_name
    #         else:
    #             split = song_name.split("(")
    #             song_basename = split[0]
    #             song_synonym = split[1].replace(")", "")
    #         if song_basename in song_synonyms:
    #             song_synonyms[song_basename].append(song_synonym)
    #         else:
    #             song_synonyms[song_basename] = [song_synonym]
    #         print("Split song: %s -> %s (%s)." % (song_name, song_basename, song_synonym))
    #         if "(" in song_name or "(" in song_synonym:
    #             bug = True
    #             shitty_songs.append(song_name)
    #
    # if bug:
    #     print("BUG! Shitty songs: %s." % shitty_songs)
    #     exit(2)
        if "(" in song_name or "[" in song_name:
            print("Buggy name: %s.")
            exit(2)
        if "_" in song_name:
            print("SORCERY! %s" % song_title)
            exit(1)

        song_names.add(song_name)
        artist_names.add(artist_name)

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
    with open("/tmp/data.json", "w+") as f:
        json.dump(data, f)
    res = requests.post(apiai_url + "/entities", data='''{"name": "Song", "entries": [{"value": "You're Timeless To Me Hairspray"}]}''', headers=headers)
    print("Send new %ss: %s.\n" % (entity, res.json()['status']['errorType']))
    print(res.json())
    # TODO: Entity with the name 'Song' already exists.
    # print(json.dumps(data, indent=1))

if __name__ == "__main__":
    update_all()
