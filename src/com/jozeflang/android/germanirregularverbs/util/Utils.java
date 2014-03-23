package com.jozeflang.android.germanirregularverbs.util;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/** 
 * Library class
 * @author @author Jozef Lang (developer@jozeflang.com)
 */
public class Utils {

	/**
	 * Character escape map: <br />
	 * <ul>
	 * 	<li>ß = ss</li>
	 *  <li>Ö = Oe</li> 
	 *  <li>ö = oe</li>
	 *  <li>Ä = Ae</li>
	 *  <li>ä = ae</li>
	 *  <li>Ü = Ue</li>
	 *  <li>ü = ue</li>
	 * </ul>
	 */
	private final static Map<Character, String> escapeCharacterMap = new HashMap<Character, String>();
	static {
		escapeCharacterMap.put('ß', "ss");
		escapeCharacterMap.put('Ü', "Ue");
		escapeCharacterMap.put('ü', "ue");
		escapeCharacterMap.put('Ö', "Oe");
		escapeCharacterMap.put('ö', "oe");
		escapeCharacterMap.put('Ä', "Ae");
		escapeCharacterMap.put('ä', "ae");
	}
	
	private Utils() {
		
	}
	
	/**
	 * Returns random number from random number generator. Generated number is between selected boundaries.
	 * @param from
	 * @param to
	 * @return
	 */
	public static int getRandom(int from, int to) {
		return getRandom(new Random(), from, to);
	}
	
	/**
	 * Returns random number from random number generator. Generated number is between selected boundaries.
	 * @param rg
	 * @param from
	 * @param to
	 * @return
	 */
	public static int getRandom(final Random rg, int from, int to) {
		int randomInt = rg.nextInt(to-from+1);
		return randomInt + from;
	}
	
	/**
	 * Converts long value to integer.<br />
	 * An {@link IllegalArgumentException} is thrown when long value is outside of the integer minimum or maximum values.
	 * @param lvalue
	 * @return
	 */
	public static int longToInt(final long lvalue) {
		if (lvalue <= Integer.MIN_VALUE || lvalue > Integer.MAX_VALUE) 
			throw new IllegalArgumentException(lvalue + " cannot be casted to integer");
		return (int) lvalue;
	}
	
	/**
	 * Reads a text file from {@link InputStream} using {@link BufferedReader#readLine()} method <br />
	 * If an exception occurs, a so far read content is returned 
	 * @param in
	 * @return
	 */
	public static String readTextFile(InputStream in) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuffer sb = new StringBuffer();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			return sb.toString();
		}
		return sb.toString();
	}
	
	/**
	 * Escapes German characters.
	 * @param s
	 * @return
	 * @see {@link Utils#escapeCharacterMap}
	 */
	public static String escapeGermanCharacters(final String s) {
		if (s == null)
			return null;
		if (TextUtils.isEmpty(s))
			return s;
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			String escapedCharacter = escapeCharacterMap.get(Character.valueOf(c));
			sb.append(escapedCharacter == null ? c : escapedCharacter);
		}
		return sb.toString();
	}

    /**
     * Constructs a String containing values of collection delimited by delimiter string
     * @param collection
     * @param delimiter
     * @return
     */
    public static String buildDelimitedString(Collection<?> collection, String delimiter) {
        StringBuilder sb = new StringBuilder();
        boolean wasAtleastOne = false;
        for (Object e : collection) {
            if (wasAtleastOne) {
                sb.append(delimiter);
            }
            wasAtleastOne = true;
            sb.append(e.toString());
        }
        return sb.toString();
    }
}
