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

package frc.team2890libs.hawklib;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Add your docs here.
 */
public abstract class HawkSerialBusDevice 
{

    /**
     * This method is called to read data from the device synchronously with the specified length.
     *
     * @param address specifies the data address if any, can be -1 if no address is required.
     * @param length specifies the number of bytes to read.
     * @return a byte array containing the data read.
     */
    public abstract byte[] readData(int address, int length);

    /**
     * This method is called to write data to the device synchronously with the specified data buffer and length.
     *
     * @param address specifies the data address if any, can be -1 if no address is required.
     * @param buffer specifies the buffer containing the data to be written to the device.
     * @param length specifies the number of bytes to write.
     * @return number of bytes written.
     */
    public abstract int writeData(int address, byte[] buffer, int length);
    
    /**
     * This class implements a request. Typically, a request will be put into a FIFO request queue so that each
     * request will be processed in the order they came in.
     */
    public class Request
    {
        public Object requestCtxt;
        public boolean readRequest;
        public int address;
        public byte[] buffer;
        public int length;
        public boolean repeat;
        public HawkEvent event;
        public HawkNotifier.Receiver handler;
        public boolean error;
        public boolean canceled;

        /**
         * Constructor: Create an instance of the object.
         *
         * @param requestCtxt specifies the request context and is not interpreted by the HawkSerialBusDevice class.
         *                    it is just passed back to the requester's notification handler.
         * @param readRequest specifies true for a read request, false for a write request.
         * @param address specifies the data address if any, can be -1 if no address is required.
         * @param buffer specifies the buffer that contains data for a write request, ignored for read request.
         * @param length specifies the number of bytes to read or write.
         * @param repeat specifies true to re-queue the request when completed.
         * @param event specifies the event to signal when the request is completed, can be null if none specified.
         * @param handler specifies the notification handler to call when the request is completed, can be null if
         *                none specified.
         */
        public Request(Object requestCtxt, boolean readRequest, int address, byte[] buffer, int length, boolean repeat, HawkEvent event, HawkNotifier.Receiver handler)
        {
            this.requestCtxt = requestCtxt;
            this.readRequest = readRequest;
            this.address = address;
            this.buffer = buffer;
            this.length = length;
            this.repeat = repeat;
            this.event = event;
            this.handler = handler;
            this.error = false;
            this.canceled = false;
        }   

        /**
         * This method returns the request info as a string.
         *
         * @return request info string.
         */
        public String toString()
        {
            return String.format(Locale.US, "%s: %s, addr=%d, buff=%s, len=%d, repeat=%s, event=%s, err=%s, canceled=%s",
                requestCtxt != null? requestCtxt: "null", readRequest? "Read": "Write", address,
                buffer == null? "null": Arrays.toString(buffer), length, repeat, event, error, canceled);
        }   

    }

    private final String instanceName;
    private final LinkedBlockingQueue<Request> requestQueue;
    private volatile Thread deviceThread = null;
    private boolean enabled = false;
    private Request preemptingRequest = null;
    private double totalNanoTime = 0.0;
    private int totalRequests = 0;

    /**
     * Constructor: Creates an instance of the object.
     *
     * @param instanceName specifies the instance name.
     */
    public HawkSerialBusDevice(String instanceName)
    {
        this.instanceName = instanceName;
        requestQueue = new LinkedBlockingQueue<>();
    }   

    /**
     * This method returns the instance name.
     *
     * @return instance name.
     */
    public String toString()
    {
        return instanceName;
    }   

    /**
     * This method enables/disables the serial bus device thread.
     *
     * @param enabled specifies true to enable device thread, false to disable.
     */
    public synchronized void setEnabled(boolean enabled)
    {
        if (deviceThread == null && enabled)
        {
            //
            // Device thread was not enabled, somebody wants to enable it.
            //
            deviceThread = new Thread(this::deviceTask, instanceName);
            deviceThread.start();
            this.enabled = true;
        }
        else if (deviceThread != null && !enabled)
        {
            //
            // Device thread was enabled, somebody wants to disable it.
            //
            if (this.enabled)
            {
                //
                // Make sure the device thread is indeed enabled. The request queue may not be empty. So we need to
                // signal termination but allow the device thread to orderly shut down.
                // If device thread is already disabled and the deviceThread is still active, it means the thread is
                // busy emptying its queue. So we don't need to double signal termination.
                //
                this.enabled = false;
                deviceThread.interrupt();
            }
        }
    } 

    /**
     * This method checks if the serial bus device is enabled.
     *
     * @return true if serial bus device is enabled, false if disabled.
     */
    public synchronized boolean isEnabled()
    {
        return enabled;
    }  

