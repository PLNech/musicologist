import h5py
import algoliasearch
import time

APP_ID = "TDNMRH8LS3"
API_KEY = "d271815c0cc8467a478cd8ce005dcb5e"
INDEX_NAME = "million_songs"
filename = '/tmp/MillionSongSubset/data/A/A/A/TRAAAAW128F429D538.h5'
f = h5py.File(filename, 'r')

md_songs = f["/metadata/songs"]
a_songs = f["/analysis/songs"]

for md_song, a_song in zip(md_songs, a_songs):
    song = {
        'artist_familiarity': md_song[2],
        'artist_hotttnesss': md_song[3],
        'artist_location': md_song[6].decode("ascii"),
        'artist_name': md_song[9].decode("ascii"),
        'genre': md_song[11].decode("ascii"),
        'release': md_song[14].decode("ascii"),
        'song_hotttnesss': md_song[16],
        'title': md_song[18].decode("ascii"),

        'audio_md5': a_song[1].decode("ascii"),
        'danceability': a_song[2],
        'duration': a_song[3],
        'energy': a_song[5],
        'key': a_song[21],
        'key_confidence': a_song[22],
        'loudness': a_song[23],
        'mode': a_song[24],
        'mode_confidence': a_song[25],
        'time_signature': a_song[28],
        'time_signature_confidence': a_song[29],
        'tempo': a_song[27],
    }
    print("Processed song: %s" % song)

    client = algoliasearch.client.Client(APP_ID, API_KEY)
    res = client.delete_index(INDEX_NAME)
    print(res)
    res = index = client.init_index(INDEX_NAME)
    print(res)
    res = index.add_object(song)
    print(res)
