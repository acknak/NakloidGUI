package nak.nakloidGUI.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import nak.nakloidGUI.NakloidGUI;

@JsonDeserialize(builder=Note.Builder.class)
final public class Note implements Comparable<Note> {
	private int id;
	private PronunciationAlias pron;
	private int start, end;
	private int front_margin, back_margin;
	private int front_padding, back_padding;
	private int vel;
	private int pitch;
	private List<ArrayList<Long>> vel_points = new ArrayList<ArrayList<Long>>();

	public static class Builder {
		private int id = 0;
		private PronunciationAlias pron;
		private int start=0, end=500;
		private int frontMargin=0, backMargin=0;
		private int frontPadding = NakloidGUI.preferenceStore.getInt("ini.note.ms_front_padding");
		private int backPadding = NakloidGUI.preferenceStore.getInt("ini.note.ms_back_padding");
		private int baseVelocity = 100;
		private int basePitch = 69;
		private List<ArrayList<Long>> vel_points = new ArrayList<ArrayList<Long>>();
		public Builder() {}
		public Builder(int id) {
			this.id = id;
		}
		public Builder(Note note) {
			this.id = note.id;
			this.pron = note.pron;
			this.start = note.start;
			this.end = note.end;
			this.frontMargin = note.front_margin;
			this.backMargin = note.back_margin;
			this.frontPadding = note.front_padding;
			this.backPadding = note.back_padding;
			this.baseVelocity = note.vel;
			this.basePitch = note.pitch;
		}
		@JsonProperty("id")
		public Builder setId(int id) {
			this.id = id;
			return this;
		}
		@JsonProperty("alias")
		public Builder setPronunciationAlias(String pronunciationAliasString) {
			this.pron = new PronunciationAlias(pronunciationAliasString);
			return this;
		}
		public Builder setPronunciationAlias(PronunciationAlias pron) {
			this.pron = pron;
			return this;
		}
		public Builder range(int start, int end) {
			this.start = start;
			this.end = end;
			return this;
		}
		@JsonProperty("start")
		public Builder setStart(int start) {
			this.start = start;
			return this;
		}
		@JsonProperty("end")
		public Builder setEnd(int end) {
			this.end = end;
			return this;
		}
		public Builder margin(int front, int back) {
			this.frontMargin = front;
			this.backMargin = back;
			return this;
		}
		@JsonProperty("front_margin")
		public Builder setFrontMargin(int frontMargin) {
			this.frontMargin = frontMargin;
			return this;
		}
		@JsonProperty("back_margin")
		public Builder setBackMargin(int backMargin) {
			this.backMargin = backMargin;
			return this;
		}
		public Builder padding(int front, int back) {
			this.frontPadding = front;
			this.backPadding = back;
			return this;
		}
		@JsonProperty("front_padding")
		public Builder setFrontPadding(int frontPadding) {
			this.frontPadding = frontPadding;
			return this;
		}
		@JsonProperty("back_padding")
		public Builder setBackPadding(int backPadding) {
			this.backPadding = backPadding;
			return this;
		}
		@JsonProperty("vel")
		public Builder setBaseVelocity(int baseVelocity) {
			this.baseVelocity = baseVelocity;
			return this;
		}
		@JsonProperty("pitch")
		public Builder setBasePitch(int setBasePitch) {
			this.basePitch = setBasePitch;
			return this;
		}
		@JsonProperty("vel_points")
		public Builder setVelPoints(List<ArrayList<Long>> vel_points) {
			this.vel_points = vel_points;
			return this;
		}
		public Note build() {
			if (pron == null) {
				pron = new PronunciationAlias("- ら");
			}
			return new Note(this);
		}
	}

	private Note(Builder builder) {
		this.id = builder.id;
		this.pron = builder.pron;
		this.start = builder.start;
		this.end = builder.end;
		this.front_margin = builder.frontMargin;
		this.back_margin = builder.backMargin;
		this.front_padding = builder.frontPadding;
		this.back_padding = builder.backPadding;
		this.vel = builder.baseVelocity;
		this.pitch = builder.basePitch;
		this.vel_points = builder.vel_points;
	}

	@Override
	public int compareTo(Note tmpNote) {
		return getStart() - tmpNote.getStart();
	}

	@JsonProperty("id")
	public int getId() {
		return id;
	}

	@JsonProperty("alias")
	public String getPronunciationAliasString() {
		return pron.getAliasString();
	}

	@JsonIgnore
	public PronunciationAlias getPronunciationAlias() {
		return pron;
	}

	@JsonProperty("start")
	public int getStart() {
		return start;
	}

	@JsonProperty("end")
	public int getEnd() {
		return end;
	}

	@JsonIgnore
	public int getLength() {
		return end - start;
	}

	@JsonProperty("front_margin")
	public int getFrontMargin() {
		return front_margin;
	}

	@JsonProperty("back_margin")
	public int getBackMargin() {
		return back_margin;
	}

	@JsonProperty("front_padding")
	public int getFrontPadding() {
		return front_padding;
	}

	@JsonProperty("back_padding")
	public int getBackPadding() {
		return back_padding;
	}

	@JsonProperty("vel")
	public int getBaseVelocity() {
		return vel;
	}

	@JsonProperty("pitch")
	public int getBasePitch() {
		return pitch;
	}

	@JsonIgnore
	public int getPronStart(int score_margin, Voice voice) {
		return start - voice.getPreutterance() - ((voice.getOverlap()<0)?voice.getOverlap():0) + score_margin;
	}

	@JsonIgnore
	public int getPronEnd(int score_margin, Voice voice) {
		int tmp_pron_end = end + score_margin - back_margin;
		int tmp_pron_start = getPronStart(score_margin, voice);
		if (tmp_pron_start > tmp_pron_end) {
			return tmp_pron_start + 1;
		}
		return tmp_pron_end;
	}

	@JsonIgnore
	public int getPronLength(Voice voice) {
		return getPronEnd(0, voice) - getPronStart(0, voice);
	}

	@JsonProperty("vel_points")
	public List<ArrayList<Long>> getVelPoints() {
		return new ArrayList<ArrayList<Long>>(vel_points);
	}

	@JsonIgnore
	public List<Long> getVelPoint(int i) {
		return vel_points.get(i);
	}

	@JsonIgnore
	public int getVelPointsSize() {
		return vel_points.size();
	}

	@JsonIgnore
	public Note addVelPoint(long ms, long size) {
		List<ArrayList<Long>> tmpVelPoints = new ArrayList<ArrayList<Long>>(vel_points);
		ArrayList<Long> tmpVelPoint = new ArrayList<Long>();
		tmpVelPoint.add(ms);
		tmpVelPoint.add(size);
		tmpVelPoints.add(tmpVelPoint);
		return new Note.Builder(this).setVelPoints(tmpVelPoints).build();
	}

	@JsonIgnore
	public Note deleteVelPoint(long ms) {
		List<ArrayList<Long>> tmpVelPoints = new ArrayList<ArrayList<Long>>();
		for (ArrayList<Long> tmpVelPoint : vel_points) {
			if (tmpVelPoint.get(0)!=ms && end-start-back_margin+tmpVelPoint.get(0)!=ms) {
				tmpVelPoints.add(new ArrayList<Long>(tmpVelPoint));
			}
		}
		return new Note.Builder(this).setVelPoints(tmpVelPoints).build();
	}
}
