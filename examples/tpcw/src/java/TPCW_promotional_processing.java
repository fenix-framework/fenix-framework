/* 
 * TPCW_promotional_processing.java - This class is basically just a 
 *                                    utility function used to spit
 *                                    out the promotional processing
 *                                    at the top of many web pages.
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

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import tpcw_dto.*;

public class TPCW_promotional_processing {

    public static void DisplayPromotions(PrintWriter out, 
					 HttpServletRequest req,
					 HttpServletResponse res,
					 int new_sid){
	int I_ID = TPCW_Util.getRandomI_ID();
	Vector related_item_ids = new Vector();
	Vector thumbnails = new Vector();
	int i;
	String url;

	TPCW_Database.getRelated(I_ID, related_item_ids, thumbnails);

	String C_ID = req.getParameter("C_ID");
	String SHOPPING_ID = req.getParameter("SHOPPING_ID");

	//Create table and "Click on our latest books..." row
	out.print("<TABLE ALIGN=CENTER BORDER=0 WIDTH=660>\n");
	out.print("<TR ALIGN=CENTER VALIGN=top>\n");
	out.print("<TD COLSPAN=5><B><FONT COLOR=#ff0000 SIZE=+1>"+
		  "Click on one of our latest books to find out more!" +
		  "</FONT></B></TD></TR>\n");
	out.print("<TR ALIGN=CENTER VALIGN=top>\n");
	
	//Create links and references to book images
	for(i = 0; i < related_item_ids.size(); i++){
	    url = "./TPCW_product_detail_servlet";
	    url = url + "?I_ID=" + 
		String.valueOf(related_item_ids.elementAt(i));
	    if(SHOPPING_ID != null)
		url = url + "&SHOPPING_ID=" + SHOPPING_ID;
	    else if(new_sid != -1)
		url = url + "&SHOPPING_ID=" + new_sid;
	    if(C_ID != null)
		url = url + "&C_ID=" + C_ID;
	    out.print("<TD><A HREF=\""+ res.encodeUrl(url));
	    out.print("\"><IMG SRC=\"../tpcw/Images/" +thumbnails.elementAt(i)
		      + "\" ALT=\"Book " + String.valueOf(i+1) 
		      + "\" WIDTH=\"100\" HEIGHT=\"150\"></A>\n");
	    out.print("</TD>");
	}
	out.print("</TR></TABLE>\n");
    }

}
