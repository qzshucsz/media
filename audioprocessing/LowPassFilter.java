public class LowPassFilter {
    byte[] inputBytes;
    PitchProcessor LowPassFilter;
    public LowPassFilter(byte[] buffer){
        this.inputBytes=buffer;
         LowPassFilter=new PitchProcessor(inputBytes);
         LowPassFilter.pitchShifter.setPitchShiftFactor(1.0f);
    }
    public void updateByteBuffer(byte[] updateBuffer){
        this.inputBytes=updateBuffer;
        LowPassFilter.updateByteBuffer(inputBytes);
    }
    byte[] process(){
        return LowPassFilter.process();
    }
}
