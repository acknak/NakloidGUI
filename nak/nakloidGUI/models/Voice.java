package nak.nakloidGUI.models;

import java.nio.file.Path;
import java.nio.file.Paths;

final public class Voice implements Comparable<Voice> {
	private PronunciationAlias pronunciation;
	private short offset; //左ブランク
	private short overlap; //オーバーラップ
	private short preutterance; //先行発声
	private short consonant; //子音部
	private short blank; //右ブランク
	private Path pathWav, pathOtoIni;

	public static class Builder {
		private PronunciationAlias pronunciation;
		private short offset = 0;
		private short overlap = 0;
		private short preutterance = 0;
		private short consonant = 0;
		private short blank = 0;
		private Path pathWav, pathOtoIni;
		public Builder(PronunciationAlias pronunciation) {
			this.pronunciation = pronunciation;
		}
		public Builder(String pronunciation) {
			this.pronunciation = new PronunciationAlias(pronunciation);
		}
		public Builder(Voice voice) {
			this.offset = voice.offset;
			this.overlap = voice.overlap;
			this.preutterance = voice.preutterance;
			this.consonant = voice.consonant;
			this.blank = voice.blank;
			this.pathWav = voice.getWavPath();
			this.pathOtoIni = voice.getOtoIniPath();
			this.pronunciation = voice.getPronunciationAlias();
		}
		public Builder otoParams(short offset, short overlap, short preutterance, short consonant, short blank) {
			this.offset = offset;
			this.overlap = overlap;
			this.preutterance = preutterance;
			this.consonant = consonant;
			this.blank = blank;
			return this;
		}
		public Builder otoParams(Voice voice) {
			this.offset = voice.offset;
			this.overlap = voice.overlap;
			this.preutterance = voice.preutterance;
			this.consonant = voice.consonant;
			this.blank = voice.blank;
			return this;
		}
		public Builder offset(short offset) {
			this.offset = offset;
			return this;
		}
		public Builder overlap(short overlap) {
			this.overlap = overlap;
			return this;
		}
		public Builder preutterance(short preutterance) {
			this.preutterance = preutterance;
			return this;
		}
		public Builder consonant(short consonant) {
			this.consonant = consonant;
			return this;
		}
		public Builder blank(short blank) {
			this.blank = blank;
			return this;
		}
		public Builder otoPaths(Path pathWav, Path pathOtoIni) {
			this.pathWav = pathWav;
			this.pathOtoIni = pathOtoIni;
			return this;
		}
		public Builder otoPaths(String wavname, Path pathOtoIni) {
			this.pathWav = Paths.get(pathOtoIni.getParent().toString(), wavname);
			this.pathOtoIni = pathOtoIni;
			return this;
		}
		public Builder otoPaths(Voice voice) {
			this.pathWav = voice.getWavPath();
			this.pathOtoIni =voice.getOtoIniPath();
			return this;
		}
		public Voice build(){
			return new Voice(this);
		}
	}

	private Voice(Builder builder) {
		this.pronunciation = builder.pronunciation;
		this.offset = builder.offset;
		this.overlap = builder.overlap;
		this.preutterance = builder.preutterance;
		this.consonant = builder.consonant;
		this.blank = builder.blank;
		this.pathWav = builder.pathWav;
		this.pathOtoIni = builder.pathOtoIni;
	}

	static public enum ParameterType {
		PRONUNCIATION(0), OFFSET(1), OVERLAP(2), PREUTTERANCE(3), CONSONANT(4), BLANK(5);
		private static final String[] emStrings = {"発音","左ブランク","オーバーラップ","先行発声","子音部","右ブランク"};
		private static final String[] enStrings = {"発音","左ﾌﾞﾗﾝｸ","ｵｰﾊﾞｰﾗｯﾌﾟ","先行発声","子音部","右ﾌﾞﾗﾝｸ"};
		private final int id;
		private ParameterType(int id) {
			this.id = id;
		}
		public String getEmString() {
			return emStrings[id];
		}
		public String getEnString() {
			return enStrings[id];
		}
	}

	public int compareTo(Voice target){
		return this.pronunciation.getAliasString().compareTo(target.pronunciation.getAliasString());
	}

	public String getPronunciationString() {
		return pronunciation.getAliasString();
	}

	public PronunciationAlias getPronunciationAlias() {
		return pronunciation;
	}

	public boolean isVCV() {
		return pronunciation.checkVCV();
	}

	public short getOffset() {
		return offset;
	}

	public short getOverlap() {
		return overlap;
	}

	public short getPreutterance() {
		return preutterance;
	}

	public short getConsonant() {
		return consonant;
	}

	public short getBlank() {
		return blank;
	}

	public Path getWavPath() {
		return pathWav;
	}

	public Path getOtoIniPath() {
		return pathOtoIni;
	}

	public Path getPmpPath() {
		return Paths.get(pathWav.getParent().toString(), pronunciation.getAliasString().replace("*","_")+".pmp");
	}

	public Path getUwcPath() {
		return Paths.get(pathWav.getParent().toString(), pronunciation.getAliasString().replace("*","_")+".uwc");
	}

	public Path getFrqPath() {
		return Paths.get(pathWav.toString().replace(".wav","_wav")+".frq");
	}
}
