int radiusReadingPrev = 0;
int toneReadingPrev = 0;
int volumeReadingPrev = 0;

void setup() {
  Serial.begin(9600); //Establish rate of Serial communication
  //establishContact(); //See function below
}

void loop() {
  //if (Serial.available() > 0) { //If we've heard from Processing do the following
    
    int inByte = Serial.read();

    int radiusReading = analogRead(A0);
    int toneReading = analogRead(A1);
    int volumeReading = analogRead(A2);

    int radiusDif = abs(radiusReading - radiusReadingPrev);
    int toneDif = abs(toneReading - toneReadingPrev);
    int volumeDif = abs(volumeReading - volumeReadingPrev);

    if(radiusDif > 20 || toneDif > 20 || volumeDif > 20){
      int radiusValue = int(radiusReading/342);
      int toneValue = int(toneReading/342);
      int volumeValue = int(round(volumeReading/102.3));

      Serial.print(radiusValue);
      Serial.print(",");
      Serial.print(toneValue);
      Serial.print(",");
      Serial.println(volumeValue);

      radiusReadingPrev = radiusReading;
      toneReadingPrev = toneReading;
      volumeReadingPrev = volumeReading;
    }
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
