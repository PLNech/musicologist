# Examples

## Search
- > Do you know songs by the rollin stun?
- > Any music released by Eminim?  
- > Find songs released in 1991  
- > Do you know music from the seventies?  
- > What tunes come from the 70s?  
- > Give me the songs made by Eminem between 1960 and 2010  
- > Find songs made by the Rolling Stones since 2000  
- > Any new song since New Year's Eve 2000?     

# TODO

improve dataset
typo tolerance on artists?
solve missing words in input.original
solve "the 80s/the 70s" generalisation issue
# FIXME

- Do you know music from the seventies?
-> `Searching for songs [missing] the seventies`

- What tunes come from the 80s?
-> `input.unknown`

# Outline
- Get user query
- Send to API.AI for NLU
- Parse response to get search parameters
- Send to Algolia
- Display results
