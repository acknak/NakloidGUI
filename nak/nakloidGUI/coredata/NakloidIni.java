package nak.nakloidGUI.coredata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import nak.nakloidGUI.NakloidGUI;

public class NakloidIni {
	static public enum ScoreMode {score_mode_nak, score_mode_ust, score_mode_smf}
	static public enum PitchesMode {pitches_mode_pit, pitches_mode_lf0, pitches_mode_none}
	public class Input {
		public Path path_input_score = Paths.get(NakloidGUI.preferenceStore.getString("ini.input.path_input_score"));
		public ScoreMode score_mode = ScoreMode.score_mode_nak;
		public short track = (short)NakloidGUI.preferenceStore.getInt("ini.input.track");
		public Path path_lyrics = Paths.get(NakloidGUI.preferenceStore.getString("ini.input.path_lyrics"));
		public Path path_input_pitches = Paths.get(NakloidGUI.preferenceStore.getString("ini.input.path_input_pitches"));
		public PitchesMode pitches_mode = PitchesMode.pitches_mode_pit;
		public short pitch_frame_length = (short)NakloidGUI.preferenceStore.getInt("ini.input.pitch_frame_length");;
		public Path path_singer = Paths.get(NakloidGUI.preferenceStore.getString("ini.input.path_singer"));
		public Path path_prefix_map = null;
	}
	public class Output {
		public Path path_song = Paths.get(NakloidGUI.preferenceStore.getString("ini.output.path_song"));
		public Path path_output_score = null;
		public Path path_output_pitches = null;
		public long ms_margin = NakloidGUI.preferenceStore.getLong("ini.output.ms_margin");
		public double max_volume = NakloidGUI.preferenceStore.getDouble("ini.output.max_volume");
		public boolean compressor = NakloidGUI.preferenceStore.getBoolean("ini.output.compressor");
		public double compressor_threshold = NakloidGUI.preferenceStore.getDouble("ini.output.compressor_threshold");
		public double compressor_ratio = NakloidGUI.preferenceStore.getDouble("ini.output.compressor_ratio");
		public boolean print_debug = NakloidGUI.preferenceStore.getBoolean("ini.output.print_debug");
		public boolean print_log = true;
	}
	public class VocalLibrary {
		public boolean use_pmp_cache = NakloidGUI.preferenceStore.getBoolean("ini.vocal_library.use_pmp_cache");
		public boolean use_uwc_cache = NakloidGUI.preferenceStore.getBoolean("ini.vocal_library.use_uwc_cache");
	}
	public class Note {
		public short ms_front_padding = (short)NakloidGUI.preferenceStore.getInt("ini.note.ms_front_padding");
		public short ms_back_padding = (short)NakloidGUI.preferenceStore.getInt("ini.note.ms_back_padding");
	}
	public class Arrange {
		public boolean auto_vowel_combining = NakloidGUI.preferenceStore.getBoolean("ini.arrange.auto_vowel_combining");
		public double vowel_combining_volume = NakloidGUI.preferenceStore.getDouble("ini.arrange.vowel_combining_volume");
		public boolean vibrato = NakloidGUI.preferenceStore.getBoolean("ini.arrange.vibrato");
		public short ms_vibrato_offset = (short)NakloidGUI.preferenceStore.getInt("ini.arrange.ms_vibrato_offset");
		public short ms_vibrato_width = (short)NakloidGUI.preferenceStore.getInt("ini.arrange.ms_vibrato_width");
		public double pitch_vibrato = NakloidGUI.preferenceStore.getDouble("ini.arrange.pitch_vibrato");
		public boolean overshoot = NakloidGUI.preferenceStore.getBoolean("ini.arrange.overshoot");
		public short ms_overshoot = (short)NakloidGUI.preferenceStore.getInt("ini.arrange.ms_overshoot");
		public double pitch_overshoot = NakloidGUI.preferenceStore.getDouble("ini.arrange.pitch_overshoot");
		public boolean preparation = NakloidGUI.preferenceStore.getBoolean("ini.arrange.preparation");
		public short ms_preparation = (short)NakloidGUI.preferenceStore.getInt("ini.narrangeote.ms_preparation");
		public double pitch_preparation = NakloidGUI.preferenceStore.getDouble("ini.arrange.pitch_preparation");
		public boolean finefluctuation = NakloidGUI.preferenceStore.getBoolean("ini.arrange.finefluctuation_deviation");
		public double finefluctuation_deviation = NakloidGUI.preferenceStore.getDouble("ini.arrange.finefluctuation_deviation");
	}
	public class UnitWaveformContainer {
		public double target_rms = NakloidGUI.preferenceStore.getDouble("ini.unit_waveform_container.target_rms");
		public short num_lobes = (short)NakloidGUI.preferenceStore.getInt("ini.unit_waveform_container.num_lobes");
		public boolean uwc_normalize = NakloidGUI.preferenceStore.getBoolean("ini.unit_waveform_container.uwc_normalize");
	}
	public class Pitchmark {
		public long default_pitch = NakloidGUI.preferenceStore.getLong("ini.pitchmark.default_pitch");
		public short pitch_margin = (short)NakloidGUI.preferenceStore.getInt("ini.pitchmark.pitch_margin");
		public double xcorr_threshold = NakloidGUI.preferenceStore.getDouble("ini.pitchmark.xcorr_threshold");
	}
	public class Overlap {
		public boolean stretch_self_fade = NakloidGUI.preferenceStore.getBoolean("ini.overlap.stretch_self_fade");
		public long ms_self_fade = NakloidGUI.preferenceStore.getLong("ini.overlap.ms_self_fade");
		public boolean interpolation = NakloidGUI.preferenceStore.getBoolean("ini.overlap.interpolation");
		public boolean overlap_normalize = NakloidGUI.preferenceStore.getBoolean("ini.overlap.overlap_normalize");
		public boolean window_modification = NakloidGUI.preferenceStore.getBoolean("ini.overlap.window_modification");
	}

