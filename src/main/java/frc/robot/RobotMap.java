/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.XboxController;
import frc.robot.subsystems.DriveTrainSubsystem;

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

  //PORT IDS
  public static final int DRIVER_CONTROLLER_PORT = 0;
  public static final int ASSISTANT_DRIVER_CONTROLLER_PORT = 1;
  public static final int LEFT_DRIVER_JOYSTICK_PORT = 0;
  public static final int RIGHT_DRIVER_JOYSTICK_PORT = 1;
  public static final int LEFT_FRONT_TALON_ID = 1;
  public static final int RIGHT_FRONT_TALON_ID = 2;
  public static final int LEFT_BACK_TALON_ID = 3;
  public static final int RIGHT_BACK_TALON_ID = 4;
  public static final int CENTER_TALON_ID = 5;

  public static final int DRIVETRAIN_ENCODER_TICKS_PER_REVOLUTION = 4096;
  public static final int DRIVETRAIN_WHEEL_DIAMETER = 4;

  public static final double DRIVETRAIN_SPEED_MODIFIER = 0.5;
  public static final double DRIVETRAIN_FULL_SPEED = 1.0;
  public static final double DRIVETRAIN_FULL_STOP = 0.0;
  public static final double DRIVETRAIN_REVERSE_MODIFIER = -1.0;

  public static final double MAX_VELOCITY = 17.5;
  public static final double kV = 1.0 / MAX_VELOCITY;

  //Talons
  public static WPI_TalonSRX leftFrontTalon;
  public static WPI_TalonSRX rightFrontTalon;
  public static WPI_TalonSRX leftBackTalon;
  public static WPI_TalonSRX rightBackTalon;
  public static WPI_TalonSRX centralTalon;

  public static AHRS navX;

  public static Joystick leftDriverJoystick;
  public static Joystick rightDriverJoystick;

  //Controllers
  public static XboxController driverController;
  public static XboxController assistantDriverController;

  //Subsystems
  public static DriveTrainSubsystem driveTrainSubsystem;


  public static SerialPort arduino;

  public static void init()
  {
    arduino = new SerialPort(115200, SerialPort.Port.kUSB);

    driveTrainSubsystem = new DriveTrainSubsystem();
    driverController = new XboxController(DRIVER_CONTROLLER_PORT);
		assistantDriverController = new XboxController(ASSISTANT_DRIVER_CONTROLLER_PORT);

    leftFrontTalon = new WPI_TalonSRX(LEFT_FRONT_TALON_ID);
    rightFrontTalon = new WPI_TalonSRX(RIGHT_FRONT_TALON_ID);
    leftBackTalon = new WPI_TalonSRX(LEFT_BACK_TALON_ID);
    rightBackTalon = new WPI_TalonSRX(RIGHT_BACK_TALON_ID);
    centralTalon = new WPI_TalonSRX(CENTER_TALON_ID);

    leftDriverJoystick = new Joystick(LEFT_DRIVER_JOYSTICK_PORT);
    rightDriverJoystick = new Joystick(RIGHT_DRIVER_JOYSTICK_PORT);

    navX = new AHRS(SPI.Port.kMXP);

    leftFrontTalon.setInverted(true);
    leftBackTalon.setInverted(true);
    centralTalon.setInverted(true);

    leftBackTalon.follow(leftFrontTalon);
    rightBackTalon.follow(rightFrontTalon);
  }
}
