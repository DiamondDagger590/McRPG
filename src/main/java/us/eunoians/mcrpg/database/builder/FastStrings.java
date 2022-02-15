/*
 * MIT License
 *
 * Copyright (c) 2019 Ethan Bacurio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package us.eunoians.mcrpg.database.builder;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * A fast utility for handling Strings.
 *
 * <p>This is my own implementation of methods that could be found in
 * Apache Commons Lang's StringUtils</p>
 */
public class FastStrings {

  /**
   * Check if a {@link CharSequence} is blank.
   *
   * <p>This methods checks if the length of the {@link CharSequence}
   * is 0, if true, that means the sequence is indeed blank. This method
   * also checks if all code points in the sequence are 'non-printable'.
   * Refer to {@link FastStrings#isGhostChars(int)} to learn more
   * about 'non-printable'</p>
   *
   * @param charSequence {@link CharSequence} that you want to process.
   * @return if the {@link CharSequence} is blank.
   */
  public static boolean isBlank(CharSequence charSequence) {
    if (isEmpty(charSequence)) return true;
    for (int i = 0; i < charSequence.length(); i++)
      if (!isGhostChars((int) charSequence.charAt(i)))
        return false;
    return true;
  }

  /**
   * Check if a {@link CharSequence} is numeric.
   *
   * <p>This methods checks if the length of the {@link CharSequence}
   * is 0, if true, that means the sequence is not numeric. This method
   * also checks if all code points in the sequence are actually numbers.
   * Refer to {@link FastStrings#charIsNumeric(int)} to learn more
   * about numeric chars</p>
   *
   * @param charSequence {@link CharSequence} that you want to process.
   * @return if the {@link CharSequence} is numeric.
   */
  public static boolean isNumeric(CharSequence charSequence) {
    if (isBlank(charSequence)) return false;
    for (int i = 0; i < charSequence.length(); i++) {
      if (!charIsNumeric((int) charSequence.charAt(i)))
        return false;
    }
    return true;
  }

  public static boolean charIsNumeric(int codepoint) {
    if (!Character.isValidCodePoint(codepoint))
      return false;
    return codepoint >= 0x0030 && codepoint <= 0x0039;
  }

  /**
   * Checks if a codepoint of a char is a 'non-printable' char.
   *
   * <p>This method checks if the codepoint provided--from a
   * {@link Character}--is an escape sequence or either a space.</p>
   *
   * @param codepoint code point to process.
   * @return returns true if code point is an escape sequence or a space.
   */
  public static boolean isGhostChars(int codepoint) {
    if(!Character.isValidCodePoint(codepoint))
      return false;
    return     codepoint == 0x0020 // space
            || codepoint == 0x000D // carriage-return
            || codepoint == 0x000A // new-line
            || codepoint == 0x0009 // tab
            || codepoint == 0x0008; // backspace
  }

  /**
   * Count the number of occurrences of a {@link char} in a different
   * {@link CharSequence}.
   *
   * <p>This does not support non-planar characters (characters with code points > 0xFFFF).
   * If the argument is a non-planar character, the method will just return 0</p>
   *
   * @param original The original {@link CharSequence} where this method will be counting
   *                 occurrences from.
   * @param argument The {@link char} that we will count the number of occurrences
   *                 from the parameter "original".
   * @return How many times the argument occurred in the parameter original.
   */
  public static int countOccurrences(CharSequence original, char argument) {
    if (isBlank(original))
      return 0;
    int count = 0;
    for (int i = 0; i < original.length(); ++i)
      if (argument == original.charAt(i))
        count++;
    return count;
  }

  public static String join(Object[] array, CharSequence delimiter) {
    StringJoiner joiner = new StringJoiner(delimiter);
    for (Object obj : array)
      joiner.add(String.valueOf(obj));
    return joiner.toString();
  }

  public static boolean isEmpty(CharSequence charSequence) {
    return charSequence == null || charSequence.length() == 0;
  }

  public static boolean containsNonPlaneChar(String string) {
    int length = string.length();
    return length > string.codePointCount(0, length);
  }

  private static boolean isPlaneChar(int codePoint) {
    return codePoint >= 0x0000 && codePoint <= 0xFFFF;
  }

  public static String removeNonPlaneChar(String str) {
    if (isBlank(str))
      return "";

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < str.length(); ) {
      int c = str.codePointAt(i);
      if (isPlaneChar(c)) {
        sb.append((char) c);
        i++;
      } else {
        i += 2;
      }
    }
    return sb.toString();
  }

  public static List<Integer> getIndexOfNonPlaneChars(String str) {
    if (isBlank(str))
      return Collections.emptyList();

    ImmutableList.Builder<Integer> builder = new ImmutableList.Builder<>();
    int npCount = 0;
    for (int i = 0; i < str.length(); ) {
      int c = str.codePointAt(i);
      if (!isPlaneChar(c)) {
        i += 2;
        builder.add(i - 2 - npCount);
        npCount++;
      } else
        i++;
    }
    return builder.build();
  }

}