	public Input input = new Input();
	public Output output = new Output();
	public VocalLibrary vocalLibrary = new VocalLibrary();
	public UnitWaveformContainer unitWaveformContainer = new UnitWaveformContainer();
	public Note note = new Note();
	public Arrange arrange = new Arrange();
	public Pitchmark pitchmark = new Pitchmark();
	public Overlap overlap = new Overlap();
	public final static Path pathNakloidIni = Paths.get("Nakloid.ini");

	public NakloidIni(){}

	public NakloidIni(Path path) throws IOException {
		load(path);
	}

	public void load(Path path) throws IOException {
		Properties p = new Properties();
		try (BufferedReader br = Files.newBufferedReader(path)) {
			p.load(br);
		} catch (IOException e) {
			throw e;
		}
		if (p.containsKey("path_input_score")) {
			input.path_input_score = Paths.get(p.getProperty("path_input_score"));
		}
		if (p.containsKey("score_mode")) {
			String tmp = p.getProperty("score_mode");
			if (tmp.equals("smf")) {
				input.score_mode = ScoreMode.score_mode_smf;
			} else if (tmp.equals("ust")) {
				input.score_mode = ScoreMode.score_mode_ust;
			} else {
				input.score_mode = ScoreMode.score_mode_nak;
			}
		}
		if (p.containsKey("track")) {
			input.track = Short.valueOf(p.getProperty("track"), input.track);
		}
		if (p.containsKey("path_lyrics")) {
			input.path_lyrics = Paths.get(p.getProperty("path_lyrics"));
		}
		if (p.containsKey("path_input_pitches")) {
			input.path_input_pitches = Paths.get(p.getProperty("path_input_pitches"));
		}
		if (p.containsKey("pitches_mode")) {
			String tmp = p.getProperty("pitches_mode");
			if (tmp.equals("pit")) {
				input.pitches_mode = PitchesMode.pitches_mode_pit;
			} else if (tmp.equals("lf0")) {
				input.pitches_mode = PitchesMode.pitches_mode_lf0;
			} else {
				input.pitches_mode = PitchesMode.pitches_mode_none;
			}
		}
		if (p.containsKey("input.pitch_frame_length")) {
			input.pitch_frame_length = Short.valueOf(p.getProperty("pitch_frame_length"));
		}
		if (p.containsKey("path_singer")) {
			input.path_singer = Paths.get(p.getProperty("path_singer"));
		}
		if (p.containsKey("path_prefix_map")) {
			input.path_prefix_map = Paths.get(p.getProperty("path_prefix_map"));
		}
		if (p.containsKey("path_song")) {
			output.path_song = Paths.get(p.getProperty("path_song"));
		}
		if (p.containsKey("path_output_score")) {
			output.path_output_score = Paths.get(p.getProperty("path_output_score"));
		}
		if (p.containsKey("path_output_pitches")) {
			output.path_output_pitches = Paths.get(p.getProperty("path_output_pitches"));
		}
		if (p.containsKey("ms_margin")) {
			output.ms_margin = Long.valueOf(p.getProperty("ms_margin"));
		}
		if (p.containsKey("max_volume")) {
			output.max_volume = Double.valueOf(p.getProperty("max_volume"));
		}
		if (p.containsKey("compressor")) {
			output.compressor = Boolean.valueOf(p.getProperty("compressor"));
		}
		if (p.containsKey("compressor_threshold")) {
			output.compressor_threshold = Double.valueOf(p.getProperty("compressor_threshold"));
		}
		if (p.containsKey("compressor_ratio")) {
			output.compressor_ratio = Double.valueOf(p.getProperty("compressor_ratio"));
		}
		if (p.containsKey("print_debug")) {
			output.print_debug = Boolean.valueOf(p.getProperty("print_debug"));
		}
		if (p.containsKey("print_log")) {
			output.print_log = Boolean.valueOf(p.getProperty("print_log"));
		}
		if (p.containsKey("use_pmp_cache")) {
			vocalLibrary.use_pmp_cache = Boolean.valueOf(p.getProperty("use_pmp_cache"));
		}
		if (p.containsKey("use_uwc_cache")) {
			vocalLibrary.use_uwc_cache = Boolean.valueOf(p.getProperty("use_uwc_cache"));
		}
		if (p.containsKey("ms_front_padding")) {
			note.ms_front_padding = Short.valueOf(p.getProperty("ms_front_padding"));
		}
		if (p.containsKey("ms_back_padding")) {
			note.ms_back_padding = Short.valueOf(p.getProperty("ms_back_padding"));
		}
		if (p.containsKey("auto_vowel_combining")) {
			arrange.auto_vowel_combining = Boolean.valueOf(p.getProperty("auto_vowel_combining"));
		}
		if (p.containsKey("vowel_combining_volume")) {
			arrange.vowel_combining_volume = Double.valueOf(p.getProperty("vowel_combining_volume"));
		}
		if (p.containsKey("vibrato")) {
			arrange.vibrato = Boolean.valueOf(p.getProperty("vibrato"));
		}
		if (p.containsKey("ms_vibrato_offset")) {
			arrange.ms_vibrato_offset = Short.valueOf(p.getProperty("ms_vibrato_offset"));
		}
		if (p.containsKey("ms_vibrato_width")) {
			arrange.ms_vibrato_width = Short.valueOf(p.getProperty("ms_vibrato_width"));
		}
		if (p.containsKey("pitch_vibrato")) {
			arrange.pitch_vibrato = Double.valueOf(p.getProperty("pitch_vibrato"));
		}
		if (p.containsKey("overshoot")) {
			arrange.overshoot = Boolean.valueOf(p.getProperty("overshoot"));
		}
		if (p.containsKey("ms_overshoot")) {
			arrange.ms_overshoot = Short.valueOf(p.getProperty("ms_overshoot"));
		}
		if (p.containsKey("pitch_overshoot")) {
			arrange.pitch_overshoot = Double.valueOf(p.getProperty("pitch_overshoot"));
		}
		if (p.containsKey("preparation")) {
			arrange.preparation = Boolean.valueOf(p.getProperty("preparation"));
		}
		if (p.containsKey("ms_preparation")) {
			arrange.ms_preparation = Short.valueOf(p.getProperty("ms_preparation"));
		}
		if (p.containsKey("pitch_preparation")) {
			arrange.pitch_preparation = Double.valueOf(p.getProperty("pitch_preparation"));
		}
		if (p.containsKey("finefluctuation")) {
			arrange.finefluctuation = Boolean.valueOf(p.getProperty("finefluctuation"));
		}
		if (p.containsKey("finefluctuation_deviation")) {
			arrange.finefluctuation_deviation = Double.valueOf(p.getProperty("finefluctuation_deviation"));
		}
		if (p.containsKey("target_rms")) {
			unitWaveformContainer.target_rms = Double.valueOf(p.getProperty("target_rms"));
		}
		if (p.containsKey("num_lobes")) {
			unitWaveformContainer.num_lobes = Short.valueOf(p.getProperty("num_lobes"));
		}
		if (p.containsKey("uwc_normalize")) {
			unitWaveformContainer.uwc_normalize = Boolean.valueOf(p.getProperty("uwc_normalize"));
		}
		if (p.containsKey("pitch_default")) {
			pitchmark.default_pitch = Long.valueOf(p.getProperty("pitch_default"));
		}
		if (p.containsKey("pitch_margin")) {
			pitchmark.pitch_margin = Short.valueOf(p.getProperty("pitch_margin"));
		}
		if (p.containsKey("xcorr_threshold")) {
			pitchmark.xcorr_threshold = Double.valueOf(p.getProperty("xcorr_threshold"));
		}
		if (p.containsKey("stretch_self_fade")) {
			overlap.stretch_self_fade = Boolean.valueOf(p.getProperty("stretch_self_fade"));
		}
		if (p.containsKey("ms_self_fade")) {
			overlap.ms_self_fade = Long.valueOf(p.getProperty("ms_self_fade"));
		}
		if (p.containsKey("interpolation")) {
			overlap.interpolation = Boolean.valueOf(p.getProperty("interpolation"));
		}
		if (p.containsKey("overlap_normalize")) {
			overlap.overlap_normalize = Boolean.valueOf(p.getProperty("overlap_normalize"));
		}
		if (p.containsKey("window_modification")) {
			overlap.window_modification = Boolean.valueOf(p.getProperty("window_modification"));
		}
	}

