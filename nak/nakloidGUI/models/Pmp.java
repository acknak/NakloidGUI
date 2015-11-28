package nak.nakloidGUI.models;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder=Pmp.Builder.class)
final public class Pmp {
	private int subFadeStart;
	private int basePitch;
	private List<Integer> pitchmarkPoints;
	private VowelWav baseVowelWav;
	private VowelWav prefixVowelWav;
	@JsonIgnore
	public final Path path;

	final public static class VowelWav {
		public String filename = "";
		public int from=0, to=0;
		public VowelWav(){}
		public VowelWav(String filename, int from, int to) {
			this.filename = filename;
			this.from = from;
			this.to = to;
		}
		@JsonIgnore
		public int getLength() {
			return to-from;
		}
	}

	public static class Builder {
		private int subFadeStart = 0;
		private int basePitch = 0;
		private List<Integer> pitchmarkPoints = null;
		private VowelWav baseVowelWav;
		private VowelWav prefixVowelWav;
		private Path path;
		public Builder() {}
		public Builder(Voice voice) throws IOException {
			this.path = voice.getPmpPath();
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			try (InputStream is = Files.newInputStream(this.path)) {
				Pmp pmp = mapper.readValue(is, Pmp.class);
				subFadeStart = pmp.subFadeStart;
				basePitch = pmp.basePitch;
				pitchmarkPoints = pmp.pitchmarkPoints;
				baseVowelWav = new VowelWav(pmp.baseVowelWav.filename, pmp.baseVowelWav.from, pmp.baseVowelWav.to);
				prefixVowelWav = new VowelWav(pmp.prefixVowelWav.filename, pmp.prefixVowelWav.from, pmp.prefixVowelWav.to);
			}
		}
		public Builder(Pmp pmp) {
			this.subFadeStart = pmp.subFadeStart;
			this.basePitch = pmp.basePitch;
			this.pitchmarkPoints = pmp.pitchmarkPoints;
			this.baseVowelWav = new VowelWav(pmp.baseVowelWav.filename, pmp.baseVowelWav.from, pmp.baseVowelWav.to);
			this.prefixVowelWav = new VowelWav(pmp.prefixVowelWav.filename, pmp.prefixVowelWav.from, pmp.prefixVowelWav.to);
			this.path = pmp.path;
		}
		@JsonProperty("sub_fade_start")
		public Builder setSubFadeStart(int subFadeStart) {
			this.subFadeStart = subFadeStart;
			return this;
		}
		@JsonProperty("base_pitch")
		public Builder setBasePitch(int basePitch) {
			this.basePitch = basePitch;
			return this;
		}
		@JsonProperty("pitchmark_points")
		public Builder setPitchmarkPoints(int[] pitchmarkPoints) {
			this.pitchmarkPoints = Arrays.stream(pitchmarkPoints).boxed().collect(Collectors.toList());
			return this;
		}
		@JsonIgnore
		public Builder setPitchmarkPoints(String pitchmarkString) {
			return setPitchmarkPoints(pitchmarkString, 0, Integer.MAX_VALUE);
		}
		@JsonIgnore
		public Builder setPitchmarkPoints(String pitchmarkString, int from, int to) {
			if (!pitchmarkString.isEmpty()) {
				pitchmarkPoints = Stream
						.concat(
								Arrays.stream(pitchmarkString.replaceAll("\\r|\\n|\\s","").split(","))
										.flatMapToInt(s->IntStream.of(Integer.valueOf(s)))
										.filter(i->i>=from&&i<=to)
										.boxed(),
								pitchmarkPoints.stream()
										.filter(i->i<from||i>to)
						)
						.sorted()
						.distinct()
						.collect(Collectors.toList());
			}
			return this;
		}
		@JsonProperty("base_vowel_wav")
		public Builder setBaseVowelWav(VowelWav baseVowelWav) {
			this.baseVowelWav = new VowelWav(baseVowelWav.filename, baseVowelWav.from, baseVowelWav.to);
			return this;
		}
		@JsonProperty("prefix_vowel_wav")
		public Builder setPrefixVowelWav(VowelWav prefixVowelWav) {
			this.prefixVowelWav = new VowelWav(prefixVowelWav.filename, prefixVowelWav.from, prefixVowelWav.to);
			return this;
		}
		@JsonIgnore
		public Builder setPath(Path path) {
			this.path = path;
			return this;
		}
		public Pmp build(){
			return new Pmp(this);
		}
	}

	private Pmp(Builder builder) {
		this.subFadeStart = builder.subFadeStart;
		this.basePitch = builder.basePitch;
		this.pitchmarkPoints = builder.pitchmarkPoints;
		this.baseVowelWav = new VowelWav(builder.baseVowelWav.filename, builder.baseVowelWav.from, builder.baseVowelWav.to);
		this.prefixVowelWav = new VowelWav(builder.prefixVowelWav.filename, builder.prefixVowelWav.from, builder.prefixVowelWav.to);
		this.path = builder.path;
	}

	@JsonIgnore
	public Path getPath(){
		return this.path;
	}

	@JsonProperty("filename")
	public String getFilenameString(){
		return this.path.getFileName().toString();
	}

	@JsonProperty("sub_fade_start")
	public int getSubFadeStart() {
		return subFadeStart;
	}

	@JsonProperty("base_pitch")
	public int getBasePitch() {
		return basePitch;
	}

	@JsonProperty("pitchmark_points")
	public List<Integer> getPitchmarkPointsList() {
		return new ArrayList<Integer>(pitchmarkPoints);
	}

	@JsonIgnore
	public String getPitchmarkPointsString() {
		return pitchmarkPoints.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));
	}

	@JsonIgnore
	public String getPitchmarkPointsStringFromRange(int from, int to) {
		return pitchmarkPoints.stream()
				.filter(i->i>=from&&i<=to)
				.map(String::valueOf)
				.collect(Collectors.joining(","));
	}

	@JsonProperty("base_vowel_wav")
	public VowelWav getBaseVowelWav() {
		return baseVowelWav;
	}

	@JsonProperty("prefix_vowel_wav")
	public VowelWav getPrefixVowelWav() {
		return prefixVowelWav;
	}

	@JsonIgnore
	public int size() {
		return pitchmarkPoints.size();
	}

	@JsonIgnore
	public int get(int sub) {
		return pitchmarkPoints.get(sub);
	}

	@JsonIgnore
	public void save(Path path) throws JsonGenerationException, JsonMappingException, IOException {
		Path pathSave = (path==null)?this.path:path;
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.configure(Feature.ESCAPE_NON_ASCII, true);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		try (BufferedWriter bw = Files.newBufferedWriter(pathSave)) {
			mapper.writeValue(bw, this);
		}
	}
}
