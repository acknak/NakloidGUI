package nak.nakloidGUI.models;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

final public class Waveform implements LineListener {
	private double[] waveformData;
	private float sampleRate = 0;
	private Clip clip;
	private Path path;
	private int id = (int)(Math.random()*Integer.MAX_VALUE);
	private WaveformStatus status;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	static public enum WaveformStatus {
		LOADING(0), STOPPED(1), PLAYING(2), CLOSED(-1), UNSUPPORTED_AUDIO_FILE_EXCEPTION(-2), IO_EXCEPTION(-3), LINE_UNAVAILABLE_EXCEPTION(-4);
		private int value;
		private WaveformStatus(int value) {
			this.value = value;
		}
		public int getValue() {
			return this.value;
		}
		public boolean isLoaded() {
			return getValue()>0;
		}
		public boolean hasError() {
			return getValue()<0;
		}
	}

	public Waveform(Path path) {
		this.path = path;
		status = WaveformStatus.LOADING;
		executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					clip = AudioSystem.getClip();
					clip.open(AudioSystem.getAudioInputStream(path.toFile()));

					AudioInputStream ais = AudioSystem.getAudioInputStream(path.toFile());
					AudioFormat audioFormat = ais.getFormat();
					sampleRate = audioFormat.getSampleRate();
					byte[] byteData = new byte[ais.available()];
					ais.read(byteData);
					ais.close();
					ais = null;
					System.gc();

					ShortBuffer sbuf = ByteBuffer.wrap(byteData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
					short[] shortData = new short[sbuf.capacity()];
					sbuf.get(shortData);
					waveformData = new double[shortData.length];
					for (int i=0; i<shortData.length; i++) {
						waveformData[i] = shortData[i]/(double)Short.MAX_VALUE;
					}
					status = WaveformStatus.STOPPED;
				} catch (UnsupportedAudioFileException e) {
					status = WaveformStatus.UNSUPPORTED_AUDIO_FILE_EXCEPTION;
				} catch (IOException e) {
					status = WaveformStatus.IO_EXCEPTION;
				} catch (LineUnavailableException e) {
					status = WaveformStatus.LINE_UNAVAILABLE_EXCEPTION;
				}
			}
		});
		executor.shutdown();
	}

	public int getId() {
		return id;
	}

	public double getData(int sub) {
		return waveformData[sub];
	}

	public double[] getData() {
		if (isLoaded()) {
			return Arrays.copyOf(waveformData, waveformData.length);
		}
		return null;
	}

	public int getDataSize() {
		return waveformData.length;
	}

	public float getSampleRate() {
		if (isLoaded()) {
			return sampleRate;
		}
		return 0;
	}

	public Path getPath() {
		if (!hasError()) {
			return path;
		}
		return null;
	}

	public boolean isLoaded() {
		return status.isLoaded();
	}

	public boolean hasError() {
		return status.hasError();
	}

	public boolean isPlaying() {
		return status==WaveformStatus.PLAYING;
	}

	public WaveformStatus getStatus() {
		return status;
	}

	public void setVolume(int volume) {
		if (isLoaded()) {
			FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
			control.setValue((float)Math.log10(volume/100.0)*20);
		}
	}

	public void play() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		if (status == WaveformStatus.STOPPED) {
			status = WaveformStatus.PLAYING;
			clip.addLineListener(this);
			clip.start();
		}
	}

	public void pause() {
		if (status == WaveformStatus.PLAYING) {
			long tmp = clip.getLongFramePosition();
			clip.stop();
			clip.flush();
			clip.setFramePosition((int)tmp);
			status = WaveformStatus.STOPPED;
		}
	}

	public void close() {
		if (clip != null) {
			clip.stop();
			clip.close();
			clip = null;
		}
		waveformData = null;
		status = WaveformStatus.CLOSED;
	}

	public long getMicrosecond() {
		if (isLoaded() && clip!=null) {
			return clip.getMicrosecondPosition();
		}
		return 0;
	}

	public void setMicrosecond(long pos) {
		if (isLoaded()) {
			clip.setMicrosecondPosition(pos);
		}
	}

	@Override
	public void update(LineEvent e) {
		if (e.getType()==LineEvent.Type.STOP) {
			clip.stop();
			clip.setFramePosition(0);
			clip.removeLineListener(this);
			status = WaveformStatus.STOPPED;
		}
	}
}
