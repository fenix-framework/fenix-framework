/* 
 * TPCW_Util.java - Some random utility functions needed by the servlets.
 *
 ************************************************************************
 *
 * This is part of the the Java TPC-W distribution,
 * written by Harold Cain, Tim Heil, Milo Martin, Eric Weglarz, and Todd
 * Bezenek.  University of Wisconsin - Madison, Computer Sciences
 * Dept. and Dept. of Electrical and Computer Engineering, as a part of
 * Prof. Mikko Lipasti's Fall 1999 ECE 902 course.
 *
 * Copyright (C) 1999, 2000 by Harold Cain, Timothy Heil, Milo Martin, 
 *                             Eric Weglarz, Todd Bezenek.
 *
 * This source code is distributed "as is" in the hope that it will be
 * useful.  It comes with no warranty, and no author or distributor
 * accepts any responsibility for the consequences of its use.
 *
 * Everyone is granted permission to copy, modify and redistribute
 * this code under the following conditions:
 *
 * This code is distributed for non-commercial use only.
 * Please contact the maintainer for restrictions applying to 
 * commercial use of these tools.
 *
 * Permission is granted to anyone to make or distribute copies
 * of this code, either as received or modified, in any
 * medium, provided that all copyright notices, permission and
 * nonwarranty notices are preserved, and that the distributor
 * grants the recipient permission for further redistribution as
 * permitted by this document.
 *
 * Permission is granted to distribute this code in compiled
 * or executable form under the same conditions that apply for
 * source code, provided that either:
 *
 * A. it is accompanied by the corresponding machine-readable
 *    source code,
 * B. it is accompanied by a written offer, with no time limit,
 *    to give anyone a machine-readable copy of the corresponding
 *    source code in return for reimbursement of the cost of
 *    distribution.  This written offer must permit verbatim
 *    duplication by anyone, or
 * C. it is distributed by someone who received only the
 *    executable form, and is accompanied by a copy of the
 *    written offer of source code that they received concurrently.
 *
 * In other words, you are welcome to use, share and improve this codes.
 * You are forbidden to forbid anyone else to use, share and improve what
 * you give them.
 *
 ************************************************************************
 *
 * Changed 2003 by Jan Kiefer.
 *
 ************************************************************************/

import java.util.*;

public class TPCW_Util {
    
    //public final String SESSION_ID="JIGSAW_SESSION_ID";
    //public static final String SESSION_ID="JServSessionIdroot";
    public static final String SESSION_ID="@sessionIdString@";

    //This must be equal to the number of items in the ITEM table
    public static final int NUM_ITEMS = @num.item@;

    public static int getRandomI_ID(){
	Random rand = new Random();
	Double temp = new Double(Math.floor(rand.nextFloat() * NUM_ITEMS));
	return temp.intValue();
    }

    public static int getRandom(int i) {  // Returns integer 1, 2, 3 ... i
	return ((int) (java.lang.Math.random() * i)+1);
    }

    //Not very random function. If called in swift sucession, it will
    //return the same string because the system time used to seed the
    //random number generator won't change. 
    public static String getRandomString(int min, int max){
	String newstring = new String();
	Random rand = new Random();
	int i;
	final char[] chars = {'a','b','c','d','e','f','g','h','i','j','k',
			      'l','m','n','o','p','q','r','s','t','u','v',
			      'w','x','y','z','A','B','C','D','E','F','G',
			      'H','I','J','K','L','M','N','O','P','Q','R',
			      'S','T','U','V','W','X','Y','Z','!','@','#',
			      '$','%','^','&','*','(',')','_','-','=','+',
			      '{','}','[',']','|',':',';',',','.','?','/',
			      '~',' '}; //79 characters
	int strlen = (int) Math.floor(rand.nextDouble()*(max-min+1));
	strlen += min;
	for(i = 0; i < strlen; i++){
	    char c = chars[(int) Math.floor(rand.nextDouble()*79)];
	    newstring = newstring.concat(String.valueOf(c));
	}
	return newstring;
    }
    
    // Defined in TPC-W Spec Clause 4.6.2.8
    private static final String [] digS = {
	"BA","OG","AL","RI","RE","SE","AT","UL","IN","NG"
    };
  

    public static String DigSyl(int d, int n)
    {
	String s = "";
	
	if (n==0) return(DigSyl(d));	
	for (;n>0;n--) {
	    int c = d % 10;
	    s = digS[c]+s;
	    d = d /10;
	}
	
	return(s);
    }
    
    public static String DigSyl(int d)
    {
	String s = "";
	
	for (;d!=0;d=d/10) {
	    int c = d % 10;
	    s = digS[c]+s;      
	}
	
	return(s);
    }
}
