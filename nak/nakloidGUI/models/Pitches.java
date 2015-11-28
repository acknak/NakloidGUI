package nak.nakloidGUI.models;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

final public class Pitches {
	private double[] midiNoteNumbers;
	final public Path path;

	public static class Builder {
		private double[] midiNoteNumbers;
		private Path path = null;
		public Builder(Path path) throws IOException {
			this.path = path;
			double logBottom = Math.log(2.0);
			midiNoteNumbers = new double[(int)Files.size(path)/Integer.BYTES];
			byte[] tmpBytes = new byte[Float.BYTES];
			ByteBuffer bb = ByteBuffer.allocate(Float.BYTES);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(Files.readAllBytes(path)))) {
				for (int i=0; i<midiNoteNumbers.length; i++) {
					bis.read(tmpBytes);
					midiNoteNumbers[i] = 12*Math.log(bb.put(tmpBytes).getFloat(0)/440.0)/logBottom+69;
					bb.clear();
				}
			}
		}
		public Builder(Pitches pitches) {
			this.midiNoteNumbers = pitches.midiNoteNumbers.clone();
			this.path = pitches.path;
		}
		public Builder(double[] midiNoteNumbers) {
			this.midiNoteNumbers = midiNoteNumbers.clone();
		}
		public Builder(List<Double> midiNoteNumbers) {
			this.midiNoteNumbers = new double[midiNoteNumbers.size()];
			for (int i=0; i<this.midiNoteNumbers.length; i++) {
				this.midiNoteNumbers[i] = midiNoteNumbers.get(0);
			}
		}
		public Builder replaceMidiNoteNumbers(double[] tmpNumbers, int from) {
			int to = Math.min(from+tmpNumbers.length, midiNoteNumbers.length);
			for (int i=from; i < to; i++) {
				midiNoteNumbers[i] = tmpNumbers[i-from];
			}
			return this;
		}
		public Builder stretch(int ms) {
			if (midiNoteNumbers.length>0 && ms>0) {
				double[] tmpMidiNoteNumbers = new double[ms+midiNoteNumbers.length];
				for (int i=0; i<ms; i++) {
					tmpMidiNoteNumbers[i] = midiNoteNumbers[0];
				}
				for (int i=0; i<midiNoteNumbers.length; i++) {
					tmpMidiNoteNumbers[ms+i] = midiNoteNumbers[i];
				}
				midiNoteNumbers = tmpMidiNoteNumbers;
			}
			return this;
		}
		public Builder shorten(int ms) {
			if (midiNoteNumbers.length>0 && ms>0 && midiNoteNumbers.length>ms) {
				double[] tmpMidiNoteNumbers = new double[midiNoteNumbers.length-ms];
				for (int i=0; i<tmpMidiNoteNumbers.length; i++) {
					tmpMidiNoteNumbers[i] = midiNoteNumbers[ms+i];
				}
				midiNoteNumbers = tmpMidiNoteNumbers;
			}
			return this;
		}
		public Builder replaceMidiNoteNumbers(List<Double> tmpNumbers, int from) {
			int to = Math.min(from+tmpNumbers.size(), midiNoteNumbers.length);
			for (int i=from; i<to; i++) {
				midiNoteNumbers[i] = tmpNumbers.get(i-from);
			}
			return this;
		}
		public Pitches build() {
			return new Pitches(this);
		}
	}

	private Pitches(Builder builder) {
		this.midiNoteNumbers = builder.midiNoteNumbers.clone();
		this.path = builder.path;
	}

	public double getMidiNoteNumber(int sub) {
		return midiNoteNumbers[sub];
	}

	public double[] getMidiNoteNumbers() {
		return midiNoteNumbers.clone();
	}

	public List<Double> getMidiNoteNumbersList() {
		return DoubleStream.of(midiNoteNumbers).boxed().collect(Collectors.toList());
	}

	public int size() {
		return midiNoteNumbers.length;
	}

	public void save() throws IOException {
		save(path);
	}

	public void save(Path path) throws IOException {
		if (path != null) {
			ByteBuffer bb = ByteBuffer.allocate(midiNoteNumbers.length*Integer.BYTES);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			for (double midiNoteNumber : midiNoteNumbers) {
				bb.putInt(Float.floatToIntBits((float)(Math.pow(2.0,(midiNoteNumber-69)/12)*440)));
			}
			try (BufferedOutputStream dos = new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.WRITE))){
				dos.write(bb.array());
			}
		}
	}
}
