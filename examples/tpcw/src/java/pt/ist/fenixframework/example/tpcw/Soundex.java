package pt.ist.fenixframework.example.tpcw;

/*
 * Copyright (c) Ian F. Darwin, http://www.darwinsys.com/, 1996-2002.
 * All rights reserved. Software written by Ian F. Darwin and others.
 * $Id: LICENSE,v 1.8 2004/02/09 03:33:38 ian Exp $
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Java, the Duke mascot, and all variants of Sun's Java "steaming coffee
 * cup" logo are trademarks of Sun Microsystems. Sun's, and James Gosling's,
 * pioneering role in inventing and promulgating (and standardizing) the Java 
 * language and environment is gratefully acknowledged.
 * 
 * The pioneering role of Dennis Ritchie and Bjarne Stroustrup, of AT&T, for
 * inventing predecessor languages C and C++ is also gratefully acknowledged.
 */
/**
 * Soundex - the Soundex Algorithm, as described by Knuth, adapted to behave like mysql's soundex
 * <p>
 * This class implements the soundex algorithm as described by Donald
 * Knuth in Volume 3 of <I>The Art of Computer Programming</I>.  The
 * algorithm is intended to hash words (in particular surnames) into
 * a small space using a simple model which approximates the sound of
 * the word when spoken by an English speaker.  Each word is reduced
 * to a four character string, the first character being an upper case
 * letter and the remaining three being digits. Double letters are
 * collapsed to a single digit.
 * 
 * <h2>EXAMPLES</h2>
 * Knuth's examples of various names and the soundex codes they map
 * to are:
 * <b>Euler, Ellery -> E460
 * <b>Gauss, Ghosh -> G200
 * <b>Hilbert, Heilbronn -> H416
 * <b>Knuth, Kant -> K530
 * <b>Lloyd, Ladd -> L300
 * <b>Lukasiewicz, Lissajous -> L222
 * 
 * <h2>LIMITATIONS</h2>
 * As the soundex algorithm was originally used a <B>long</B> time ago
 * in the United States of America, it uses only the English alphabet
 * and pronunciation.
 * <p>
 * As it is mapping a large space (arbitrary length strings) onto a
 * small space (single letter plus 3 digits) no inference can be made
 * about the similarity of two strings which end up with the same
 * soundex code.  For example, both "Hilbert" and "Heilbronn" end up
 * with a soundex code of "H416".
 * <p>
 * The soundex() method is static, as it maintains no per-instance
 * state; this means you never need to instantiate this class.
 *
 * @author Perl implementation by Mike Stok (<stok@cybercom.net>) from
 * the description given by Knuth.  Ian Phillips (<ian@pipex.net>) and
 * Rich Pinder (<rpinder@hsc.usc.edu>) supplied ideas and spotted
 * mistakes.
 * @author Ian Darwin, http://www.darwinsys.com/ (Java Version)
 * @version $Id: Soundex.java,v 1.9 2004/02/23 00:30:49 ian Exp $
 */
public class Soundex {

  /* Implements the mapping
   * from: AEHIOUWYBFPVCGJKQSXZDTLMNR
   * to:   00000000111122222222334556
   */
  public static final char[] MAP = {
    //A  B   C   D   E   F   G   H   I   J   K   L   M
    '0','1','2','3','0','1','2','0','0','2','2','4','5',
    //N  O   P   W   R   S   T   U   V   W   X   Y   Z
    '5','0','1','2','6','2','3','0','1','0','2','0','2'
  };

  /** Convert the given String to its Soundex code.
   * @return "" If the given string can't be mapped to Soundex.
   */
  public static String soundex(String s) {

    // Algorithm works on uppercase (mainframe era).
    String t = s.toUpperCase();

    StringBuffer res = new StringBuffer();
    char c, prev = '?';

    // Main loop: find up to 4 chars that map.
    for (int i=0; i<t.length() && res.length() < 4 ; i++) {
      c = t.charAt(i);
      // Check to see if the given character is alphabetic.
      // Text is already converted to uppercase. Algorithm
      // only handles ASCII letters, do NOT use Character.isLetter()!
      // Also, skip double letters.
      if (c>='A' && c<='Z' && c != prev) {
        prev = c;

        // First char is installed unchanged, for sorting.
        if (res.length() == 0)
          res.append(c);
        else {
	    char m = MAP[c-'A'];
	    if (m != '0') {
		int len = res.length();  
		if ((len == 1 && m != MAP[res.charAt(0)-'A'])
		    || (len != 1 && m != res.charAt(res.length()-1)))
		    res.append(m);
	    }
        }
      }
    }
    if (res.length() == 0)
      return "";
    for (int i=res.length(); i<4; i++)
      res.append('0');
    return res.toString();
  }
  /** main */
  public static void main(String[] args) { 
    String[] names = {
      "Darwin, Ian",
      "Davidson, Greg",
      "Darwent, William",
      "Derwin, Daemon"
    };
    for (int i = 0; i< names.length; i++)
      System.out.println(Soundex.soundex(names[i]) + ' ' + names[i]);
  }

}
