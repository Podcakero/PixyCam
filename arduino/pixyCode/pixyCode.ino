//
// begin license header
//
// This file is part of Pixy CMUcam5 or "Pixy" for short
//
// All Pixy source code is provided under the terms of the
// GNU General Public License v2 (http://www.gnu.org/licenses/gpl-2.0.html).
// Those wishing to use Pixy source code, software and/or
// technologies under different licensing terms should contact us at
// cmucam@cs.cmu.edu. Such licensing terms are available for
// all portions of the Pixy codebase presented here.
//
// end license header
//
// This sketch is a good place to start if you're just getting started with 
// Pixy and Arduino.  This program simply prints the detected object blocks 
// (including color codes) through the serial console.  It uses the Arduino's 
// ICSP SPI port.  For more information go here:
//
// https://docs.pixycam.com/wiki/doku.php?id=wiki:v2:hooking_up_pixy_to_a_microcontroller_-28like_an_arduino-29
//
  
#include <Pixy2.h>
#include "Adafruit_VL6180X.h"

Adafruit_VL6180X vl = Adafruit_VL6180X();

// This is the main Pixy object 
Pixy2 pixy;

void setup()
{
  Serial.begin(115200);
  Serial.print("Starting...\n");
  
  pixy.init();
}

void loop()
{ 
  float lux = vl.readLux(VL6180X_ALS_GAIN_5);

  uint8_t range = vl.readRange();
  uint8_t status = vl.readRangeStatus();
  
  // grab blocks!
  pixy.ccc.getBlocks();

  bool stage1Done = false;
  bool stage2Done = false;
  bool stage3Done = false;
  
  // If there are detect blocks, print them!
  if (pixy.ccc.numBlocks >= 2)
  {
    if (pixy.ccc.blocks[0].m_x > pixy.ccc.blocks[1].m_x)
      Serial.println(pixy.ccc.blocks[0].m_width - pixy.ccc.blocks[1].m_width);
    else if (pixy.ccc.blocks[1].m_x > pixy.ccc.blocks[0].m_x)
      Serial.println(pixy.ccc.blocks[1].m_width - pixy.ccc.blocks[0].m_width);
    else if (pixy.ccc.blocks[1].m_width == pixy.ccc.blocks[0].m_width)
    {
      if (!stage1Done)
      {
        Serial.println("Done");
        stage1Done = true;
      }
      if (((pixy.ccc.blocks[0].m_x + pixy.ccc.blocks[1].m_x) / 2) == 200)
      {
        if (!stage2done)
        {
          Serial.println("Done");
          stage2Done = true;
        }
        if (range >= 100)
          Serial.println(range);
        else
        {
          if (!stage3Done)
          {
            Serial.println("Done");
            stage3Done = true;
          }
        }
          
      }
      else
        Serial.println((pixy.ccc.blocks[0].m_x + pixy.ccc.blocks[1].m_x) / 2);
    }
  }
}
