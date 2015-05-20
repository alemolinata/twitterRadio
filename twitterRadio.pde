import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Runtime;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.temboo.core.*;
import com.temboo.Library.Twitter.Search.*;

// Create a session using your Temboo account application details
TembooSession session = new TembooSession("duffman7", "myFirstApp", "0629a4b5ec194d60bd3022c7eecd2844");
//String [] params =  { "/usr/bin/say", "test"};

String lastNews;
String[] queries = new String[]{"twittertemboociid", "ciid", "copenhagen", "sunny", "rainy", "happy", "random", "objects"};
String[] tweets = new String[3];
String[] users = new String[3];
String[] url;

Process p;

int queryCounter = 0;
long waitTime = 15000;
long prevTime;

String id;
boolean macIsTalking;

boolean silenced = false;

void setup() {
  prevTime = millis();  
  runTweetsChoreo(queries[queryCounter]);
}

void runTweetsChoreo(String query) {
  // Create the Choreo object using your Temboo session
  Tweets tweetsChoreo = new Tweets(session);

  // Set credential
  tweetsChoreo.setCredential("TwitterAccount");

  // Set inputs
  tweetsChoreo.setCount("3");
  tweetsChoreo.setQuery(query);
  tweetsChoreo.setLanguage("en");
  tweetsChoreo.setResultType("recent");

  // Run the Choreo and store the results
  TweetsResultSet tweetsResults = tweetsChoreo.run();
  
  // Print results
  // println(tweetsResults.getResponse());
  // println(tweetsResults.getLimit());
  // println(tweetsResults.getRemaining());
  // println(tweetsResults.getReset());

  JSONObject json = JSONObject.parse(tweetsResults.getResponse());
  JSONArray statuses = json.getJSONArray("statuses");  
  
  for (int i = 0; i < statuses.size (); i++) {
    JSONObject tweet = statuses.getJSONObject(i);
    JSONObject user = tweet.getJSONObject("user");
    String screenName = user.getString("screen_name");
    String tweetText = tweet.getString("text");
    println(screenName, tweetText);

    tweetText = tweetText + " ";
    tweetText = tweetText.replaceAll("^[Rr][Tt] ", "");
    tweetText = tweetText.replaceAll("#", "hashtag ");
    tweetText = tweetText.replaceAll("http://(.*?) ", "");
    users[i] = screenName;
    tweets[i] = tweetText;
  }
  String tweetsString = "";
  for(int i = 0; i<3; i++){
    if(tweets[i]!= null){
      tweetsString += users[i] + " says " + tweets[i] + ". ";
    }
  }

  String[] params = { "/usr/bin/say", tweetsString};
  try {
    p = Runtime.getRuntime().exec(params);
    //p.waitFor();
  } 
  catch (Exception e) {
    e.printStackTrace();
  }
}

void draw() {
  String processId = isTalking();
  if(!macIsTalking && !silenced){
    queryCounter ++;
    if (queryCounter>7){
      queryCounter = 0;
    }
    runTweetsChoreo(queries[queryCounter]);
  }
  else if(macIsTalking && silenced){
    try {
      String[] toKill = {"kill", processId};
      p = Runtime.getRuntime().exec(toKill);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

void mousePressed() {
  silenced = ! silenced;
}

String isTalking() {
  
  String id = "";
  
  try {
    String process;
    // getRuntime: Returns the runtime object associated with the current Java application.
    // exec: Executes the specified string command in a separate process.
    Process p = Runtime.getRuntime().exec("ps aux");
    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
    while ((process = input.readLine()) != null) {
      if(process.contains("say")) {
        
        println(process);
        
        String paa = "\\d{5}";
        Pattern pattern = Pattern.compile(paa, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(process);
        matcher.find();
        id = matcher.group(0);
        
        println(id);
        macIsTalking = true;
        return id;
      }
    }
    input.close();
  } catch (Exception err) {
    err.printStackTrace();
  }
  macIsTalking = false;
  return id;
}