    /**
     * This method is doing a synchronous read from the device with the specified length to read.
     *
     * @param address specifies the data address if any, can be -1 if no address is required.
     * @param length specifies the number of bytes to read.
     * @return data read as an array of bytes.
     */
    public byte[] syncRead(int address, int length)
    {
        final String funcName = "syncRead";
        byte[] data = null;

        if (!isEnabled())
            throw new RuntimeException("Device is not enabled, must call setEnabled first.");

        HawkEvent event = new HawkEvent(instanceName + "." + funcName + "." + length);
        Request request = new Request(null, true, address, null, length, false, event, null);

        requestQueue.add(request);

        while (!event.isSignaled())
            Thread.yield();

        data = request.buffer;
        request.buffer = null;

        return data;
    }  

    /**
     * This method is doing a synchronous read from the device with the specified length to read.
     *
     * @param length specifies the number of bytes to read.
     * @return data read as an array of bytes.
     */
    public byte[] syncRead(int length)
    {
        return syncRead(-1, length);
    } 

    /**
     * This method is doing a synchronous write to the device with the specified data and length.
     *
     * @param address specifies the data address if any, can be -1 if no address is required.
     * @param data specifies the data to write to the device.
     * @param length specifies the number of bytes to write.
     * @return number of bytes written.
     */
    public int syncWrite(int address, byte[] data, int length)
    {
        final String funcName = "syncWrite";
        int bytesWritten;

        if (!isEnabled())
            throw new RuntimeException("Must call setEnabled first.");

        HawkEvent event = new HawkEvent(instanceName + "." + funcName + "." + length);
        Request request = new Request(null, false, address, data, length, false, event, null);

        requestQueue.add(request);

        while (!event.isSignaled())
            Thread.yield();
        bytesWritten = request.length;

        return bytesWritten;
    } 

    /**
     * This method is doing a synchronous write to the device with the specified data and length.
     *
     * @param data specifies the data to write to the device.
     * @param length specifies the number of bytes to write.
     * @return number of bytes written.
     */
    public int syncWrite(byte[] data, int length)
    {
        return syncWrite(-1, data, length);
    } 

    /**
     * This method is doing an asynchronous read from the device with the specified length to read.
     *
     * @param requestCtxt specifies the request context and is not interpreted by the HawkSerialBusDevice class.
     *                    it is just passed back to the requester's notification handler.
     * @param address specifies the data address if any, can be -1 if no address is required.
     * @param length specifies the number of bytes to read.
     * @param repeat specifies true to re-queue the request when completed.
     * @param event specifies the event to signal when the request is completed, can be null if none specified.
     * @param handler specifies the notification handler to call when the request is completed, can be null if none
     *                specified.
     */
    public void asyncRead(Object requestCtxt, int address, int length, boolean repeat, HawkEvent event, HawkNotifier.Receiver handler)
    {
        requestQueue.add(new Request(requestCtxt, true, address, null, length, repeat, event, handler));
    } 

    /**
     * This method is doing an asynchronous read from the device with the specified length to read.
     *
     * @param requestCtxt specifies the request context and is not interpreted by the HawkSerialBusDevice class.
     *                    it is just passed back to the requester's notification handler.
     * @param address specifies the data address if any, can be -1 if no address is required.
     * @param length specifies the number of bytes to read.
     * @param event specifies the event to signal when the request is completed, can be null if none specified.
     * @param handler specifies the notification handler to call when the request is completed, can be null if none
     *                specified.
     */
    public void asyncRead(Object requestCtxt, int address, int length, HawkEvent event, HawkNotifier.Receiver handler)
    {
        asyncRead(requestCtxt, address, length, false, event, handler);
    }  

    /**
     * This method is doing an asynchronous read from the device with the specified length to read.
     *
     * @param requestCtxt specifies the request context and is not interpreted by the HawkSerialBusDevice class.
     *                    it is just passed back to the requester's notification handler.
     * @param length specifies the number of bytes to read.
     * @param repeat specifies true to re-queue the request when completed.
     * @param event specifies the event to signal when the request is completed, can be null if none specified.
     * @param handler specifies the notification handler to call when the request is completed, can be null if none
     *                specified.
     */
    public void asyncRead(Object requestCtxt, int length, boolean repeat, HawkEvent event, HawkNotifier.Receiver handler)
    {
        asyncRead(requestCtxt, -1, length, repeat, event, handler);
    } 

    /**
     * This method is doing an asynchronous read from the device with the specified length to read.
     *
     * @param requestCtxt specifies the request context and is not interpreted by the HawkSerialBusDevice class.
     *                    it is just passed back to the requester's notification handler.
     * @param length specifies the number of bytes to read.
     * @param event specifies the event to signal when the request is completed, can be null if none specified.
     * @param handler specifies the notification handler to call when the request is completed, can be null if none
     *                specified.
     */
    public void asyncRead(Object requestCtxt, int length, HawkEvent event, HawkNotifier.Receiver handler)
    {
        asyncRead(requestCtxt, -1, length, false, event, handler);
    } 

