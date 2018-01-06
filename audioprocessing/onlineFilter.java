package android.media;
public class onlineFilter{
    float Fs = 44100;
    float F0 = 8000;
    float Q = 0.707f;
    //中间值
    float w0;
    float alpha;
    //滤波器系数计算
    float a0 ;
    float b0 ;
    float b1 ;
    float b2 ;
    float a1 ;
    float a2 ;
    public onlineFilter(int rate){
        this.Fs=rate;
        w0= (float)(2*Math.PI*F0/Fs);
        alpha =(float)Math.sin(w0)/(2*Q);
        a0 = 1 + alpha;
        b0 = (1-(float)Math.cos(w0))/2/a0;
        b1 = (1-(float)Math.cos(w0))/a0;
        b2 = (1-(float)Math.cos(w0))/2/a0;
        a1 = -2*(float)Math.cos(w0)/a0;
        a2 =  (1 - alpha)/a0;
    }
    //初始化
    float xmem1, xmem2, ymem1, ymem2;
    public void reset(){
        xmem1 = xmem2 = ymem1 = ymem2 = 0;

    }
    //滤波计算
    public float process(float x ) {
        float y = b0*x + b1*xmem1 + b2*xmem2 - a1*ymem1 - a2*ymem2;
        xmem2 = xmem1;
        xmem1 = x;
        ymem2 = ymem1;
        ymem1 = y;
        return y;
    }

    /*处理读取方式为byte[]和ByteBuffer的情况*/
    public byte[] process(byte[] data){
        if(Fs<2*F0)//转角频率要小于半采样频率才有意义，否则直接输出原始数据
            return data;
        else{
        float[] floatBuffer=new float[data.length/2];
        toFloatArray(data,0,floatBuffer,0,floatBuffer.length);
        for(int i=0;i<floatBuffer.length;i++)
            floatBuffer[i]=process(floatBuffer[i]);
        return toByteArray(floatBuffer,0,floatBuffer.length,data,0);
    }

    }

    /*处理读取方式为short[]的情况*/
    public short[] process(short[] data){
        if(Fs<2*F0)//转角频率要小于半采样频率才有意义，否则直接输出原始数据
            return data;
        else{
            byte[] byteBuffer=new byte[data.length*2];
            byteBuffer=shortArrayToByteArray(data);
            byteBuffer=process(byteBuffer);
            data=byteArrayToShortArray(byteBuffer);
            return data;
        }
    }

    /*处理读取方式为float[]的情况*/
    public float[] process(float[] data){
        if(Fs<2*F0)//转角频率要小于半采样频率才有意义，否则直接输出原始数据
            return data;
        else{
            for(int i=0;i<data.length;i++)
                data[i]=process(data[i]);
            return data;
        }

    }


     /*各种数据转换*/
    /**
     * [toFloatArray description]
     * @param  in_buff    转换数据源byte[]
     * @param  in_offset  转换数据源初始读取位置
     * @param  out_buff   转换后的数据float[]
     * @param  out_offset 转换后数组初始写入位置
     * @param  out_len    数据写入长度
     * @return            转换后的数据float[]
     */
    /*byte[] 转float[]*/
    public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
        int ix = in_offset;
        int len = out_offset + out_len;
        for (int ox = out_offset; ox < len; ox++) {
            out_buff[ox] = ((short) ((in_buff[ix++] & 0xFF) |
                    (in_buff[ix++] << 8))) * (1.0f / 32767.0f);
        }
        return out_buff;
    }


    /*float[]转byte[]*/
    public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
        int ox = out_offset;
        int len = in_offset + in_len;
        for (int ix = in_offset; ix < len; ix++) {
            int x = (int) (in_buff[ix] * 32767.0);
            out_buff[ox++] = (byte) x;
            out_buff[ox++] = (byte) (x >>> 8);
        }
        return out_buff;
    }

    /*the convertion between byte arrray and short array*/
     public short[] byteArrayToShortArray(byte[] in_buff) {

        int shortLength = in_buff.length >> 1;
        short[] dest = new short[shortLength];
        for (int i = 0; i < shortLength; i++) {
            dest[i] = (short) (in_buff[i * 2] << 8 | in_buff[2 * i + 1] & 0xff);
        }
        return dest;
    }

    public byte[] shortArrayToByteArray(short[] in_buff) {

        int byteLength = in_buff.length<<1;
        byte[] dest = new byte[byteLength];
        for (int i = 0; i < in_buff.length; i++) {
            dest[i * 2] = (byte) (in_buff[i] >> 8);
            dest[i * 2 + 1] = (byte) (in_buff[i] >> 0);
        }

        return dest;
    }
}

