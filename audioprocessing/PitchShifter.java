import be.tarsos.dsp.util.fft.FFT;

/**
 * This is a translation of code by Stephan M. Bernsee. See the following explanation on this code:
 * <a href="http://www.dspdimension.com/admin/pitch-shifting-using-the-ft/">Pitch shifting using the STFT</a>.
 * 
 * @author Joren Six
 * @author Stephan M. Bernsee
 */

/**
 * Note: This is a modify version from Joren Six and Stephan M. Bernsee's original PitchShifter class by Chen
 */
public class PitchShifter {

	private final FFT fft;
	private final int size;
	 final float[] currentMagnitudes;
	 final float[] currentPhase;
	 final float[] currentFrequencies;
	 final float[] outputAccumulator;
	 final float[] summedPhase;
	
	  float[] previousPhase;
	
	 double pitchShiftRatio = 0;

	 final double sampleRate;
	
	 long osamp;
	
	 double excpt;
	
	public PitchShifter(double factor, double sampleRate, int size, int overlap){

		
		pitchShiftRatio = factor;
		this.size = size;
		this.sampleRate = sampleRate;
		//this.d = d;
		
		osamp=size/(size-overlap);
		
		this.excpt = 2.*Math.PI*(double)(size-overlap)/(double)size;
		
		fft = new FFT(size);
		
		currentMagnitudes = new float[size/2];
		currentFrequencies = new float[size/2];
		currentPhase = new float[size/2];
		
		previousPhase = new float[size/2];
		summedPhase = new float[size/2];
		outputAccumulator = new float[size*2];
	}
	
	public void setPitchShiftFactor(float newPitchShiftFactor){
		this.pitchShiftRatio = newPitchShiftFactor;
	}

	public  float[] process(float[] frameFloat){
		for (int i = 0; i < size; i++) {
			float window = (float) (-.5 * Math.cos(2. * Math.PI * (double) i / (double) size) + .5);
			frameFloat[i] = window * frameFloat[i];
		}
		//进行傅里叶变换
		fft.forwardTransform(frameFloat);
		//计算幅度和相位信息
		fft.powerAndPhaseFromFFT(frameFloat, currentMagnitudes, currentPhase);

		float freqPerBin = (float) (sampleRate / (float) size);    // distance in Hz between FFT bins

		for (int i = 0; i < size / 2; i++) {

			float phase = currentPhase[i];

			/* 计算相位差 */
			double tmp = phase - previousPhase[i];
			previousPhase[i] = phase;

			/* 减去预期的相位差 */
			tmp -= (double) i * excpt;

			/* 将δ相映射到+/- Pi区间 */
			long qpd = (long) (tmp / Math.PI);
			if (qpd >= 0)
				qpd += qpd & 1;
			else
				qpd -= qpd & 1;
			tmp -= Math.PI * (double) qpd;

			/* +/- Pi区间的bin频率偏差*/
			tmp = osamp * tmp / (2. * Math.PI);

			/* 计算第k个部分的真实频率 */
			tmp = (double) i * freqPerBin + tmp * freqPerBin;

			/* 在分析数组中存储幅度和真实频率 */
			currentFrequencies[i] = (float) tmp;
		}

		/* ***************** PROCESSING ******************* */
		/* 该部分真正进行音高变换操作 */
		float[] newMagnitudes = new float[size / 2];
		float[] newFrequencies = new float[size / 2];

		for (int i = 0; i < size / 2; i++) {
			int index = (int) (i * pitchShiftRatio);
			if (index < size / 2) {
				newMagnitudes[index] += currentMagnitudes[i];
				newFrequencies[index] = (float) (currentFrequencies[i] * pitchShiftRatio);
			}
		}

		///合成****
		float[] newFFTData = new float[size];

		for (int i = 0; i < size / 2; i++) {

			float magn = newMagnitudes[i];
			double tmp = newFrequencies[i];

			/* 提取该帧的中间频率 */
			tmp -= (double) i * freqPerBin;

			/* 从频率偏差得到bin偏差 */
			tmp /= freqPerBin;

			/* 考虑 osamp  */
			tmp = 2. * Math.PI * tmp / osamp;

			/* 重新加入重叠阶段 */
			tmp += (double) i * excpt;

			/* 为了得到bin相，计算delta相 */
			summedPhase[i] += tmp;
			float phase = summedPhase[i];

			/* 得到实部和虚部，并进行再交错 */
			newFFTData[2 * i] = (float) (magn * Math.cos(phase));
			newFFTData[2 * i + 1] = (float) (magn * Math.sin(phase));
		}

		/*将负频率置零 */
		for (int i = size / 2 + 2; i < size; i++) {
			newFFTData[i] = 0.f;
		}
		//傅里叶逆变换
		fft.backwardsTransform(newFFTData);
		for (int i = 0; i < newFFTData.length; i++) {
			float window = (float) (-.5 * Math.cos(2. * Math.PI * (double) i / (double) size) + .5);
			//outputAccumulator[i] += 2000*window*newFFTData[i]/(float) (size*osamp);
			outputAccumulator[i] += window * newFFTData[i] / (float) osamp;
			if (outputAccumulator[i] > 1.0 || outputAccumulator[i] < -1.0) {
				System.err.println("Clipping!");
			}
		}
		int stepSize = (int) (size / osamp);

		//数据窗口滑动stepSize
		System.arraycopy(outputAccumulator, stepSize, outputAccumulator, 0, size);
		float[] audioBuffer=new float[size];
		System.arraycopy(outputAccumulator, 0, audioBuffer, size - stepSize, stepSize);
		return audioBuffer;

	}


}
