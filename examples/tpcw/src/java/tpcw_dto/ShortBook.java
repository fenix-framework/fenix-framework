package tpcw_dto;
/* 
 * ShortBook.java - Class stores a subset of the information related to a 
 *                  a single ITEM in the DB.
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
 ************************************************************************/

import java.util.Date;
import java.sql.*;

public class ShortBook {
    protected ShortBook() {}
    // Construct a book from a ResultSet
    public ShortBook(ResultSet rs) {
	// The result set should have all of the fields we expect.
	// This relies on using field name access.  It might be a bad
	// way to break this up since it does not allow us to use the
	// more efficient select by index access method.  This also
	// might be a problem since there is no type checking on the
	// result set to make sure it is even a reasonble result set
	// to give to this function.
       
	try {
	    i_id = rs.getInt("i_id");
	    i_title = rs.getString("i_title");
	    a_fname = rs.getString("a_fname");
	    a_lname = rs.getString("a_lname");		
	} catch (java.lang.Exception ex) {
	    ex.printStackTrace();
	}
    }
    // From Item
    public int i_id;
    public String i_title;
    public String a_fname;
    public String a_lname;
}



