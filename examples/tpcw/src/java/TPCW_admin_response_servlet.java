/* 
 * TPCW_admin_respons_servlet.java - Servlet class implements the admin
 *                                   response web interaction.
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
import javax.servlet.*;
import javax.servlet.http.*;

import tpcw_dto.*;

public class TPCW_admin_response_servlet extends HttpServlet {
    
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {
      PrintWriter out = res.getWriter();
      String url;

    // Set the content type of this servlet's result.
      res.setContentType("text/html");

      HttpSession session = req.getSession(false);
      
      //Pull out the parameters
      int I_ID = Integer.parseInt((String) req.getParameter("I_ID"));
      String I_NEW_IMAGE = (String) req.getParameter("I_NEW_IMAGE");
      String I_NEW_THUMBNAIL = (String) req.getParameter("I_NEW_THUMBNAIL");
      String I_NEW_COSTstr = (String) req.getParameter("I_NEW_COST");
      Double I_NEW_COSTdbl = Double.valueOf(I_NEW_COSTstr);
            
      String C_ID = req.getParameter("C_ID");
      String SHOPPING_ID = req.getParameter("SHOPPING_ID");
      
      //Get this book out of the database
      Book book = TPCW_Database.getBook(I_ID);
      
      //Spit out the HTML
      out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD W3 HTML//EN\">\n");
      out.print("<HTML> <HEAD><TITLE>TPC-W Admin Response Page</TITLE></HEAD>\n");
      out.print("<BODY BGCOLOR=\"#FFFFFF\">\n");
      out.print("<H1 ALIGN=\"CENTER\">TPC Web Commerce Benchmark (TPC-W)</H1>\n");
      out.print("<H2 ALIGN=\"CENTER\"><IMG SRC=\"../tpcw/Images/tpclogo.gif\""
		+"ALIGN=\"BOTTOM\" BORDER=\"0\" WIDTH=\"288\" "
		+"HEIGHT=\"67\"></H2> ");
      
      if(I_NEW_COSTstr.length() == 0 || I_NEW_IMAGE.length() == 0 
	 || I_NEW_THUMBNAIL.length() == 0) {
	  out.print("<H2>Invalid Input</H2>");
      }
      else {
	  //Update the database
	  TPCW_Database.adminUpdate(I_ID, I_NEW_COSTdbl.doubleValue(),
				    I_NEW_IMAGE,I_NEW_THUMBNAIL);
	  
	  out.print("<H2>Product Updated</H2>");
	  out.print("<H2>Title: " + book.i_title + "</H2>\n");
	  out.print("<P>Author: " + book.a_fname+ " " + book.a_lname + 
		    "</P>\n");
	  out.print("<P><IMG SRC=\"../tpcw/Images/" + I_NEW_IMAGE +
		    "\" ALIGN=\"RIGHT\" BORDER=\"0\" WIDTH=\"200\" " +
		    "HEIGHT=\"200\">");
	  out.print("<IMG SRC=\"../tpcw/Images/" + I_NEW_THUMBNAIL + 
		    "\" ALT=\"Book 1\" ALIGN=\"RIGHT\" WIDTH=\"100\"" 
		    +" HEIGHT=\"150\">\n");
	  out.print("Description: " + book.i_desc + "</P>\n");
	  out.print("<BLOCKQUOTE><P><B>Suggested Retail: $" + book.i_srp +
		    "</B><BR><B>Our Price: </B><FONT COLOR=\"#DD0000\"><B>" +
		    I_NEW_COSTstr+"</B></FONT><BR><B>You Save: </B><FONT " + 
		    "COLOR=\"#DD0000\"><B>" + 
		    Double.toString((book.i_srp - 
				     (Double.valueOf(I_NEW_COSTstr)).doubleValue())) + "</B></FONT></P></BLOCKQUOTE> ");
	  out.print("<P><FONT SIZE=\"2\">" + book.i_backing +", " + 
		    book.i_page +
		    " pages<BR>\n");
	  out.print("Published by " + book.i_publisher + "<BR>\n");
	  out.print("Publication date: " + book.i_pub_Date + "<BR>\n");
	  out.print("Dimensions (in inches): " +book.i_dimensions+ "<BR>\n");
	  out.print("ISBN: " + book.i_isbn + 
		    "</FONT><BR CLEAR=\"ALL\"></P>\n");
	  
	  out.print("<CENTER>");
	  url = "TPCW_search_request_servlet";
	  if(SHOPPING_ID != null){
	      url = url+"?SHOPPING_ID="+SHOPPING_ID;
	      if(C_ID!=null)
		  url = url + "&C_ID=" + C_ID;
	  }
	  else if(C_ID!=null)
	      url = url + "?C_ID=" + C_ID;
	  
	  out.print("<A HREF=\"" + res.encodeUrl(url));
	  out.print("\"><IMG SRC=\"../tpcw/Images/search_B.gif\" "
		    + "ALT=\"Search\"></A>\n");
	  
	  url = "TPCW_home_interaction";
	  if(SHOPPING_ID != null){
	      url = url+"?SHOPPING_ID="+SHOPPING_ID;
	      if(C_ID!=null)
		  url = url + "&C_ID=" + C_ID;
	  }
	  else if(C_ID!=null)
	      url = url + "?C_ID=" + C_ID;
	  
	  out.print("<A HREF=\"" + res.encodeUrl(url));

	  out.print("\"><IMG SRC=\"../tpcw/Images/home_B.gif\" " 
		    + "ALT=\"Home\"></A></P></CENTER>\n");

	  out.print("</FORM>\n");
      }
      out.print("</BODY></HTML>");
      out.close();
      return;
  }
}
