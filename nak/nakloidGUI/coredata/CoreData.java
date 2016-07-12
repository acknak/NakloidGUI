package nak.nakloidGUI.coredata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.models.Note;
import nak.nakloidGUI.models.Pitches;
import nak.nakloidGUI.models.PronunciationAlias;
import nak.nakloidGUI.models.VocalInfo;
import nak.nakloidGUI.models.Voice;
import nak.nakloidGUI.models.Waveform;

public class CoreData {
	public NakloidIni nakloidIni;
	private Vocal vocal;
	private Pitches pitches;
	private Score score;
	private Waveform wfSong;
	private boolean isSaved = true;
	final public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
	private Path pathSynthStdout, pathAllPmpStdout;

	private List<CoreDataSubscriber> coreDataSubscribers = new ArrayList<CoreDataSubscriber>();
	public interface CoreDataSubscriber {
		public void updateScore();
		public void updatePitches();
		public void updateVocal();
		public void updateSongWaveform();
	}
	public void addSubscribers(CoreDataSubscriber coreDataSubscriber) {
		coreDataSubscribers.add(coreDataSubscriber);
	}
	public void removeSubscribers(CoreDataSubscriber coreDataSubscriber) {
		coreDataSubscribers.remove(coreDataSubscriber);
	}

	public interface CoreDataSynthesisListener {
		public void synthesisFinished();
	}

	static public class Builder {
		public NakloidIni nakloidIni;
		public Vocal otoIni;
		public Pitches pitches;
		public Score score;
		public Waveform wfSong;

		public Builder() {
			nakloidIni = new NakloidIni();
		}
		public Builder loadFromDefaultPath() throws IOException {
			nakloidIni = new NakloidIni(NakloidIni.pathNakloidIni);
			otoIni = new Vocal(nakloidIni.input.path_singer);
			score = new Score(nakloidIni.input.path_input_score);
			pitches = new Pitches.Builder(nakloidIni.input.path_input_pitches).build();
			wfSong = new Waveform(nakloidIni.output.path_song);
			return this;
		}
		public Builder loadNakloidIni() throws IOException {
			nakloidIni.load(NakloidIni.pathNakloidIni);
			return this;
		}
		public Builder loadNakloidIni(Path path) throws IOException {
			nakloidIni.load(path);
			return this;
		}
		public Builder loadOtoIni() throws IOException {
			otoIni = new Vocal(nakloidIni.input.path_singer);
			return this;
		}
		public Builder loadOtoIni(Path path) throws IOException {
			otoIni = new Vocal(path);
			return this;
		}
		public Builder loadPitches() throws IOException {
			pitches = new Pitches.Builder(nakloidIni.input.path_input_pitches).build();
			return this;
		}
		public Builder loadPitches(Path path) throws IOException {
			nakloidIni.input.path_input_pitches = path;
			return loadPitches();
		}
		public Builder loadScore() throws IOException {
			score = new Score(nakloidIni.input.path_input_score);
			return this;
		}
		public Builder loadScore(Path path) throws IOException {
			nakloidIni.input.path_input_score = path;
			return loadScore();
		}
		public Builder loadSongWaveform() {
			wfSong = new Waveform(nakloidIni.output.path_song);
			return this;
		}
		public Builder loadSongWaveform(Path path) {
			nakloidIni.output.path_song = path;
			return loadSongWaveform();
		}
		public CoreData build() {
			if (otoIni == null) {
				otoIni = new Vocal();
			}
			if (score == null) {
				score = new Score();
			}
			return new CoreData(this);
		}
	}

	private CoreData(Builder builder) {
		this.nakloidIni = builder.nakloidIni;
		this.vocal = builder.otoIni;
		this.pitches = builder.pitches;
		this.score = builder.score;
		this.wfSong = builder.wfSong;
	}

	public void reloadPreference() throws IOException {
		long oldMargin = nakloidIni.output.ms_margin;
		long newMargin = NakloidGUI.preferenceStore.getLong("ini.output.ms_margin");
		if (oldMargin > newMargin) {
			pitches = new Pitches.Builder(pitches).stretch((int)(newMargin-oldMargin)).build();
		} else if (oldMargin < newMargin) {
			pitches = new Pitches.Builder(pitches).shorten((int)(oldMargin-newMargin)).build();
		}
		nakloidIni = new NakloidIni();
		if (oldMargin != newMargin) {
			pitches.save(nakloidIni.input.path_input_pitches);
		}
		coreDataSubscribers.stream().forEach(CoreDataSubscriber::updateScore);
		coreDataSubscribers.stream().forEach(CoreDataSubscriber::updatePitches);
	}

