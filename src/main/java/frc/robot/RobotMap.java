/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;
import io.github.pseudoresonance.pixy2api.*;
import io.github.pseudoresonance.pixy2api.links.*;

/**
 * The RobotMap is a mapping from the ports sensors and actuators are wired into
 * to a variable name. This provides flexibility changing wiring, makes checking
 * the wiring easier and significantly reduces the number of magic numbers
 * floating around.
 */
public class RobotMap {
  // For example to map the left and right motors, you could define the
  // following variables to use with your drivetrain subsystem.
  // public static int leftMotor = 1;
  // public static int rightMotor = 2;

  // If you are using multiple modules, make sure to define both the port
  // number and the module. For example you with a rangefinder:
  // public static int rangefinderPort = 1;
  // public static int rangefinderModule = 1;

  //
  // Field dimensions in inches.
  //
  public static final double FIELD_LENGTH = 54*12.0;
  public static final double FIELD_WIDTH = 27*12.0;

  public static final double BATTERY_CAPACITY_WATT_HOUR = 18.0*12.0;

  //Pixy2 api taken from https://github.com/PseudoResonance/Pixy2JavaAPI

  public static Pixy2 pixyCam;

  public static SPILink pixySPILink;

  public static void init()
  {
    pixySPILink = new SPILink();
    pixySPILink.open(0);

    pixyCam = new Pixy2(pixySPILink);
  }
}