	public void save() throws IOException {
		save(pathNakloidIni);
	}

	public void save(Path path) throws IOException {
		if (!Files.exists(path)) {
			Files.createFile(path);
		}
		try (BufferedWriter bw = Files.newBufferedWriter(path, Charset.forName("Shift_JIS"))) {
			PrintWriter pw = new PrintWriter(bw);
			pw.println("[Input]");
			pw.println("path_input_score=" + input.path_input_score);
			if (input.score_mode == ScoreMode.score_mode_smf) {
				pw.println("score_mode=smf");
				pw.println("track=" + input.track);
				pw.println("path_lyrics=" + input.path_lyrics);
			} else if (input.score_mode == ScoreMode.score_mode_ust) {
				pw.println("score_mode=ust");
			} else {
				pw.println("score_mode=nak");
			}
			pw.println("path_input_pitches=" + ((input.path_input_pitches==null)?"":input.path_input_pitches));
			if (input.pitches_mode == PitchesMode.pitches_mode_pit) {
				pw.println("pitches_mode=pit");
			} else if (input.pitches_mode == PitchesMode.pitches_mode_lf0) {
				pw.println("pitches_mode=lf0");
				pw.println("path_input_pitches=" + input.pitch_frame_length);
			} else {
				pw.println("pitches_mode=none");
			}
			pw.println("path_singer=" + input.path_singer);
			pw.println("path_prefix_map=" + ((input.path_prefix_map==null)?"":input.path_prefix_map));
			pw.println("[Output]");
			pw.println("path_song=" + ((output.path_song==null)?"":output.path_song));
			pw.println("path_output_score=" + ((output.path_output_score==null)?"":output.path_output_score));
			pw.println("path_output_pitches=" + ((output.path_output_pitches==null)?"":output.path_output_pitches));
			pw.println("ms_margin=" + output.ms_margin);
			pw.println("max_volume=" + output.max_volume);
			pw.println("compressor=" + output.compressor);
			pw.println("compressor_threshold=" + output.compressor_threshold);
			pw.println("compressor_ratio=" + output.compressor_ratio);
			pw.println("print_log=" + output.print_log);
			pw.println("print_debug=" + output.print_debug);
			pw.println("[VocalLibrary]");
			pw.println("use_pmp_cache=" + vocalLibrary.use_pmp_cache);
			pw.println("use_uwc_cache=" + vocalLibrary.use_uwc_cache);
			pw.println("[Note]");
			pw.println("ms_front_padding=" + note.ms_front_padding);
			pw.println("ms_back_padding=" + note.ms_back_padding);
			pw.println("[Arrange]");
			pw.println("auto_vowel_combining=" + arrange.auto_vowel_combining);
			pw.println("vowel_combining_volume=" + arrange.vowel_combining_volume);
			pw.println("vibrato=" + arrange.vibrato);
			pw.println("ms_vibrato_offset=" + arrange.ms_vibrato_offset);
			pw.println("ms_vibrato_width=" + arrange.ms_vibrato_width);
			pw.println("pitch_vibrato=" + arrange.pitch_vibrato);
			pw.println("overshoot=" + arrange.overshoot);
			pw.println("ms_overshoot=" + arrange.ms_overshoot);
			pw.println("pitch_overshoot=" + arrange.pitch_overshoot);
			pw.println("preparation=" + arrange.preparation);
			pw.println("ms_preparation=" + arrange.ms_preparation);
			pw.println("pitch_preparation=" + arrange.pitch_preparation);
			pw.println("finefluctuation=" + arrange.finefluctuation);
			pw.println("finefluctuation_deviation=" + arrange.finefluctuation_deviation);
			pw.println("[UnitWaveformContainer]");
			pw.println("target_rms=" + unitWaveformContainer.target_rms);
			pw.println("num_lobes=" + unitWaveformContainer.num_lobes);
			pw.println("uwc_normalize=" + unitWaveformContainer.uwc_normalize);
			pw.println("[Pitchmark]");
			pw.println("default_pitch=" + pitchmark.default_pitch);
			pw.println("pitch_margin=" + pitchmark.pitch_margin);
			pw.println("xcorr_threshold=" + pitchmark.xcorr_threshold);
			pw.println("[Overlap]");
			pw.println("stretch_self_fade=" + overlap.stretch_self_fade);
			pw.println("ms_self_fade=" + overlap.ms_self_fade);
			pw.println("interpolation=" + overlap.interpolation);
			pw.println("overlap_normalize=" + overlap.overlap_normalize);
			pw.println("window_modification=" + overlap.window_modification);
			pw.flush();
			pw.close();
		}
	}
}
