/*****************************************************************************
 *  PhraseCraze is a family friendly word game for mobile phones.
 *  Copyright (C) 2011 Siramix Team
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ****************************************************************************/
package com.siramix.phrasecraze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Siramix Labs
 * Client for communicating with the phrasecraze pack server
 */
public class PackClient {
  
  /**
   * URL Constants
   */
  private static final String URL_BASE = "http://siramix.com/phrasecraze/packs/";
  private static final String LIST_URL = "list.json";
  
  /**
   * Members
   */
  private static PackClient mInstance = null;

  /**
   * Return the intance of the PackClient object
   * @return
   */
  public static PackClient getInstance() {
    if(mInstance == null) {
      mInstance = new PackClient();
    }
    return mInstance;
  }

  public LinkedList<Pack> getPacks() throws IOException, URISyntaxException, JSONException {
    BufferedReader in = null;
    LinkedList<Pack> ret = null;
    in = doHTTPGet(URL_BASE+LIST_URL);
    ret = PackParser.parsePacks(in);
    return ret;
  }

  public CardJSONIterator getCardsForPack(Pack pack) throws IOException, URISyntaxException {
    BufferedReader in = null;
    CardJSONIterator ret = null;
    String packURL = pack.getPath();
    in = doHTTPGet(URL_BASE+packURL);
    ret = PackParser.parseCards(in);
    return ret;
  }

  private static BufferedReader doHTTPGet(String url) throws IOException, URISyntaxException {
    HttpClient client = new DefaultHttpClient();
    HttpGet request = new HttpGet();
    request.setURI(new URI(url));
    HttpResponse response = client.execute(request);
    BufferedReader ret = new BufferedReader
    (new InputStreamReader(response.getEntity().getContent()));
    return ret;
  }
}
