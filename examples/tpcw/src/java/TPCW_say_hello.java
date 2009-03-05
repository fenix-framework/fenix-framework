/* 
 * TPCW_say_hello.java - Utility function used by home interaction, 
 *                       creates a new session id for new users.
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

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import tpcw_dto.*;

public class TPCW_say_hello {
    
    public static void print_hello(HttpSession session, HttpServletRequest req,
				   PrintWriter out){

	//If we have seen this session id before
	if (!session.isNew()) {
	    int C_ID[] = (int [])session.getValue("C_ID");
	    //check and see if we have a customer name yet
	    if (C_ID != null) // Say hello.
		out.println("Hello " + (String)session.getValue("C_FNAME") +
			    " " + (String)session.getValue("C_LNAME"));
	    else out.println("Hello unknown user");
	} 
	else {//This is a brand new session
	    
	    out.println("Thisis a brand new session!");
	    // Check to see if a C_ID was given.  If so, get the customer name
	    // from the database and say hello.
	    String C_IDstr = req.getParameter("C_ID");
	    if (C_IDstr != null) {
		String name[];
		int C_ID[] = new int[1];
		C_ID[0] = Integer.parseInt(C_IDstr, 10);
                out.flush();
		// Use C_ID to get the user name from the database.
		name = TPCW_Database.getName(C_ID[0]);
		// Set the values for this session.
		if(name==null){
		   out.println("Hello unknown user!");
		   return;
		}
		session.putValue("C_ID", C_ID);
		session.putValue("C_FNAME", name[0]);
		session.putValue("C_LNAME", name[1]);
		out.println("Hello " + name[0] + " " + name[1] +".");
		
	    } 
	    else out.println("Hello unknown user!");
	}
    }
}


