int radiusReadingPrev = 0;
int toneReadingPrev = 0;
int volumeReadingPrev = 0;
int switchReadingPrev = 0;

int switchState = 0;
int switchStatePrev = 0;

boolean sendSwitch = false;

long lastDebounceTime = 0;
long debounceDelay = 50;

void setup() {
  pinMode(2, INPUT);
  Serial.begin(9600); //Establish rate of Serial communication
  //establishContact(); //See function below
}

void loop() {
  //if (Serial.available() > 0) { //If we've heard from Processing do the following
  int inByte = Serial.read();

  int radiusReading = analogRead(A0);
  int toneReading = analogRead(A1);
  int volumeReading = analogRead(A2);
  int switchReading = digitalRead(2);

  volumeReading = 1023 - volumeReading;
  
  int radiusDif = abs(radiusReading - radiusReadingPrev);
  int toneDif = abs(toneReading - toneReadingPrev);
  int volumeDif = abs(volumeReading - volumeReadingPrev);
  
  // debounce for the switch
  if (switchReading != switchReadingPrev) {
    lastDebounceTime = millis();
  } 
  if ((millis() - lastDebounceTime) > debounceDelay) {
    switchState = switchReading;
    
    if(switchState != switchStatePrev){
      sendSwitch = true;
      
      switchStatePrev = switchState;
    }
  }
  switchReadingPrev = switchReading;

  if(radiusDif > 20 || toneDif > 20 || volumeDif > 20 || sendSwitch){
    int radiusValue = int(radiusReading/342);
    int toneValue = int(toneReading/342);
    int volumeValue = int(round(volumeReading/102.3));

    Serial.print(radiusValue);
    Serial.print(",");
    Serial.print(toneValue);
    Serial.print(",");
    Serial.print(volumeValue);
    Serial.print(",");
    Serial.println(switchState);
    
    radiusReadingPrev = radiusReading;
    toneReadingPrev = toneReading;
    volumeReadingPrev = volumeReading;
  }
  sendSwitch = false;
  //}
}
/*
void establishContact() {
  // when Arduino receives a Serial message from Processing
  while (Serial.available() <= 0) {
    Serial.println("hello");
    delay(300);
  }
}*/
