
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
    int input = mySerial.read();
    //This exists for debugging purposes
    Serial.write(input);
    //This is dirty, but it works.
    int incomingByte = input - '0';
    int moduls = incomingByte%4;
    if(incomingByte != lastByte)
    {
      setEL(moduls);
    }
  }
  if (Serial.available())
  {
    mySerial.write(Serial.read());
  }
}
