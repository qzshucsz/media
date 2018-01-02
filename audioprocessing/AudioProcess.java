package android.media;
import be.tarsos.dsp.util.fft.FFT;
import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;

import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.SystemApi;
import android.app.ActivityThread;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.ArrayMap;
import android.util.Log;

import com.android.internal.annotations.GuardedBy;


public class AudioProcess {

    private final static String TAG = "android.media.audioprocessing.AudioProcess";
    //first frame has not overlap. It should be processed indepently
    boolean isFirstFrame;
    int firstFrameSize=2048;
    int frameByteSize=64;
    byte[] firstFrame=new byte[firstFrameSize];
    LowPassFilter lowPassFilter=null;
    public AudioProcess(){
        this.isFirstFrame=true;
    }
    public void setFirstFrame(boolean isFirstFrame){
        this.isFirstFrame=isFirstFrame;
    }

    /**
     *This function do low pass filter procession to original audio data
     * @param tempData, original data.
     * @param sizeInBytes, size of bytes data that to be processed
     * @return tempData, tempData is byte[] after processed
     * @throws IOException
     */
    public byte[] filterProcess(@NonNull byte[] tempData, int sizeInBytes) throws IOException {
        if(isFirstFrame) {
            if(tempData.length==sizeInBytes)
                Log.d(TAG, "-----------byte[] is equal to sizeInBytes-----------");
            Log.d(TAG, "-----------Start the first frame bytes processing-----------");
            System.arraycopy(audioData, 0, firstFrame, 0, firstFrameSize);
            Log.d(TAG, "-----------Enter PROCESS success when reading in bytes-----------");
            lowPassFilter= new LowPassFilter(firstFrame);
            Log.d(TAG, "-----------Create LowPassFilter successfully!-----------");
            System.arraycopy(lowPassFilter.process(),0,tempData,0,firstFrameSize);
            byte[] frameBytes=new byte[frameByteSize];
            int currentPos=0;
            while(currentPos<((sizeInBytes-firstFrameSize)/frameByteSize)){
                System.arraycopy(tempData,firstFrameSize+currentPos*frameByteSize,frameBytes,0,frameByteSize);
                lowPassFilter.updateByteBuffer(frameBytes);
                Log.d(TAG, "-----------update LowPassFilter buffer successfully!-----------");
                System.arraycopy(lowPassFilter.process(),0,tempData,firstFrameSize+currentPos*frameByteSize,frameByteSize);
                currentPos++;
            }
            setFirstFrame(false);
            Log.d(TAG, "-----------set FirstFrame symbol false successfully!-----------");
        }else{
            byte[] frameBytes=new byte[frameByteSize];
            int currentPos=0;
            while(currentPos<(sizeInBytes/frameByteSize)){
                System.arraycopy(tempData,currentPos*frameByteSize,frameBytes,0,frameByteSize);
                lowPassFilter.updateByteBuffer(frameBytes);
                System.arraycopy(lowPassFilter.process(),0,tempData,currentPos*frameByteSize,frameByteSize);
                currentPos++;
            }
        }
        Log.d(TAG, "-----------LowPassFilter process success when reading in bytes-----------");
        return tempData;
    }

    /**
     *This function do pitch shift procession to original audio data
     * @param tempData, original data.
     * @param sizeInBytes, size of bytes data that to be processed
     * @return tempData, tempData is byte[] after processed
     * @throws IOException
     */
    public byte[] pitchProcess(@NonNull byte[] tempData, int sizeInBytes)throws IOException {
        if (isFirstFrame) {
            if (tempData.length == sizeInBytes)
                Log.d(TAG, "-----------byte[] is equal to sizeInBytes-----------");
            Log.d(TAG, "-----------Start the first frame bytes processing-----------");
            //read the firstFrameSize bytes data from audioData
            System.arraycopy(audioData, 0, firstFrame, 0, firstFrameSize);
            Log.d(TAG, "-----------Enter PROCESS success when reading in bytes-----------");
            PitchProcessor pitchProcessor = new PitchProcessor(firstFrame);
            Log.d(TAG, "-----------Create PitchProcessor successfully!-----------");
            System.arraycopy(pitchProcessor.process(), 0, tempData, 0, firstFrameSize);
            byte[] frameBytes = new byte[frameByteSize];
            int currentPos = 0;
            while (currentPos < ((sizeInBytes - firstFrameSize) / frameByteSize)) {
                System.arraycopy(tempData, firstFrameSize + currentPos * frameByteSize, frameBytes, 0, frameByteSize);
                pitchProcessor.updateByteBuffer(frameBytes);
                Log.d(TAG, "-----------update PitchProcessor buffer successfully!-----------");
                System.arraycopy(pitchProcessor.process(), 0, tempData, firstFrameSize + currentPos * frameByteSize, frameByteSize);
                currentPos++;
            }
            setFirstFrame(false);
            Log.d(TAG, "-----------set FirstFrame symbol false successfully!-----------");
        } else {
            byte[] frameBytes = new byte[frameByteSize];
            int currentPos = 0;
            while (currentPos < (sizeInBytes / frameByteSize)) {
                System.arraycopy(tempData, currentPos * frameByteSize, frameBytes, 0, frameByteSize);
                pitchProcessor.updateByteBuffer(frameBytes);
                System.arraycopy(pitchProcessor.process(), 0, tempData, currentPos * frameByteSize, frameByteSize);
                currentPos++;
            }
        }
        Log.d(TAG, "-----------pitchShift process successfully when reading in bytes-----------");
        return tempData;
    }

    public short[] process(@NonNull short[] tempData, int sizeInShorts) throws IOException {
        Log.d(TAG, "-----------Enter PROCESS success when reading in shorts-----------");
        return tempData;
    }

    public float[] process(@NonNull float[] tempData, int sizeInFloats) throws IOException {
        Log.d(TAG, "-----------Enter PROCESS success when reading in floats-----------");
        return tempData;
    }

    public ByteBuffer process(@NonNull ByteBuffer tempData, int sizeInBytes) throws IOException {
        Log.d(TAG, "-----------Enter PROCESS success when reading in direct buffer-----------");
        return tempData;
    }

}