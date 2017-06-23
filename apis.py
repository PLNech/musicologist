import os

import apiai
from algoliasearch import algoliasearch

# Algolia
app_id = os.environ.get("ALGOLIA_APPLICATION_ID")
api_key = os.environ.get("ALGOLIA_API_KEY")
client = algoliasearch.Client(app_id, api_key)
index = client.init_index("songs")

# API.AI
token_dev = '***REMOVED***'
apiai_url = "https://api.api.ai/v1"
headers = {"Authorization": "Bearer " + token_dev,
           "Content-Type": "application/json"}
ai = apiai.ApiAI(token_dev)