    /**
     * This method is doing an asynchronous write to the device with the specified data and length
     *
     * @param requestCtxt specifies the request context and is not interpreted by the HawkSerialBusDevice class.
     *                    it is just passed back to the requester's notification handler.
     * @param address specifies the data address if any, can be -1 if no address is required.
     * @param data specifies the buffer containing the data to write to the device.
     * @param length specifies the number of bytes to write.
     * @param event specifies the event to signal when the request is completed, can be null if none specified.
     * @param handler specifies the notification handler to call when the request is completed, can be null if none
     *                specified.
     */
    public void asyncWrite(
        Object requestCtxt, int address, byte[] data, int length, HawkEvent event, HawkNotifier.Receiver handler)
    {

        requestQueue.add(new Request(requestCtxt, false, address, data, length, false, event, handler));
    }

    /**
     * This method is doing an asynchronous write to the device with the specified data and length
     *
     * @param requestCtxt specifies the request context and is not interpreted by the HawkSerialBusDevice class.
     *                    it is just passed back to the requester's notification handler.
     * @param data specifies the buffer containing the data to write to the device.
     * @param length specifies the number of bytes to write.
     * @param event specifies the event to signal when the request is completed, can be null if none specified.
     * @param handler specifies the notification handler to call when the request is completed, can be null if none
     *                specified.
     */
    public void asyncWrite(Object requestCtxt, byte[] data, int length, HawkEvent event, HawkNotifier.Receiver handler)
    {
        asyncWrite(requestCtxt, -1, data, length, event, handler);
    } 

    /**
     * This method writes the data to the device preemptively bypassing the queue.
     *
     * @param address specifies the data address if any, can be -1 if no address is required.
     * @param data specifies the buffer containing the data to write to the device.
     * @param length specifies the number of bytes to write.
     */
    public synchronized void preemptiveWrite(int address, byte[] data, int length)
    {
        preemptingRequest = new Request(null, false, address, data, length, false, null, null);
    }

    /**
     * This method sends a byte command to the device.
     *
     * @param address specifies the data address if any, can be -1 if no address is required.
     * @param command specifies the command byte.
     * @param waitForCompletion specifies true to wait for write completion.
     */
    public void sendByteCommand(int address, byte command, boolean waitForCompletion)
    {
        byte[] data = new byte[1];

        data[0] = command;
        if (waitForCompletion)
            syncWrite(address, data, data.length);
        else
            //
            // Fire and forget.
            //
            asyncWrite(null, address, data, data.length, null, null);
    }

    /**
     * This method sends a 16-bit command to the device.
     *
     * @param address specifies the data address if any, can be -1 if no address is required.
     * @param command specifies the 16-bit command.
     * @param waitForCompletion specifies true to wait for write completion.
     */
    public void sendWordCommand(int address, short command, boolean waitForCompletion)
    {
        byte[] data = new byte[2];

        data[0] = (byte)(command & 0xff);
        data[1] = (byte)(command >> 8);
        if (waitForCompletion)
            syncWrite(address, data, data.length);
        else
            //
            // Fire and forget.
            //
            asyncWrite(null, address, data, data.length, null, null);
    } 

    /**
     * This method processes a request.
     *
     * @param request specifies the request to be processed.
     */
    private void processRequest(Request request)
    {
        long startNanoTime = HawkUtil.getCurrentTimeNanos();
        if (request.readRequest)
        {
            request.buffer = readData(request.address, request.length);
            request.error = request.buffer == null;
        }
        else
        {
            int length = writeData(request.address, request.buffer, request.length);
            request.error = length != request.length;
            request.length = length;
        }
        long elapsedTime = HawkUtil.getCurrentTimeNanos() - startNanoTime;
        totalNanoTime += elapsedTime;
        totalRequests++;

        if (request.event != null)
            request.event.set(true);

        if (request.handler != null)
            request.handler.notify(request);

        if (request.readRequest && request.repeat)
            //
            // This is a repeat request, add it back to the tail of the queue.
            //
            requestQueue.add(request);
    } 

    /**
     * This method cancels a request.
     *
     * @param request specifies the request to be canceled.
     */
    private void cancelRequest(Request request)
    {
        request.canceled = true;

        if (request.event != null)
            request.event.cancel();

        if (request.handler != null)
            request.handler.notify(request);
    } 

    /**
     * This method is called when the device thread is started. It processes all requests in the request queue when
     * they arrive. If the request queue is empty, the thread is blocked until a new request arrives. Therefore,
     * this thread only runs when there are requests in the queue. If this thread is interrupted, it will clean up
     * the request queue before exiting.
     */
    private void deviceTask()
    {
        Request request;

        while (!Thread.currentThread().isInterrupted())
        {
            synchronized (this)
            {
                if (preemptingRequest != null)
                {
                    request = preemptingRequest;
                    preemptingRequest = null;
                }
                else
                    request = null;
            }

            try
            {
                if (request == null)
                    request = requestQueue.take();

                processRequest(request);
            }
            catch (InterruptedException e)
            {
                break;
            }
        }
        //
        // The thread is terminating, empty the queue before exiting.
        //
        while ((request = requestQueue.poll()) != null)
            cancelRequest(request);

        deviceThread = null;
    }

    public double getTotalNanoTime()
    {
        return totalNanoTime;
    }

    public int getTotalRequests()
    {
        return totalRequests;
    }
}
