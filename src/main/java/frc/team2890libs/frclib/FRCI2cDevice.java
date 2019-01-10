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

package frc.team2890libs.frclib;

import java.nio.ByteBuffer;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.I2C.Port;
import frc.team2890libs.hawklib.HawkSerialBusDevice;

/**
 * Add your docs here.
 */
public class FRCI2cDevice extends HawkSerialBusDevice
{
    private I2C device;

    /**
     * Constructor: Creates an instance of the object.
     *
     * @param instanceName specifies the instance name.
     * @param port specifies the I2C port the device is connected to.
     * @param devAddress specifies the address of the device on the I2C bus.
     */
    public FRCI2cDevice(final String instanceName, Port port, int devAddress)
    {
        super(instanceName);
        device = new I2C(port, devAddress);
    }   

    /**
     * Constructor: Creates an instance of the object.
     *
     * @param instanceName specifies the instance name.
     * @param devAddress specifies the address of the device on the I2C bus.
     */
    public FRCI2cDevice(final String instanceName, int devAddress)
    {
        this(instanceName, Port.kOnboard, devAddress);
    }   

    //
    // Implements HawkSerialBusDevice abstract methods.
    //

    /**
     * This method is called to read data from the device with the specified length.
     *
     * @param address specifies the I2C register address to read from if any.
     * @param length specifies the number of bytes to read.
     * @return a byte array containing the data read.
     */
    @Override
    public byte[] readData(int address, int length)
    {
        byte[] buffer = new byte[length];

        if (address == -1 && device.readOnly(buffer, length) || address != -1 && device.read(address, length, buffer))
            buffer = null;

        return buffer;
    }   

    /**
     * This method is called to write data to the device with the specified data buffer and length.
     *
     * @param address specifies the I2C register address to write to if any.
     * @param buffer specifies the buffer containing the data to be written to the device.
     * @param length specifies the number of bytes to write.
     * @return number of bytes written.
     */
    @Override
    public int writeData(int address, byte[] buffer, int length)
    {
        int buffLen = address == -1? length: length + 1;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffLen);

        if (address != -1)
            byteBuffer.put((byte)address);
        byteBuffer.put(buffer);

        if (device.writeBulk(byteBuffer, length))
            length = 0;

        return length;
    }
}
