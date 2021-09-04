package io.github.mezatsong.ladalja.tools;

public class Utils {

    public static boolean englishConsonant(char ch) {
        switch (Character.toLowerCase(ch)) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                return false;
            default:
                return true;
        }
    }

    public static String makePlural(String singularWord) {
        String pluralWord = "";
        int length = singularWord.length();
        String checker = singularWord.substring(0, singularWord.length() - 1);
        char lastLetter = singularWord.charAt(singularWord.length() - 1);

        if (length == 1) {
            return singularWord;
        }

        switch (lastLetter) {
			case 's':
			case 'x':
			case 'z':
				pluralWord = singularWord + "es";
				break;
			case 'h':
				if ((singularWord.charAt(singularWord.length() - 2) == 'c') || (singularWord.charAt(singularWord.length() - 2) == 's')) {
					pluralWord = singularWord + "es";
					break;
				}
			case 'f':
				if (englishConsonant(singularWord.charAt(singularWord.length() - 2))) {
					pluralWord = checker + "ves";
					break;
				}
			case 'y':
				if (englishConsonant(singularWord.charAt(singularWord.length() - 2))) {
					pluralWord = checker + "ies";
					break;
				}
			default:
				pluralWord = singularWord + "s";
				break;
		}
		
        return pluralWord;
    }


    public static String toCaptitalize(String str)
	{
		if(str == null || str.isEmpty()) {
			return str;
        }
		char tab[] = str.toCharArray();
		tab[0] = String.valueOf(tab[0]).toUpperCase().charAt(0);
		return String.valueOf(tab);
	}
	
}
