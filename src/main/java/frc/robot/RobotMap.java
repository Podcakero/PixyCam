/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

<<<<<<< HEAD
=======
import edu.wpi.first.wpilibj.SPI;
import frc.team2890libs.*;
import frc.team2890libs.frclib.*;

>>>>>>> parent of 2291757... Removed Team492 libs. Added PseudoResonance libs. IT WORKS.git add .!
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

<<<<<<< HEAD
  public static void init()
  {
=======
  //
  // Vision subsystem.
  //
  public static final int PIXYCAM_WIDTH = 320;
  public static final int PIXYCAM_HEIGHT = 200;
  public static final int PIXY_BRIGHTNESS = 80;
  public static final double PIXY_CAM_OFFSET = 8.0;
  public static final PixyVision.Orientation PIXY_ORIENTATION = PixyVision.Orientation.NORMAL_LANDSCAPE;
  public static final SPI.Port PIXYCAM_SPI_PORT = FRCPixyCam.DEF_SPI_PORT;
  public static final int PIXY_TARGET_SIGNATURE = 1;

  public static PixyVision pixy;

  public static void init()
  {
    pixy = new PixyVision("PixyCam", PIXY_TARGET_SIGNATURE, PIXY_BRIGHTNESS, PIXY_ORIENTATION, PIXYCAM_SPI_PORT);
>>>>>>> parent of 2291757... Removed Team492 libs. Added PseudoResonance libs. IT WORKS.git add .!
  }
}
