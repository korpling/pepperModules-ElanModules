package mpi.eudico.client.annotator.recognizer.impl;


import java.io.IOException;
import java.util.*;

import javax.swing.JPanel;

import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import mpi.eudico.client.util.WAVSampler;

/**
 * 
 * @author albertr
 *
 */
public class TestRecognizer implements Recognizer {
	private RecognizerHost host;
	private TestRecognizerPanel controlPanel;
	private String currentMediaFilePath;
	private WAVSampler sampler;
	private int nrOfChannels;
	private int sampleFrequency;
	private long nrOfSamples;
	private float duration;
	private float normalizationFactor;
	private boolean canHandleMedia;
	private long sampleBufferBeginTime;
	private int sampleBufferDuration;
	private boolean keepRunning;
	
	/**
	 * Lightweight constructor, try to do as little as possible here
	 *
	 */
	public TestRecognizer() {
		
	}

	/**
	 * Called by RecognizerHost to get a name for this recognizer in the ComboBox with available recognizers
	 * 
	 * @return the name of this recognizer
	 */
	public String getName() {
		return "Test Recognizer";
	}

	/**
	 * Called by RecognizerHost to get a control panel for this recognizers parameters
	 * 
	 * @return a JPanel with the recognizers GUI controls or null if there are no controls
	 */
	public JPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = new TestRecognizerPanel();
		}
		return controlPanel;
	}
	
	/**
	 * Called by RecognizerHost to set the media for this recognizer and to see if this recognizer can handle the media
	 * 
	 * @param mediaFilePath String that contains the full path to the media file
	 * @return true if this recognizer can handle the media, false otherwise
	 */
	public boolean setMedia(List<String> mediaFilePaths) {
		if (mediaFilePaths == null) {
			return false;
		}
		currentMediaFilePath = (String) mediaFilePaths.get(0); // only uses one file, the visible one
		canHandleMedia = false;
		
		try {
			sampler = new WAVSampler(currentMediaFilePath);
			nrOfChannels = sampler.getWavHeader().getNumberOfChannels();
			sampleFrequency = sampler.getSampleFrequency();
			nrOfSamples = sampler.getNrOfSamples();
			duration = sampler.getDuration();
			normalizationFactor = sampler.getPossibleMaxSample() + 1;
			sampleBufferBeginTime = -1;
			sampleBufferDuration = 0;
			canHandleMedia = true;
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		return canHandleMedia;
	}
	
	/**
	 * Called by RecognizerHost to give this recognizer an object for callbacks
	 * 
	 * @param host the RecognizerHost that talks with this recognizer
	 */
	public void setRecognizerHost(RecognizerHost host) {
		this.host = host;
	}
	
	/**
	 * Fills an array with normalized sample values for a certain time interval and a certain channel.
	 * This method was needed to optimize the WAVReaderthat is rather slow for small time steps
	 * 
	 * Pad with zeros if you read more samples than available in the media file
	 * 
	 * @param from interval begin time in milliseconds
	 * @param to interval end time in milliseconds
	 * @param channel the audio channel from which the samples must be read
	 * @param samples the int[] array that must be filled with the samples
	 */
	private void getSamples(long from, long to, int channel, float[] samples) {
		try {
			// check if the requested samples are in the buffer
			long sampleBufferEndTime = sampleBufferBeginTime + sampleBufferDuration;
			if (from < sampleBufferBeginTime || from >= sampleBufferEndTime || 
			                                    to < sampleBufferBeginTime || to >= sampleBufferEndTime) {
				sampleBufferDuration = 10000;
				while (to - from > sampleBufferDuration) {
					sampleBufferDuration += 1000;
				}
				int nSamples = (sampleBufferDuration * sampleFrequency) / 1000;
				sampleBufferBeginTime = from;
				sampler.seekTime(sampleBufferBeginTime);
				sampler.readInterval(nSamples, nrOfChannels);
			}
			
			Arrays.fill(samples, 0);
			int srcPos = (int) (((from - sampleBufferBeginTime) * sampleFrequency) / 1000);
			int length = (int) (((to - from) * sampleFrequency) / 1000);
			if (channel == 1) {
				int[] intSamples = sampler.getFirstChannelArray();
				for (int i = 0; i < length; i++) {
					samples[i] = ((float) intSamples[i + srcPos]) / normalizationFactor;
				}
			} else {
				int[] intSamples = sampler.getSecondChannelArray();
				for (int i = 0; i < length; i++) {
					samples[i] = ((float) intSamples[i + srcPos]) / normalizationFactor;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Called by RecognizerHost to start the recognizer
	 *
	 */
	public void start() {
		keepRunning = true;
		recog();
	}

	/**
	 * Called by RecognizerHost to stop the recognizer, MUST BE OBEYED AT ALL TIMES
	 *
	 */
	public void stop() {
		keepRunning = false;
	}
	
	/**
	 * 
	 *
	 */
	public void recog() {
		int stepSize = 10;
		int stepDuration = 25;
		int nSamplesInStep = (stepDuration * sampleFrequency) / 1000;
		int powerOf2Size = (int) Math.pow(2, Math.floor(Math.log(nSamplesInStep) / Math.log(2)) + 1);
		System.out.println("samps: " + nSamplesInStep + " pow: " + powerOf2Size);
		float samples1R[] = new float[powerOf2Size];
		float samples1C[] = new float[powerOf2Size];
		float power1[] = new float[12];
		float samples2R[] = new float[powerOf2Size];
		float samples2C[] = new float[powerOf2Size];
		float power2[] = new float[12];
		for (long time = 0; time + stepDuration < duration; time += stepSize) {
			host.setProgress(time / duration);

			System.out.print(time + "  ");
			Arrays.fill(samples1C, 0);
			getSamples(time, time + stepDuration, 1, samples1R);
			hann(samples1R, nSamplesInStep);
			//float pre = samples1R[0];
			complexToComplex(1, powerOf2Size, samples1R, samples1C);
			power(samples1R, samples1C, power1, 8000);
			//complexToComplex(-1, powerOf2Size, samples1R, samples1C);
			//System.out.println(time + "       " + pre + " -> " + samples1R[0]);
			
			if (!keepRunning) {
				break;
			}
		}
		// if keepRunning is still true make sure the progress is set to 1
		if (keepRunning) {
			host.setProgress(1);
		}

	}

	public void power(float[] real, float[] complex, float[] power, int maxFreq) {
		int nBands = power.length;
		int samplesToMaxFreq = (real.length * sampleFrequency) / (2 * maxFreq);
		int samplesPerBand = (samplesToMaxFreq - 1) / nBands;
		float sum = 0;
		for (int i = 0; i < nBands; i++) {
			double pow = 0;
			for (int j = 0; j < samplesPerBand; j++) {
				int index = 1 + nBands * samplesPerBand + j;
				pow += Math.sqrt(real[i] * real[i] + complex[i] * complex[i]);
			}
			power[i] = (float) pow / samplesPerBand;
			sum += power[i];
		}
		System.out.println(sum);
	}
	
	
	//	 Taken from the freely available Fast Fourier transform laboratory by Dave Hale
	//	 http://sepwww.stanford.edu/oldsep/hale/FftLab.java
    public void complexToComplex(int sign, int n,
                                        float ar[], float ai[]) {
        float scale = (float)Math.sqrt(1.0f/n);

        int i,j;
        for (i=j=0; i<n; ++i) {
            if (j>=i) {
	            float tempr = ar[j]*scale;
	            float tempi = ai[j]*scale;
	            ar[j] = ar[i]*scale;
	            ai[j] = ai[i]*scale;
	            ar[i] = tempr;
	            ai[i] = tempi;
            }
            int m = n/2;
            while (m>=1 && j>=m) {
	            j -= m;
	            m /= 2;
            }
            j += m;
        }
    
        int mmax,istep;
        for (mmax=1,istep=2*mmax; mmax<n; mmax=istep,istep=2*mmax) {
            float delta = (float)sign*3.141592654f/(float)mmax;
            for (int m=0; m<mmax; ++m) {
	            float w = (float)m*delta;
	            float wr = (float)Math.cos(w);
	            float wi = (float)Math.sin(w);
	            for (i=m; i<n; i+=istep) {
	                j = i+mmax;
	                float tr = wr*ar[j]-wi*ai[j];
	                float ti = wr*ai[j]+wi*ar[j];
	                ar[j] = ar[i]-tr;
	                ai[j] = ai[i]-ti;
	                ar[i] += tr;
	                ai[i] += ti;
	            }
            }
            mmax = istep;
        }
    }

    private void hamming(float[] samples, int N) {
    	for (int i = 0; i < N; i++) {
    		double x = (2 * Math.PI * i) / (N - 1);
    		samples[i] *= 0.53836 - 0.46164 * Math.cos(x);
    	}
    }
    
    private void hann(float[] samples, int N) {
    	for (int i = 0; i < N; i++) {
    		double x = (2 * Math.PI * i) / (N - 1);
    		samples[i] *= 0.5 * (1- Math.cos(x));
    	}
    }

	public void updateLocale(Locale locale) {
		
	}

	public boolean canCombineMultipleFiles() {
		return false;
	}

	public boolean canHandleMedia(String mediaFilePath) {
		if (mediaFilePath == null) {
			return false;
		}
		
		try {
			WAVSampler wavs = new WAVSampler(mediaFilePath);
			int nc = wavs.getWavHeader().getNumberOfChannels();
			if (nc == 0) {
				return false;
			}
		} catch(IOException ioe) {
			return false;
		} catch (Exception exc) {
			return false;
		}
		return true;
	}

	public void dispose() {
		controlPanel = null;
		sampler = null;
		host = null;
	}

	public Object getParameterValue(String param) {
		return null;
	}

	public int getRecognizerType() {
		return Recognizer.AUDIO_TYPE;
	}

	public String getReport() {
		return null;
	}

	public void setParameterValue(String param, String value) {
		// TODO Auto-generated method stub
		
	}

	public void setParameterValue(String param, float value) {
		// TODO Auto-generated method stub
		
	}

	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void validateParameters() throws RecognizerConfigurationException {
		// TODO Auto-generated method stub
		
	}
}