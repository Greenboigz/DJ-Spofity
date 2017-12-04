import os
import urllib

from google.appengine.api import users
from google.appengine.ext import ndb


import webapp2 
import logging
import json




class MusicQueue(ndb.Model):
    uri = ndb.StringProperty() 
    ranking = ndb.IntegerProperty() 
    extraInfo = ndb.StringProperty()




class MainPage(webapp2.RequestHandler):

    def get(self): 
        self.response.headers['Content-Type'] = 'application/json' 
        lst = []
        query = ndb.gql("SELECT * FROM MusicQueue ORDER BY ranking DESC")
        for entry in query:
        # logging.info("entry name is " + entry.uri)
            obj = {"uri": entry.uri,"ranking" : entry.ranking, "extraInfo":entry.extraInfo}
            lst.append(obj) 

        self.response.write(json.dumps(lst))

 
    def post(self): 
        logging.info("POST - got post")     # some debugging; shows up in the dev_appserver console output (local)
        # or app engine log files when deployed
        logging.info("POST: payload: " + self.request.get('uri'))
        entry = MusicQueue(uri = self.request.get('uri'),ranking = int(self.request.get('ranking')),extraInfo = self.request.get('extraInfo')) 
        logging.info(entry)
        entry.put() 

        logging.info( "posting")

    def put(self): 
        self.response.headers['Content-Type'] = 'application/json' 
        uri1 = self.request.get('uri')
        ranking = int(self.request.get('ranking'))
        extraInfo = self.request.get('extraInfo')
        # query = ndb.gql("SELECT * FROM MusicQueue WHERE uri = :newURI", newURI = uri1)
        query = MusicQueue.query(MusicQueue.uri == uri1)
        
        for entry in query:
            entry.ranking = entry.ranking + ranking;
            if extraInfo == 'delete' :
                entry.extraInfo = 'delete'
            entry.put()  
        

    def delete(self): 
        self.response.headers['Content-Type'] = 'application/json' 
        query = ndb.gql("SELECT * FROM MusicQueue WHERE extraInfo = 'delete'") 
        for entry in query:
            entry.key.delete() 



# [START app]
app = webapp2.WSGIApplication([
    ('/', MainPage),
    
], debug=True)
# [END app]
