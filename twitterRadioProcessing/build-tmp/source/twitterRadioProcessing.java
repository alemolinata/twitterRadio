import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.io.BufferedReader; 
import java.io.InputStreamReader; 
import java.lang.Runtime; 
import java.util.regex.Pattern; 
import java.util.regex.Matcher; 
import processing.serial.*; 
import com.temboo.core.*; 
import com.temboo.Library.Twitter.Search.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class twitterRadioProcessing extends PApplet {













// Create a session using your Temboo account application details
TembooSession session = new TembooSession("duffman7", "myFirstApp", "0629a4b5ec194d60bd3022c7eecd2844");
//String [] params =  { "/usr/bin/say", "test"};

String lastNews;
String[] mQueries = new String[]{"twittertemboociid", "ciid", "copenhagen", "sunny", "rainy", "happy", "random", "objects"};
String[] tweets = new String[3];
String[] users = new String[3];
String[] url;

ArrayList<Query> queries = new ArrayList<Query>();
ArrayList<Query> queriesToSearch = new ArrayList<Query>();

Process p;

int queryCounter = 0;

String id;
boolean macIsTalking;

Serial myPort;

boolean radioOn = false;
boolean prevRadioOn = false;

int radiusValue = 0;
int toneValue = 0;
int volumeValue = 0;

int prevRadiusValue = 0;
int prevToneValue = 0;
int prevVolumeValue = 0;

public void setup() {

  //println(Serial.list()); 
  myPort = new Serial(this, Serial.list()[3], 9600);
  myPort.bufferUntil('\n');

  queries.add(new Query(0, 0, "#copenhagenshooting", "Copenhagen Shooting"));
  queries.add(new Query(0, 0, "#copenhagenryanair", "Ryan Air now flies to Copenhagen"));
  queries.add(new Query(0, 0, "copenhagen riots", "Copenhagen Riots"));
  queries.add(new Query(0, 1, "copenhagen cycling", "Copenhagen Cycling"));
  queries.add(new Query(0, 1, "distortion copenhagen", "Copenhagen Distortion Celebration"));
  queries.add(new Query(0, 1, "#techBBQ", "Tech barbecue, Annual Networking Event"));
  queries.add(new Query(0, 2, "copenhagen trivia", "copenhagen trivia"));
  queries.add(new Query(0, 2, "queen margarethe", "queen margarethe"));
  queries.add(new Query(0, 2, "copenhagen fashion week", "copenhagen fashion week"));

  queries.add(new Query(1, 0, "paris jewish", "Jewish man attacked in Paris"));
  queries.add(new Query(1, 0, "london riots", "London Riots"));
  queries.add(new Query(1, 0, "crimea conflict", "crimea conflict"));
  queries.add(new Query(1, 1, "merkel election", "merkel election"));
  queries.add(new Query(1, 1, "UK exit EU", "United Kingdom to leave Europian Union"));
  queries.add(new Query(1, 1, "Marriage referendum", "Marriage referendum"));
  queries.add(new Query(1, 2, "glastonbury", "Glastonbury Festival"));
  queries.add(new Query(1, 2, "eurovision", "Eurovision Song Contest"));
  queries.add(new Query(1, 2, "cannes 2015", "Cannes Festival 2015"));

  queries.add(new Query(2, 0, "snowden", "snowden"));
  queries.add(new Query(2, 0, "ISIS", "ISIS"));
  queries.add(new Query(2, 0, "syria conflict", "syria conflict"));
  queries.add(new Query(2, 1, "SABC news", "South African Broadcasting Corp"));
  queries.add(new Query(2, 1, "TPP Japan", "Japn Trans Pacific Partnership"));
  queries.add(new Query(2, 1, "NASA", "NASA"));
  queries.add(new Query(2, 2, "#soccernews", "Soccer"));
  queries.add(new Query(2, 2, "#celebnews", "Celebrities"));
  queries.add(new Query(2, 2, "movie news", "Movies"));

  getQueries();
  runTweetsChoreo(queriesToSearch.get(queryCounter));

}

public void runTweetsChoreo(Query query) {
  // Create the Choreo object using your Temboo session
  Tweets tweetsChoreo = new Tweets(session);

  // Set credential
  tweetsChoreo.setCredential("TwitterAccount");

  // Set inputs
  tweetsChoreo.setCount("3");
  tweetsChoreo.setQuery(query.query);
  tweetsChoreo.setLanguage("en");
  tweetsChoreo.setResultType("mixed");

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
    //println(screenName, tweetText);

    tweetText = tweetText + " ";
    tweetText = tweetText.replaceAll("^[Rr][Tt] ", "");
    tweetText = tweetText.replaceAll("#", "");
    tweetText = tweetText.replaceAll("http://(.*?) ", "");
    users[i] = screenName;
    tweets[i] = tweetText;
  }

  String tweetsString = "Story " + query.title + ". this is what people are saying. ";
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

public void draw() {
  if(radioOn != prevRadioOn){
    if(!radioOn){
      stopTalking();
    }
    prevRadioOn = radioOn;
  }
  if(radioOn){
    if(volumeValue != prevVolumeValue){
      setVolume();
    }
    getValues();
    isTalking();
    if(!macIsTalking){
      queryCounter ++;
      if (queryCounter>2){
        queryCounter = 0;
      }
      runTweetsChoreo(queriesToSearch.get(queryCounter));
    }
  }
}

public void stopTalking(){
  String processId = isTalking();
  if(macIsTalking){
    try {
      String[] toKill = {"kill", processId};
      p = Runtime.getRuntime().exec(toKill);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

public void mousePressed() {
  stopTalking();
}

public void getValues(){
  if(radiusValue != prevRadiusValue || toneValue != prevToneValue){
    stopTalking();
    getQueries();
    queryCounter = 0;
    prevRadiusValue = radiusValue;
    prevToneValue = toneValue;
  }
}

public void getQueries(){
  queriesToSearch.clear();
  for(int i = 0; i<queries.size(); i++){
    if(queries.get(i).radius == radiusValue && queries.get(i).tone == toneValue){
      queriesToSearch.add(queries.get(i));
    }
  }
}

public String isTalking() {
  String id = "";
  
  try {
    String process;
    // getRuntime: Returns the runtime object associated with the current Java application.
    // exec: Executes the specified string command in a separate process.
    Process p = Runtime.getRuntime().exec("ps aux");
    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
    while ((process = input.readLine()) != null) {
      if(process.contains("say")) {
        
        //println(process);
        
        String paa = "\\d{5}";
        Pattern pattern = Pattern.compile(paa, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(process);
        matcher.find();
        id = matcher.group(0);
        
        //println(id);
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

public void setVolume(){
  String[] command = {"osascript", "-e", "set volume " + volumeValue};
  try {
    Runtime.getRuntime().exec(command);
  } 
  catch (Exception e) {
    e.printStackTrace();
  }
}

public void serialEvent(Serial thisPort) { 
  // read the serial buffer:
  String inputString = thisPort.readStringUntil('\n');

  if (inputString != null)
  {
    // trim the carrige return and linefeed from the input string:
    inputString = trim(inputString);

    // split the input string at the commas
    // and convert the sections into integers:
    int sensors[] = PApplet.parseInt(split(inputString, ','));

    // if we have received all the sensor values, use them:
    if (sensors.length == 4) {
      radiusValue = sensors[0];
      toneValue = sensors[1];
      volumeValue = sensors[2];
      if(sensors[3] == 0){
        radioOn = false;
      }
      else{
        radioOn = true;
      }
      //println(radiusValue + ", " + toneValue + ", " + volumeValue);
    }
  }
}


class Query {
  public int radius;
  public int tone;
  public String query;
  public String title;

  public Query( int radiusValue, int toneValue, String queryValue, String titleValue ) {
    radius = radiusValue;
    tone = toneValue;
    query = queryValue;
    title = titleValue;
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "twitterRadioProcessing" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
