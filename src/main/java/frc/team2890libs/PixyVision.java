/*
 * Copyright (c) 2019 Titan Robotics Club (http://www.titanrobotics.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

 package frc.team2890libs;

 import java.util.ArrayList;

import org.opencv.core.Rect;

import edu.wpi.first.wpilibj.SPI;
import frc.robot.*;
import frc.team2890libs.frclib.*;
import frc.team2890libs.hawklib.*;
import frc.team2890libs.hawklib.HawkPixyCam.ObjectBlock;

 public class PixyVision
 {
    private static final double LAST_TARGET_RECT_FRESH_DURATION_SECONDS = 0.1;

    //Possibly unnecessary
    public class TargetInfo
    {
        public Rect rect;
        public double xDistance;
        public double yDistance;
        public double angle;

        public TargetInfo(Rect rect, double xDistance, double yDistance, double angle)
        {
            this.rect = rect;
            this.xDistance = xDistance;
            this.yDistance = yDistance;
            this.angle = angle;
        }

        public String toString()
        {
            return String.format("Rect[%d,%d,%d,%d], xDistance=%.1f, yDistance=%.1f, angle=%.1f",
                rect.x, rect.y, rect.width, rect.height, xDistance, yDistance, angle);
        }
    }

    public enum Orientation
    {
        NORMAL_LANDSCAPE,
        CLOCKWISE_PORTRAIT,
        ANTICLOCKWISE_PORTRAIT,
        UPSIDEDOWN_LANDSCAPE
    }

    private static final double PIXY_DISTANCE_SCALE = 2300.0;   //DistanceInInches*targetWidthdInPixels
    private static final double TARGET_WIDTH_INCHES = 13.0 * Math.sqrt(2.0);// 13x13 square, diagonal is 13*sqrt(2) inches

    private FRCPixyCam pixyCamera; //The actual camera
    private Orientation orientation; //The orientation of the pixy
    private Rect lastTargetRect; //The last target
    private int signature; //The signature of the target
    private double lastTargetRectExpireTime; //Something

    //Instantiates the instance data for the different types of constructors we have
    private void commonInit(int signature, int brightness, Orientation orientation)
    {
        this.lastTargetRect = null;
        this.orientation = orientation;
        this.signature = signature;
        this.lastTargetRectExpireTime = HawkUtil.getCurrentTime();
        pixyCamera.setBrightness((byte)brightness);
    }

    public PixyVision(final String instanceName, int signature, int brightness, Orientation orientation, SPI.Port port)
    {
        pixyCamera = new FRCPixyCam(instanceName, port);
        commonInit(signature, brightness, orientation);
    }

    public void setEnabled(boolean enabled)
    {
        pixyCamera.setEnabled(enabled);
    }   

    public boolean isEnabled()
    {
        return pixyCamera.isEnabled();
    }   

    /**
     * This method gets the rectangle of the last detected target from the camera. If the camera does not have
     * any. It may mean the camera is still busy analyzing a frame or it can't find any valid target in a frame.
     * We can't tell the reason. If the camera is likely busying processing a frame, we will return the last
     * cached rectangle. Therefore, the last cached rectangle will have an expiration (i.e. cached data can be
     * stale). If the last cached data becomes stale, we will discard it and return nothing. Otherwise, we will
     * return the cached data. Of course we will return fresh data if the camera does return another rectangle,
     * in which case it will become the new cached data.
     *
     * @return rectangle of the detected target last received from the camera or last cached target if cached
     *         data has not expired. Null if no object was seen and last cached data has expired.
     */
    private Rect getTargetRect()
    {
        Rect targetRect = null;
        ObjectBlock[] detectedObjects = pixyCamera.getDetectedObjects();
        double currTime = HawkUtil.getCurrentTime();

        if (detectedObjects != null && detectedObjects.length >= 1)
        {
            //
            // Make sure the camera detected at least one objects.
            //
            ArrayList<Rect> objectList = new ArrayList<>();
            //
            // Filter out objects that don't have the correct signature.
            //
            for (int i = 0; i < detectedObjects.length; i++)
            {
                if (signature == detectedObjects[i].signature)
                {
                    int temp;
                    //
                    // If we have the camera mounted in other orientations, we need to adjust the object rectangles
                    // accordingly.
                    //
                    switch (orientation)
                    {
                        case CLOCKWISE_PORTRAIT:
                            temp = RobotMap.PIXYCAM_WIDTH - detectedObjects[i].centerX;
                            detectedObjects[i].centerX = detectedObjects[i].centerY;
                            detectedObjects[i].centerY = temp;
                            temp = detectedObjects[i].width;
                            detectedObjects[i].width = detectedObjects[i].height;
                            detectedObjects[i].height = temp;
                            break;

                        case ANTICLOCKWISE_PORTRAIT:
                            temp = detectedObjects[i].centerX;
                            detectedObjects[i].centerX = RobotMap.PIXYCAM_HEIGHT - detectedObjects[i].centerY;
                            detectedObjects[i].centerY = temp;
                            temp = detectedObjects[i].width;
                            detectedObjects[i].width = detectedObjects[i].height;
                            detectedObjects[i].height = temp;
                            break;

                        case UPSIDEDOWN_LANDSCAPE:
                            detectedObjects[i].centerX = RobotMap.PIXYCAM_WIDTH - detectedObjects[i].centerX;
                            detectedObjects[i].centerY = RobotMap.PIXYCAM_HEIGHT - detectedObjects[i].centerY;
                            break;

                        case NORMAL_LANDSCAPE:
                            break;
                    }

                    Rect rect = new Rect(detectedObjects[i].centerX - detectedObjects[i].width/2,
                                         detectedObjects[i].centerY - detectedObjects[i].height/2,
                                         detectedObjects[i].width, detectedObjects[i].height);
                    objectList.add(rect);
                }
            }

            if (objectList.size() >= 1)
            {
                //
                // Find the largest target rect in the list.
                //
                Rect maxRect = objectList.get(0);
                for(Rect rect: objectList)
                {
                    double area = rect.width * rect.height;
                    if (area > maxRect.width * maxRect.height)
                    {
                        maxRect = rect;
                    }
                }

                targetRect = maxRect;
            }

            lastTargetRect = targetRect;
            lastTargetRectExpireTime = currTime + LAST_TARGET_RECT_FRESH_DURATION_SECONDS;
        }
        else if (currTime < lastTargetRectExpireTime)
        {
            targetRect = lastTargetRect;
        }

        return targetRect;
    }

    public TargetInfo getTargetInfo()
    {
        TargetInfo targetInfo = null;
        Rect targetRect = getTargetRect();

        if (targetRect != null)
        {
            //
            // Physical target width:           W = 10 inches.
            // Physical target distance 1:      D1 = 20 inches.
            // Target pixel width at 20 inches: w1 = 115
            // Physical target distance 2:      D2 = 24 inches
            // Target pixel width at 24 inches: w2 = 96
            // Camera lens focal length:        f
            //    W/D1 = w1/f and W/D2 = w2/f
            // => f = w1*D1/W and f = w2*D2/W
            // => w1*D1/W = w2*D2/W
            // => w1*D1 = w2*D2 = PIXY_DISTANCE_SCALE = 2300
            //
            // Screen center X:                 Xs = 320/2 = 160
            // Target center X:                 Xt
            // Heading error:                   e = Xt - Xs
            // Turn angle:                      a
            //    tan(a) = e/f
            // => a = atan(e/f) and f = w1*D1/W
            // => a = atan((e*W)/(w1*D1))
            //
            double targetCenterX = targetRect.x + targetRect.width/2.0;
            double targetXDistance = (targetCenterX - RobotMap.PIXYCAM_WIDTH/2.0)*TARGET_WIDTH_INCHES/targetRect.width;
            double targetYDistance = PIXY_DISTANCE_SCALE/targetRect.width;
            double targetAngle = Math.toDegrees(Math.atan(targetXDistance/targetYDistance));
            targetInfo = new TargetInfo(targetRect, targetXDistance, targetYDistance, targetAngle);
        }

        return targetInfo;
    }
 }