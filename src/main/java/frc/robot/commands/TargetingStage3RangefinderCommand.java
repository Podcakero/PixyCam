/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import edu.wpi.first.wpilibj.command.Command;
import frc.robot.RobotMap;

public class TargetingStage3RangefinderCommand extends Command 
{
  private String data;
  private String newData;

  public TargetingStage3RangefinderCommand() 
  {
    // Use requires() here to declare subsystem dependencies
    // eg. requires(chassis);
    requires(RobotMap.driveTrainSubsystem);
    
    data = "";
    newData = "";
  }

  // Called just before this Command runs the first time
  @Override
  protected void initialize() 
  {
    newData = RobotMap.arduino.readString();
  }

  // Called repeatedly when this Command is scheduled to run
  @Override
  protected void execute() 
  {
    if (!data.equals(newData))
    {
      RobotMap.driveTrainSubsystem.arcadeDrive(Double.parseDouble(RobotMap.arduino.readString()) / RobotMap.DRIVETRAIN_RANGEFINDER_TARGETING_SPEED_MODIFIER, 0, 0);
      data = newData;
    }
  }

  // Make this return true when this Command no longer needs to run execute()
  @Override
  protected boolean isFinished() 
  {
    newData = RobotMap.arduino.readString();
    return newData.equals("done");
  }

  // Called once after isFinished returns true
  @Override
  protected void end() 
  {
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  @Override
  protected void interrupted() 
  {
  }
}