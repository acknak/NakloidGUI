package nak.nakloidGUI.models;

import java.util.HashMap;

final public class PronunciationAlias {
	private static final HashMap<String, String> data_vowel2pron = new HashMap<String, String>(){
		{put("a", "あ");}{put("i", "い");}{put("u", "う");}{put("e", "え");}{put("o", "お");}{put("n", "ん");}
	};
	private static final HashMap<String, String> data_pron2vowel = new HashMap<String, String>(){
		{put("あ", "a");}{put("い", "i");}{put("う", "u");}{put("え", "e");}{put("お", "o");}
		{put("ぁ", "a");}{put("ぃ", "i");}{put("ぅ", "u");}{put("ぇ", "e");}{put("ぉ", "o");}
		{put("か", "a");}{put("き", "i");}{put("く", "u");}{put("け", "e");}{put("こ", "o");}
		{put("が", "a");}{put("ぎ", "i");}{put("ぐ", "u");}{put("げ", "e");}{put("ご", "o");}
		{put("さ", "a");}{put("し", "i");}{put("す", "u");}{put("せ", "e");}{put("そ", "o");}
		{put("ざ", "a");}{put("じ", "i");}{put("ず", "u");}{put("ぜ", "e");}{put("ぞ", "o");}
		{put("た", "a");}{put("ち", "i");}{put("つ", "u");}{put("て", "e");}{put("と", "o");}
		{put("だ", "a");}{put("ぢ", "i");}{put("づ", "u");}{put("で", "e");}{put("ど", "o");}
		{put("な", "a");}{put("に", "i");}{put("ぬ", "u");}{put("ね", "e");}{put("の", "o");}
		{put("は", "a");}{put("ひ", "i");}{put("ふ", "u");}{put("へ", "e");}{put("ほ", "o");}
		{put("ば", "a");}{put("び", "i");}{put("ぶ", "u");}{put("べ", "e");}{put("ぼ", "o");}
		{put("ぱ", "a");}{put("ぴ", "i");}{put("ぷ", "u");}{put("ぺ", "e");}{put("ぽ", "o");}
		{put("ま", "a");}{put("み", "i");}{put("む", "u");}{put("め", "e");}{put("も", "o");}
		{put("や", "a");}{put("ゆ", "u");}{put("よ", "o");}{put("ゃ", "a");}{put("ゅ", "u");}{put("ょ", "o");}
		{put("ら", "a");}{put("り", "i");}{put("る", "u");}{put("れ", "e");}{put("ろ", "o");}
		{put("わ", "a");}{put("を", "o");}{put("ん", "n");}{put("ゐ", "i");}{put("ゑ", "e");}
		{put("ア", "a");}{put("イ", "i");}{put("ウ", "u");}{put("エ", "e");}{put("オ", "o");}{put("ヴ", "u");}
		{put("ァ", "a");}{put("ィ", "i");}{put("ゥ", "u");}{put("ェ", "e");}{put("ォ", "o");}
		{put("カ", "a");}{put("キ", "i");}{put("ク", "u");}{put("ケ", "e");}{put("コ", "o");}
		{put("ガ", "a");}{put("ギ", "i");}{put("グ", "u");}{put("ゲ", "e");}{put("ゴ", "o");}
		{put("サ", "a");}{put("シ", "i");}{put("ス", "u");}{put("セ", "e");}{put("ソ", "o");}
		{put("ザ", "a");}{put("ジ", "i");}{put("ズ", "u");}{put("ゼ", "e");}{put("ゾ", "o");}
		{put("タ", "a");}{put("チ", "i");}{put("ツ", "u");}{put("テ", "e");}{put("ト", "o");}
		{put("ダ", "a");}{put("ヂ", "i");}{put("ヅ", "u");}{put("デ", "e");}{put("ド", "o");}
		{put("ナ", "a");}{put("ニ", "i");}{put("ヌ", "u");}{put("ネ", "e");}{put("ノ", "o");}
		{put("ハ", "a");}{put("ヒ", "i");}{put("フ", "u");}{put("ヘ", "e");}{put("ホ", "o");}
		{put("バ", "a");}{put("ビ", "i");}{put("ブ", "u");}{put("ベ", "e");}{put("ボ", "o");}
		{put("パ", "a");}{put("ピ", "i");}{put("プ", "u");}{put("ペ", "e");}{put("ポ", "o");}
		{put("マ", "a");}{put("ミ", "i");}{put("ム", "u");}{put("メ", "e");}{put("モ", "o");}
		{put("ヤ", "a");}{put("ユ", "u");}{put("ヨ", "o");}{put("ャ", "a");}{put("ュ", "u");}{put("ｮ", "o");}
		{put("ラ", "a");}{put("リ", "i");}{put("ル", "u");}{put("レ", "e");}{put("ロ", "o");}
		{put("ワ", "a");}{put("ヲ", "o");}{put("ン", "n");}{put("ヰ", "i");}{put("ヱ", "e");}
	};
	private String prefix = "";
	private String pron = "";
	private String suffix = "";

	public PronunciationAlias (String alias) {
		pron = alias;
		int pos_prefix = alias.indexOf(" ");
		if (pron.length()>1 && pos_prefix != -1) {
			prefix = pron.substring(0, pos_prefix+1);
			pron = pron.substring(pos_prefix+1, pron.length());
		}
		if (pron.length() > 1) {
			for (int i=0; i<pron.length(); i++) {
				if (data_pron2vowel.containsKey(pron.substring(pron.length()-1-i,pron.length()-i))) {
					suffix = pron.substring(pron.length()-i);
					pron = pron.substring(0, pron.length()-i);
					break;
				}
			}
		}
	}

	public boolean checkVCV() {
		return (!prefix.isEmpty() && prefix.indexOf(" ")!=-1 && !prefix.startsWith("-"));
	}

	public boolean checkVowelPron() {
		return ((prefix.isEmpty() || prefix.equals("- ") || prefix.equals("-")) && isVowelPron(pron));
	}

	public String getAliasString() {
		return prefix + pron + suffix;
	}

	public String getPron() {
		return pron;
	}

	public String getPronVowel() {
		String tmp_pron = pron;
		if (tmp_pron.length() > 1) {
			tmp_pron = pron.substring(pron.length()-2, 1);
		}
		return pron2vowel(tmp_pron);
	}

	public String getPrefix() {
		return prefix;
	}

	public String getPrefixVowel() {
		if (prefix.equals("* ")) {
			return getPronVowel();
		}
		if (prefix.length() > 1) {
			return prefix.substring(prefix.length()-2, 1);
		}
		return "";
	}

	public String getPrefixPron() {
		return vowel2pron(getPrefixVowel());
	}

	public String getSuffix() {
		return suffix;
	}

	public boolean isVowel(String vowel) {
		return data_vowel2pron.containsKey(vowel);
	}

	public boolean isVowelPron(String pron) {
		return data_vowel2pron.containsValue(pron);
	}

	public boolean isPron(String pron)
	{
		return data_pron2vowel.containsKey(pron);
	}

	public static String vowel2pron(String vowel)
	{
		if (data_vowel2pron.containsKey(vowel)) {
			return data_vowel2pron.get(vowel);
		}
		return "";
	}

	public static String pron2vowel(String pron)
	{
		if(data_pron2vowel.containsKey(pron)) {
			return data_pron2vowel.get(pron);
		}
		return "";
	}
}
