# StandUp
## README

StandUp is a smart device that monitors the amount of time a person is
sedentary and reminds the person to stand up after a predetermined duration. [Watch StandUp in action!](https://www.youtube.com/watch?v=0p9jelNucXs)

* `schematic/` contains a Fritzing schematic of the hardware connections.
* `firmware/` contains the Arduino sketch that reads data from a Force-Sensing Resistor (FSR), computes the status (user sitting or standing), and transmits that status using an HC-06 Bluetooth module to an Android phone.
* `app/` contains the Android app-related code and resources. Currently polls the Bluetooth device for the status and warns the user after 30 seconds.

System requirements:

* [Arduino Uno R3](http://arduino.cc/en/Main/arduinoBoardUno)
* [JY-MCU HC-06 Bluetooth module](http://www.amazon.com/KEDSUM%C2%AE-Arduino-Wireless-Bluetooth-Transceiver/dp/B0093XAV4U/)
* [FlexiForce 100 lb/440 N force sensor (FSR)](https://www.sparkfun.com/products/8685)
* 330 ohm resistor
* Jumper cables
* LED and 330 ohm resistor (optional)
* Computer with [Fritzing](http://fritzing.org/) to view the schematic, [Arduino IDE](http://arduino.cc/en/Main/Software) and [Android Studio](http://developer.android.com/sdk/index.html)
* Android phone

Created by Jayanth Chennamangalam based on the original idea by Kurian Jacob
