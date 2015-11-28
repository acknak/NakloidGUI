package nak.nakloidGUI.coredata;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nak.nakloidGUI.models.VocalInfo;
import nak.nakloidGUI.models.Voice;

public class Vocal {
	final static public Path vocalDirectoryPath = Paths.get("./vocal");
	private Path path;
	private Map<String, Voice> voices = new HashMap<String, Voice>();
	private VocalInfo vocalInfo;

	private class PronunciationAndFilenamePair {
		private String pronunciation;
		private String filename;
		public PronunciationAndFilenamePair(String tmp) {
			pronunciation = tmp;
			String[] pronCandidates = pronunciation.split("=");
			filename = pronCandidates[0];
			if (pronCandidates.length>1 && !pronCandidates[1].isEmpty()) {
				pronunciation = pronCandidates[1];
			} else {
				int point = pronCandidates[0].lastIndexOf(".");
				if (point != -1) {
					pronunciation = pronCandidates[0].substring(0, point);
				} else {
					pronunciation = pronCandidates[0];
				}
			}
		}
		public String getPronunciation() {
			return pronunciation;
		}
		public String getFilename() {
			return filename;
		}
	}

	public Vocal() {}

	public Vocal(Path pathOriginalDir) throws IOException {
		this.path = pathOriginalDir;
		try(Stream<Path> pathStream = Files.walk(pathOriginalDir)){
			pathStream.filter(p->p.getFileName().toString().equals("character.txt")).findFirst().ifPresent(p->{
				try {
					vocalInfo = new VocalInfo(p);
				} catch (Exception e) {}
			});
		}
		try(Stream<Path> pathStream = Files.walk(pathOriginalDir)){
			pathStream.filter(p->p.getFileName().toString().equals("oto.ini")).forEach(pathTmpOtoIni->{
				try (Stream<String> strStream = Files.lines(pathTmpOtoIni, Charset.forName("Shift_JIS"))) {
					strStream.forEach(s->{
						StringTokenizer st = new StringTokenizer(s, ",");
						if (st.hasMoreTokens()) {
							short offset=0, consonant=0, blank=0, preutterance=0, overlap=0;
							PronunciationAndFilenamePair pfp = new PronunciationAndFilenamePair(st.nextToken());
							if (st.hasMoreTokens()) {
								offset = (short)Double.parseDouble(st.nextToken());
							}
							if (st.hasMoreTokens()) {
								consonant = (short)Double.parseDouble(st.nextToken());
							}
							if (st.hasMoreTokens()) {
								blank = (short)Double.parseDouble(st.nextToken());
							}
							if (st.hasMoreTokens()) {
								preutterance = (short)Double.parseDouble(st.nextToken());
							}
							if (st.hasMoreTokens()) {
								overlap = (short)Double.parseDouble(st.nextToken());
							}
							voices.put(
								pfp.getPronunciation(),
								new Voice.Builder(pfp.getPronunciation())
									.otoParams(offset, overlap, preutterance, consonant, blank)
									.otoPaths(pfp.getFilename(), pathTmpOtoIni)
									.build()
								);
						}
					});
				} catch (IOException e) {}
			});
		}
	}

	public Voice getVoice(String pron) {
		return (voices.containsKey(pron))?voices.get(pron):null;
	}

	public Voice[] getVoicesArray() {
		return voices.values().stream().sorted().toArray(size->new Voice[size]);
	}

	public void setVoice(Voice voice) {
		voices.put(voice.getPronunciationString(), voice);
	}

	public Path getPath() {
		return path;
	}

	public VocalInfo getVocalInfo() {
		return vocalInfo;
	}

	public int getVoicesSize() {
		return voices.size();
	}

	public void save() throws IOException {
		List<Path> otoIniPaths = voices.values().stream()
				.map(v->v.getOtoIniPath())
				.distinct()
				.collect(Collectors.toList());
		Map<Path, StringBuilder> sbMap = new HashMap<Path, StringBuilder>();
		for(Path otoIniPath : otoIniPaths) {
			sbMap.put(otoIniPath, new StringBuilder());
		}
		for (Voice voice : voices.values()) {
			StringBuilder sb = sbMap.get(voice.getOtoIniPath());
			sb.append(voice.getWavPath().getFileName());
			sb.append("=");
			sb.append(voice.getPronunciationString());
			sb.append(",");
			sb.append(voice.getOffset());
			sb.append(",");
			sb.append(voice.getConsonant());
			sb.append(",");
			sb.append(voice.getBlank());
			sb.append(",");
			sb.append(voice.getPreutterance());
			sb.append(",");
			sb.append(voice.getPreutterance());
			sb.append("\n");
		}
		for(Path otoIniPath : otoIniPaths) {
			try (BufferedWriter bw = Files.newBufferedWriter(otoIniPath, Charset.forName("Shift_JIS"))) {
				bw.write(voices.get(otoIniPath).toString());
			} catch (IOException e) {
				throw e;
			}
		}
	}

	public void save(Voice voice) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (Stream<String> stream = Files.lines(voice.getOtoIniPath(), Charset.forName("Shift_JIS"))) {
			stream.forEach(s->{
				StringTokenizer st = new StringTokenizer(s, ",");
				if (st.hasMoreTokens()) {
					PronunciationAndFilenamePair pfp = new PronunciationAndFilenamePair(st.nextToken());
					if (voice.getPronunciationString().equals(pfp.getPronunciation()) && voice.getWavPath().getFileName().toString().equals(pfp.getFilename())) {
						sb.append(voice.getWavPath().getFileName());
						sb.append("=");
						sb.append(voice.getPronunciationString());
						sb.append(",");
						sb.append(voice.getOffset());
						sb.append(",");
						sb.append(voice.getConsonant());
						sb.append(",");
						sb.append(voice.getBlank());
						sb.append(",");
						sb.append(voice.getPreutterance());
						sb.append(",");
						sb.append(voice.getPreutterance());
						sb.append("\n");
					} else {
						sb.append(s);
						sb.append("\n");
					}
				}
			});
			setVoice(voice);
		} catch (IOException e) {
			throw e;
		}
		try (BufferedWriter bw = Files.newBufferedWriter(voice.getOtoIniPath(), Charset.forName("Shift_JIS"))) {
			bw.write(sb.toString());
		} catch (IOException e) {
			throw e;
		}
	}

	public void save(Path path) throws IOException {
		try (BufferedWriter bw = Files.newBufferedWriter(path, Charset.forName("Shift_JIS"))) {
			StringBuilder sb = new StringBuilder();
			for (Voice voice : voices.values()) {
				sb.append(voice.getWavPath().getFileName());
				sb.append("=");
				sb.append(voice.getPronunciationString());
				sb.append(",");
				sb.append(voice.getOffset());
				sb.append(",");
				sb.append(voice.getConsonant());
				sb.append(",");
				sb.append(voice.getBlank());
				sb.append(",");
				sb.append(voice.getPreutterance());
				sb.append(",");
				sb.append(voice.getPreutterance());
				sb.append("\n");
			}
			bw.write(sb.toString());
		} catch (IOException e) {
			throw e;
		}
	}
}