	public void reloadScoreAndPitches() throws IOException {
		this.nakloidIni = new NakloidIni();
		if (nakloidIni.input.path_input_score!=null && nakloidIni.input.path_input_score.toFile().isFile()) {
			score = new Score(nakloidIni.input.path_input_score);
			coreDataSubscribers.stream().forEach(CoreDataSubscriber::updateScore);
		}
		if (nakloidIni.input.path_input_pitches!=null && nakloidIni.input.path_input_pitches.toFile().isFile()) {
			pitches = new Pitches.Builder(nakloidIni.input.path_input_pitches).build();
			coreDataSubscribers.stream().forEach(CoreDataSubscriber::updatePitches);
		}
		if (nakloidIni.output.path_song!=null && nakloidIni.output.path_song.toFile().isFile()) {
			closeSongWaveform();
		}
		isSaved = false;
	}

	public Path getVocalPath() {
		return vocal.getPath();
	}

	public Voice getVoice(String pron) {
		return vocal.getVoice(pron);
	}

	public Voice getVoice(PronunciationAlias pron) {
		return vocal.getVoice(pron.getAliasString());
	}

	public Voice[] getVoicesArray() {
		return vocal.getVoicesArray();
	}

	public int getVoicesSize() {
		return vocal.getVoicesSize();
	}

	public void setVoice(Voice voice) {
		vocal.setVoice(voice);
		score.resetNotesBorder(vocal);
		coreDataSubscribers.stream().forEach(CoreDataSubscriber::updateVocal);
	}

	public void saveVoice(Voice voice) throws IOException {
		vocal.save(voice);
	}

	public void saveVocal() throws IOException {
		vocal.save();
	}

	public void saveVocal(Path path) throws IOException {
		vocal.save(path);
	}

	public void reloadVocal() throws IOException {
		if (nakloidIni.input.path_singer!=null && nakloidIni.input.path_singer.toFile().isDirectory()) {
			vocal = new Vocal(nakloidIni.input.path_singer);
			coreDataSubscribers.stream().forEach(CoreDataSubscriber::updateVocal);
			wfSong = null;
			coreDataSubscribers.stream().forEach(CoreDataSubscriber::updateSongWaveform);
			score.resetNotesBorder(vocal);
			coreDataSubscribers.stream().forEach(CoreDataSubscriber::updateScore);
		}
	}

	public VocalInfo getVocalInfo() {
		return vocal.getVocalInfo();
	}

	public Pitches getPitches() {
		return pitches;
	}

	public void setPitches(Pitches pitches) {
		this.pitches = pitches;
		isSaved = false;
		coreDataSubscribers.stream().forEach(CoreDataSubscriber::updatePitches);
	}

	public void reloadPitches() throws IOException {
		saveScore();
		isSaved = true;
		if (nakloidIni.input.path_input_pitches!=null && nakloidIni.input.path_input_pitches.toFile().isFile()) {
			this.pitches = new Pitches.Builder(nakloidIni.input.path_input_pitches).build();
			coreDataSubscribers.stream().forEach(CoreDataSubscriber::updatePitches);
		}
	}

	public void replaceMidiNoteNumbers(List<Double> tmpNumbers, int from) {
		this.pitches = new Pitches.Builder(this.pitches)
				.replaceMidiNoteNumbers(tmpNumbers, from)
				.build();
		coreDataSubscribers.stream().forEach(CoreDataSubscriber::updatePitches);
	}

	public void savePitches() throws IOException {
		pitches.save();
	}

	public void savePitches(Path pathPitches) throws IOException {
		pitches.save(pathPitches);
	}

	public List<Note> getNotes() {
		return new ArrayList<Note>(score.getNotes());
	}

	public Note getNote(int id) {
		return score.getNote(id);
	}

	public Note getNewNote() {
		return new Note.Builder(score.getNewNoteId()).build();
	}

	public void addNote(Note note) {
		score.addNote(note);
		score.resetNotesBorder(vocal);
		isSaved = false;
		coreDataSubscribers.stream().forEach(CoreDataSubscriber::updateScore);
	}

	public void setNote(Note note) {
		score.setNote(note);
		score.resetNotesBorder(vocal);
		isSaved = false;
		coreDataSubscribers.stream().forEach(CoreDataSubscriber::updateScore);
	}

	public void removeNote(Note note) {
		score.removeNote(note);
		score.resetNotesBorder(vocal);
		isSaved = false;
		coreDataSubscribers.stream().forEach(CoreDataSubscriber::updateScore);
	}

	public void removeNote(int id) {
		score.removeNote(id);
		score.resetNotesBorder(vocal);
		isSaved = false;
		coreDataSubscribers.stream().forEach(CoreDataSubscriber::updateScore);
	}

