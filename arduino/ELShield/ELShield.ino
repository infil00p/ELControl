
#include <SoftwareSerial.h>

int inByte = 0;
int lastByte = 0;

SoftwareSerial mySerial(10, 11); // RX, TX

void setup() {
 for(int i = 4; i<8; i++)
 { 
  pinMode(i, OUTPUT);
 }
  Serial.begin(9600);

  mySerial.begin(9600);
}
 
void setEL(int ch) // set a certain EL on
{ 
 for(int i = 4; i<8; i++) // all off
 {
  digitalWrite(i, LOW);
 }
 digitalWrite(ch+4, HIGH); // ch on
} 
 
int count = 0; 
 
void loop()
{ 
  if (mySerial.available())
  {
    char input[1];
    input[0] = mySerial.read();
    //This exists for debugging purposes
    Serial.write(input[0]);
    //This is dirty, but it works.
    int incomingByte = atoi(input);
    //The application can't provide inputs higher than 4
    //so ignore them
    if(incomingByte < 4)
    {
      setEL(incomingByte);
    }
  }
  if (Serial.available())
  {
    mySerial.write(Serial.read());
  }
}
