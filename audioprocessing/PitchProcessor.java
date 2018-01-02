public class PitchProcessor {
    /*
   @param factor,根据试验在0.8——1.2范围内较为合适,
   @param sampleRate,音频源的采样频率
   @param size,处理窗口大小
   @param overlap,重叠大小，即两个相邻的窗口重叠至少75%
    */
    byte[] inputByteBuffer;
    boolean isFirstFrame=true;
    float[] temp=new float[1024];

    //定义变换系数
    final double factor=0.8;
    final float sampleRate = 44100;
    final int size=1024;
    final int overlap=1024-32;
    final int moveLens=32;
    //输入音频流大小
    byte[] byteBuffer=new byte[size*2];
    float[] floatBuffer=new float[1024];
    float[] moveFloat;
    PitchShifter pitchShifter=new PitchShifter(factor,sampleRate,size,overlap);
    Converter converter=new Converter();
    /*Constructor*/
    public PitchProcessor(byte[] buffer){
        this.inputByteBuffer=buffer;
    }
    /*An update operation in order to process coming audio data */
    public void updateByteBuffer(byte[] buffer){
        this.inputByteBuffer=buffer;
    }
    public byte[] process(){

        /**
         * 记得初始化，把buffer数据替换进来,这里buffer给的大小是2048字节
         */
        /*音高转变，使用TarsosDSP库中的PitchShifter类，构造参数如下*/
        //第一次处理的数据重叠窗口大小为0，需要读取大小为size*2的byte数据
        if(isFirstFrame) {
            byteBuffer=inputByteBuffer.clone();
            //转为float数组
            floatBuffer= converter.toFloatArray(byteBuffer,0,floatBuffer,0,1024);
            //保留第一个窗口的数据
            temp=floatBuffer.clone();
            //将判断是否是第一个窗口的标志位置为false
            isFirstFrame=false;
            return  converter.toByteArray(pitchShifter.process(floatBuffer),0,size,byteBuffer,0);
        }else{
            //非首个窗口数据，窗口重叠，所以每次只需要读取readBytes（这里为64）字节数据
            moveFloat=new float[moveLens];
            moveFloat=converter.toFloatArray(inputByteBuffer,0,moveFloat,0,moveLens);
            floatBuffer=temp.clone();
            //每次都要移动数据窗口，以便读取新的数据，移动距离为size-moveLens
            System.arraycopy(floatBuffer,moveLens,floatBuffer,0,size-moveLens);
            //除去重叠窗口数据，还需要读取长度为moveLens（这里设为32）的数据
            System.arraycopy(moveFloat,0,floatBuffer,size-moveLens,moveLens);
            temp=floatBuffer.clone();
            byte[] resultByte=new byte[moveLens*2];
            System.arraycopy(converter.toByteArray(pitchShifter.process(floatBuffer),size-moveLens,moveLens,byteBuffer,(size-moveLens)*2),(size-moveLens)*2,resultByte,0,moveLens*2);
            return resultByte;
        }

    }


}
