import os

import h5py
import algoliasearch
import time

import math

APP_ID = "TDNMRH8LS3"
API_KEY = "d271815c0cc8467a478cd8ce005dcb5e"
INDEX_NAME = "million_songs"
folder = '/tmp/MillionSongSubset/data/'


def list_files(dir):
    r = []
    for root, dirs, files in os.walk(dir):
        for name in files:
            r.append(os.path.join(root, name))
    return r


def get_and_sanitize(metadata, index):
    value = metadata[index]
    if isinstance(value, bytes):
        value = value.decode("utf8")
    elif math.isnan(value):
        value = None
    return value


files = list_files(folder)
count = len(files)
print("%d files to process." % count)

songs = []
for i, filename in enumerate(files):
    f = h5py.File(filename, 'r')

    md_songs = f["/metadata/songs"]
    a_songs = f["/analysis/songs"]

    for md_song, a_song in zip(md_songs, a_songs):
        codec = "utf8"
        song = {
            'artist_familiarity': get_and_sanitize(md_song, 2),
            'artist_hotttnesss': get_and_sanitize(md_song, 3),
            'artist_location': get_and_sanitize(md_song, 6),
            'artist_name': get_and_sanitize(md_song, 9),
            'genre': get_and_sanitize(md_song, 11),
            'release': get_and_sanitize(md_song, 14),
            'song_hotttnesss': get_and_sanitize(md_song, 16),
            'title': get_and_sanitize(md_song, 18),
            'audio_md5': get_and_sanitize(a_song, 1),
            'danceability': get_and_sanitize(a_song, 2),
            'duration': get_and_sanitize(a_song, 3),
            'energy': get_and_sanitize(a_song, 5),
            'key': get_and_sanitize(a_song, 21),
            'key_confidence': get_and_sanitize(a_song, 22),
            'loudness': get_and_sanitize(a_song, 23),
            'mode': get_and_sanitize(a_song, 24),
            'mode_confidence': get_and_sanitize(a_song, 25),
            'tempo': get_and_sanitize(a_song, 27),
            'time_signature': get_and_sanitize(a_song, 28),
            'time_signature_confidence': get_and_sanitize(a_song, 29),
        }
        print("Song %i/%i: %s" % (i, count, song))
        songs.append(song)

client = algoliasearch.client.Client(APP_ID, API_KEY)
res = client.delete_index(INDEX_NAME)
print("Delete index: %s." % res)
index = client.init_index(INDEX_NAME)
res = index.add_objects(songs)
print("Add objects: %s." % res)