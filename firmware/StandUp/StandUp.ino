/*
 * StandUp.ino
 * Sketch to sense the status of a person sitting on a chair using an FSR, and
 * transmit the status to an Android phone via Bluetooth.
 *
 * Created by Jayanth Chennamangalam on 2014.12.22
 * Original idea for StandUp: Kurian Jacob
 */

/* booleans */
#define FALSE                 0
#define TRUE                  1

/* serial communication */
#define BAUD_RATE             9600

/* pressure sensor */
#define SENSOR_PORT           A0
#define SENSOR_MIN            0
#define SENSOR_MAX            1023
#define SENSOR_INTERVAL       100      /* ms */

/* with no pressure (only pressure of chair) applied, the sensor value is 1023
   or a little lower, and while seated, the sensor value varies from ~970-990.
   the difference is > 20, so define that as the minimum difference */
#define SENSOR_STATE_DIFF     20

/* sitting status */
#define STATE_STANDING        0
#define STATE_SITTING         1

/* message strings */
#define MSG_STANDING          "User just stood up."
#define MSG_SITTING           "User just sat down."

/* indicator LED */
#define STATE_LED             12

/* global variable to hold previous state */
int prevSensorVal = SENSOR_MAX;
/* current state */
int sensorVal = SENSOR_MAX;
/* first iteration status */
int first = TRUE;
/* state */
int state = STATE_STANDING;

void setup()
{
  Serial.begin(BAUD_RATE);
  pinMode(STATE_LED, OUTPUT);
}

void loop()
{
  sensorVal = analogRead(SENSOR_PORT);

  /* the difference need to be at least SENSOR_STATE_DIFF to reduce the
     prospect of noise, also to use as a trigger */
  if (abs(sensorVal - prevSensorVal) >= SENSOR_STATE_DIFF)
  {
    updateState();
    prevSensorVal = sensorVal;
  }
  else if (first)
  {
    updateState();
    first = FALSE;
  }

  sendState();
  delay(SENSOR_INTERVAL);
}

void updateState()
{
  int LEDState = LOW;
  char* msgs[2] = { MSG_STANDING, MSG_SITTING };

  if (sensorVal < prevSensorVal)
  {
    state = STATE_SITTING;
    LEDState = HIGH;
  }
  else
  {
    state = STATE_STANDING;
    LEDState = LOW;
  }

  digitalWrite(STATE_LED, LEDState);
}

void sendState()
{
  if (Serial.available())
  {
    //Serial.println(msgs[state]);
    Serial.print(state);
    //Serial.println(sensorVal);
  }
}

