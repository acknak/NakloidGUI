package nak.nakloidGUI;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Display;

import nak.nakloidGUI.gui.MainWindow;

public class NakloidGUI {
	public static PreferenceStore preferenceStore;

	public static void main(String[] args) throws IOException {
		File lockFile = new File("lock");
		try (FileChannel fc = FileChannel.open(lockFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				FileLock lock = fc.tryLock()) {
			if (lock == null) {
				throw new RuntimeException("Nakloid GUI does NOT support multi launch.");
			}
			try (Stream<Path> pathStream = Files.walk(Paths.get("temporary"))) {
				pathStream.filter(p->!p.getFileName().toString().equals("score.nak"))
					.filter(p->!p.getFileName().toString().equals("pitches.pit"))
					.filter(p->!p.getFileName().toString().equals("song.wav"))
					.forEach(p->{try{Files.delete(p);}catch(Exception e){}});
			} catch (IOException e) {}
			initializePreferenceValue();
			MainWindow mainWindow = new MainWindow();
			mainWindow.setBlockOnOpen(true);
			mainWindow.open();
			Display.getCurrent().dispose();
		}
	}

	public static void initializePreferenceValue() {
		preferenceStore = new PreferenceStore("nakloidGUI.properties");
		preferenceStore.setDefault("workspace.path_nar", "");
		preferenceStore.setDefault("ini.input.path_input_score", "./temporary/score.nak");
		preferenceStore.setDefault("ini.input.score_mode", "nak");
		preferenceStore.setDefault("ini.input.track", 1);
		preferenceStore.setDefault("ini.input.path_lyrics", "./temporary/lyrics.txt");
		preferenceStore.setDefault("ini.input.path_input_pitches", "./temporary/pitches.pit");
		preferenceStore.setDefault("ini.input.pitches_mode", "pit");
		preferenceStore.setDefault("ini.input.pitch_frame_length", 5);
		preferenceStore.setDefault("ini.input.path_singer", "./vocal/");
		preferenceStore.setDefault("ini.input.path_prefix_map", "/temporary/prefix.map");
		preferenceStore.setDefault("ini.output.path_song", "./temporary/song.wav");
		preferenceStore.setDefault("ini.output.path_output_score", "./temporary/score.nak");
		preferenceStore.setDefault("ini.output.path_output_pitches", "./temporary/pitches.pit");
		preferenceStore.setDefault("ini.output.max_volume", 0.9);
		preferenceStore.setDefault("ini.output.compressor", false);
		preferenceStore.setDefault("ini.output.compressor_threshold", -20.0);
		preferenceStore.setDefault("ini.output.compressor_ratio", 2.5);
		preferenceStore.setDefault("ini.output.print_debug", false);
		preferenceStore.setDefault("ini.vocal_library.use_pmp_cache", true);
		preferenceStore.setDefault("ini.vocal_library.use_uwc_cache", true);
		preferenceStore.setDefault("ini.note.ms_front_padding", 5);
		preferenceStore.setDefault("ini.note.ms_back_padding", 35);
		preferenceStore.setDefault("ini.arrange.auto_vowel_combining", true);
		preferenceStore.setDefault("ini.arrange.vowel_combining_volume", 0.8);
		preferenceStore.setDefault("ini.arrange.vibrato", true);
		preferenceStore.setDefault("ini.arrange.ms_vibrato_offset", 200);
		preferenceStore.setDefault("ini.arrange.ms_vibrato_width", 150);
		preferenceStore.setDefault("ini.arrange.pitch_vibrato", 10.0);
		preferenceStore.setDefault("ini.arrange.overshoot", true);
		preferenceStore.setDefault("ini.arrange.ms_overshoot", 20);
		preferenceStore.setDefault("ini.arrange.pitch_overshoot", 30.0);
		preferenceStore.setDefault("ini.arrange.preparation", true);
		preferenceStore.setDefault("ini.arrange.ms_preparation", 20);
		preferenceStore.setDefault("ini.arrange.pitch_preparation", 30.0);
		preferenceStore.setDefault("ini.arrange.pitch_vibrato", 10.0);
		preferenceStore.setDefault("ini.arrange.finefluctuation", false);
		preferenceStore.setDefault("ini.arrange.finefluctuation_deviation", 0.5);
		preferenceStore.setDefault("ini.unit_waveform_container.target_rms", 0.05);
		preferenceStore.setDefault("ini.unit_waveform_container.num_lobes", 1);
		preferenceStore.setDefault("ini.unit_waveform_container.uwc_normalize", true);
		preferenceStore.setDefault("ini.pitchmark.default_pitch", 260);
		preferenceStore.setDefault("ini.pitchmark.pitch_margin", 5);
		preferenceStore.setDefault("ini.pitchmark.xcorr_threshold", 0.95);
		preferenceStore.setDefault("ini.overlap.self_fade_stretch_scale", 1.0);
		preferenceStore.setDefault("ini.overlap.ms_self_fade", 200);
		preferenceStore.setDefault("ini.overlap.interpolation", true);
		preferenceStore.setDefault("ini.overlap.overlap_normalize", true);
		preferenceStore.setDefault("ini.overlap.window_modification", true);
		preferenceStore.setDefault("gui.mainWindow.vocalInfoDisplayMode", "tooltip");
		preferenceStore.setDefault("gui.mainWindow.headerHeight", 100);
		preferenceStore.setDefault("gui.mainWindow.keyboardWidth", 106);
		preferenceStore.setDefault("gui.mainWindow.numMidiNoteUpperLimit", 90);
		preferenceStore.setDefault("gui.mainWindow.numMidiNoteLowerLimit", 41);
		preferenceStore.setDefault("gui.mainWindow.baseNoteHeight", 20);
		preferenceStore.setDefault("gui.mainWindow.baseMsByPixel", 10);
		preferenceStore.setDefault("gui.noteOption.volumeViewHeight", 150);
		preferenceStore.setDefault("gui.voiceOption.waveformGraphHeight", 150);
	}
}