	public Path getScorePath() {
		return score.getPath();
	}

	public void setScorePath(Path path) {
		score.setPath(path);
		score.resetNotesBorder(vocal);
		coreDataSubscribers.stream().forEach(CoreDataSubscriber::updateScore);
	}

	public String getAllLyrics() {
		return score.getAllLyrics();
	}

	public void setLyrics(List<PronunciationAlias> lyrics) {
		score.setLyrics(lyrics);
		score.resetNotesBorder(vocal);
		isSaved = false;
		coreDataSubscribers.stream().forEach(CoreDataSubscriber::updateScore);
	}

	public int getScoreLength() {
		return score.getScoreLength();
	}

	public void saveScore() throws JsonGenerationException, JsonMappingException, IOException {
		if (!isSaved) {
			score.save(nakloidIni.input.path_input_score);
		}
		isSaved = true;
	}

	public void saveScore(Path pathScore) throws JsonGenerationException, JsonMappingException, IOException {
		if (!isSaved) {
			score.save(pathScore);
		}
		isSaved = true;
	}

	public Waveform getSongWaveform() {
		return wfSong;
	}

	public void reloadSongWaveform() {
		if (nakloidIni.output.path_song!=null && nakloidIni.output.path_song.toFile().isFile()) {
			if (wfSong != null) {
				wfSong.close();
			}
			wfSong = new Waveform(nakloidIni.output.path_song);
			coreDataSubscribers.stream().forEach(CoreDataSubscriber::updateSongWaveform);
		}
	}

	public void closeSongWaveform() {
		if (wfSong != null) {
			wfSong.close();
			wfSong = null;
			coreDataSubscribers.stream().forEach(CoreDataSubscriber::updateSongWaveform);
		}
	}

	public boolean isSaved() {
		return isSaved;
	}

	public void synthesize(CoreDataSynthesisListener cdsl) throws IOException, InterruptedException {
		nakloidIni.save();
		System.out.println(sdf.format(System.currentTimeMillis()));
		pathSynthStdout = Files.createTempFile(Paths.get("temporary"), "CoreData.synthesize.", "");
		ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "Nakloid.exe", "-v", ">", pathSynthStdout.toAbsolutePath().toString(), "2>&1");
		pb.redirectErrorStream(true);
		Process process = pb.start();
		BufferedReader br = Files.newBufferedReader(pathSynthStdout, Charset.forName("Shift_JIS"));
		Display.getCurrent().syncExec(new Runnable() {
			@Override
			public void run() {
				if (Files.exists(pathSynthStdout)) {
					br.lines().forEach(System.out::println);
				}
				if (process.isAlive()) {
					Display.getCurrent().timerExec(50, this);
				} else {
					try {
						br.close();
						Files.deleteIfExists(pathSynthStdout);
					} catch (IOException e) {
					} finally {
						if (cdsl != null) {
							cdsl.synthesisFinished();
						}
					}
				}
			}
		});
	}

	public void makePmp(String pron) throws IOException {
		nakloidIni.save();
		ProcessBuilder pb = new ProcessBuilder("Nakloid.exe", "-c", "\""+pron+"\"", "-pmp");
		pb.redirectErrorStream(true);
		Process process = pb.start();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), "Shift_JIS"))) {
			String str;
			while((str = br.readLine()) != null) {
				System.out.println(str);
			}
		}
	}

	public void makeAllPmp(CoreDataSynthesisListener cdsl) throws IOException {
		nakloidIni.save();
		System.out.println(sdf.format(System.currentTimeMillis()));
		pathAllPmpStdout = Files.createTempFile(Paths.get("temporary"), "CoreData.makeAllPmp.", "");
		ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "Nakloid.exe", "-ac", "-pmp", ">", pathAllPmpStdout.toAbsolutePath().toString(), "2>&1");
		pb.redirectErrorStream(true);
		Process process = pb.start();
		BufferedReader br = Files.newBufferedReader(pathAllPmpStdout, Charset.forName("Shift_JIS"));
		Display.getCurrent().syncExec(new Runnable() {
			@Override
			public void run() {
				if (Files.exists(pathAllPmpStdout)) {
					br.lines().forEach(System.out::println);
				}
				if (process.isAlive()) {
					Display.getCurrent().timerExec(50, this);
				} else {
					try {
						br.close();
						Files.deleteIfExists(pathAllPmpStdout);
					} catch (IOException e) {
					} finally {
						if (cdsl != null) {
							cdsl.synthesisFinished();
						}
					}
				}
			}
		});
	}
}
