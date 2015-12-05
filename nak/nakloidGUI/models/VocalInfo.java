package nak.nakloidGUI.models;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import java.util.stream.Stream;

final public class VocalInfo implements Cloneable {
	private String name = "";
	private Path pathImage = null;
	private Path pathSample = null;
	private String author = "";
	private String web = "";
	private String mainText = "";
	private Path pathReadme;

	public VocalInfo(Path path) throws IOException {
		try (Stream<String> stream = Files.lines(path, Charset.forName("Shift_JIS"))) {
			stream.forEachOrdered(line -> {
				StringTokenizer st = new StringTokenizer(line, "=");
				if (st.countTokens() > 1) {
					String strLeft = st.nextToken();
					if (strLeft.equals("name")) {
						name = st.nextToken();
					} else if (strLeft.equals("image")) {
						pathImage = Paths.get(path.getParent().toString(), st.nextToken());
					} else if (strLeft.equals("sample")) {
						pathSample = Paths.get(path.getParent().toString(), st.nextToken());
					} else if (strLeft.equals("author")) {
						author = st.nextToken();
					} else if (strLeft.equals("web")) {
						web = st.nextToken();
					} else {
						mainText += line + "\n";
					}
				} else {
					mainText += line + "\n";
				}
			});
		}
		Path tmp = Paths.get(path.getParent().toString(), "readme.txt");
		if (Files.exists(tmp)) {
			pathReadme = tmp;
		}
	}

	public boolean hasName() {
		return name!=null && !name.isEmpty();
	}

	public String getName() {
		return name;
	}

	public boolean hasPathImage() {
		return pathImage!=null && Files.exists(pathImage);
	}

	public Path getPathImage() {
		return pathImage;
	}

	public boolean hasPathSample() {
		return pathSample!=null && Files.exists(pathSample);
	}

	public Path getPathSample() {
		return pathSample;
	}

	public boolean hasAuthor() {
		return author!=null && !author.isEmpty();
	}

	public String getAuthor() {
		return author;
	}

	public boolean hasWeb() {
		return web!=null && !web.isEmpty();
	}

	public String getWeb() {
		return web;
	}

	public boolean hasText() {
		return (name!=null||author!=null||web!=null||mainText!=null) && !getText().isEmpty();
	}

	public String getText() {
		StringBuilder sb = new StringBuilder();
		if (name!=null && !name.isEmpty()) {
			sb.append("音源名：");
			sb.append(name);
			sb.append("\n");
		}
		if (author!=null && !author.isEmpty()) {
			sb.append("作者：");
			sb.append(author);
			sb.append("\n");
		}
		if (web!=null && !web.isEmpty()) {
			sb.append("web：");
			sb.append(web);
			sb.append("\n");
		}
		sb.append(mainText);
		return sb.toString();
	}

	public boolean hasReadme() {
		return pathReadme != null;
	}

	public Path getPathReadme() {
		return pathReadme;
	}
}
