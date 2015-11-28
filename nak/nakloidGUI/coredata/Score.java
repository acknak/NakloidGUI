package nak.nakloidGUI.coredata;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import nak.nakloidGUI.models.Note;
import nak.nakloidGUI.models.PronunciationAlias;

public class Score {
	@JsonIgnore
	private List<Note> notes = new ArrayList<Note>();
	@JsonIgnore
	private Path path;

	public Score() {}

	public Score(Path path) throws IOException {
		this.path = path;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		try (InputStream is = Files.newInputStream(this.path)) {
			Score score = mapper.readValue(is, Score.class);
			notes = score.notes;
		}
	}

	@JsonIgnore
	public int getScoreLength() {
		Optional<Note> maxNote = notes.stream().max(Comparator.comparing(Note::getEnd));
		if (maxNote.isPresent()) {
			return maxNote.get().getEnd();
		}
		return 0;
	}

	@JsonProperty("Notes")
	public List<Note> getNotes() {
		return new ArrayList<Note>(notes);
	}

	@JsonIgnore
	public Note getNote(int id) {
		return notes.stream().filter(n->n.getId()==id).findFirst().orElse(null);
	}

	@JsonProperty("Notes")
	public void setNotes(Note[] notes) {
		this.notes = new ArrayList<Note>();
		this.notes.addAll(Arrays.asList(notes));
	}

	@JsonIgnore
	public void setNote(Note note) {
		if (notes.stream().anyMatch(n->n.getId()==note.getId())) {
			notes = notes.stream().map(n->n.getId()==note.getId()?note:n).collect(Collectors.toList());
		} else {
			addNote(note);
		}
	}

	@JsonIgnore
	public void addNote(Note note) {
		notes.add(note);
	}

	@JsonIgnore
	public void removeNote(Note note) {
		notes = notes.stream().filter(n->n!=note).collect(Collectors.toList());
	}

	@JsonIgnore
	public void removeNote(int id) {
		notes = notes.stream().filter(n->n.getId()!=id).collect(Collectors.toList());
	}

	@JsonIgnore
	public int getNewNoteId() {
		return notes.stream().mapToInt(n->n.getId()).max().orElse(0) + 1;
	}

	@JsonIgnore
	public Path getPath() {
		return path;
	}

	@JsonIgnore
	public void setPath(Path path) {
		this.path = path;
	}

	@JsonIgnore
	public String getAllLyrics() {
		return notes.stream().sorted().map(Note::getPronunciationAliasString).collect(Collectors.joining(","));
	}

	@JsonIgnore
	public void setLyrics(List<PronunciationAlias> lyrics) {
		for (int i=0; i<lyrics.size()&&i<notes.size(); i++) {
			notes.set(i, new Note.Builder(notes.get(i)).setPronunciationAlias(lyrics.get(i)).build());
		}
	}

	@JsonIgnore
	public void save(Path path) throws JsonGenerationException, JsonMappingException, IOException {
		Path pathSave = (path==null)?this.path:path;
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.configure(Feature.ESCAPE_NON_ASCII, true);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		try (BufferedWriter bw = Files.newBufferedWriter(pathSave)) {
			mapper.writeValue(bw, this);
		} catch (IOException e) {
			throw e;
		}
	}
}
