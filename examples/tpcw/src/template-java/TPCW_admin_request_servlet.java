/* 
 * TPCW_admin_request_servlet - Servlet Class implements the admin
 *                              request web interaction.
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

public class TPCW_admin_request_servlet extends HttpServlet {
    
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {
      String url;
      PrintWriter out = res.getWriter();

    // Set the content type of this servlet's result.
      res.setContentType("text/html");
      
      HttpSession session = req.getSession(false);
      String I_IDstr = req.getParameter("I_ID");
      String C_ID = req.getParameter("C_ID");
      String SHOPPING_ID = req.getParameter("SHOPPING_ID");

      int I_ID = Integer.parseInt(I_IDstr, 10);

      Book book = TPCW_Database.getBook(I_ID);

      out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD W3 HTML//EN\">\n");
      out.print("<HTML><HEAD><TITLE>TPC-W Product Update Page</TITLE></HEAD>");
      out.print("<BODY BGCOLOR=\"#ffffff\">\n");
      out.print("<H1 ALIGN=\"center\">TPC Web Commerce Benchmark "+
		"(TPC-W)</H1>");
      out.print("<H2 ALIGN=\"center\"><IMG SRC=\"../tpcw/Images/tpclogo.gif\""+
		"ALIGN=\"BOTTOM\" BORDER=\"0\" WIDTH=\"288\" " +
		"HEIGHT=\"67\"></H2>\n");
      out.print("<H2 ALIGN=\"center\">Admin Request Page</H2>");

      out.print("<H2 ALIGN=\"center\">Title:" + book.i_title + "</H2>\n");
      out.print("<P ALIGN=\"LEFT\">Author: " + book.a_fname + " "  
		+ book.a_lname + "<BR></P>\n");
      out.print("<IMG SRC=\"../tpcw/Images/" 
		+ book.i_image + "\" ALIGN=\"RIGHT\" BORDER=\"0\" " + 
		"WIDTH=\"200\" HEIGHT=\"200\" >\n");
      out.print("<IMG SRC=\"../tpcw/Images/" + book.i_thumbnail +  
		"\" ALIGN=\"RIGHT\" BORDER=\"0\">");
      out.print("<P><BR><BR></P>");
      out.print("<FORM ACTION=\"TPCW_admin_response_servlet;@sessionIdString@"+
		req.getRequestedSessionId()
		+"\" METHOD=\"get\">\n");
      out.print("<INPUT NAME=\"I_ID\" TYPE=\"hidden\" VALUE=\"" + I_ID 
		+"\">\n");
      out.print("<TABLE BORDER=\"0\">\n");
      out.print("<TR><TD><B>Suggested Retail:</B></TD><TD><B>$ " + book.i_srp+
	        "</B></TD></TR>\n");
      out.print("<TR><TD><B>Our Current Price: </B></TD>" +
		"<TD><FONT COLOR=\"#dd0000\"><B>$ " + book.i_cost +  
		"</B></FONT></TD></TR>\n");
      out.print("<TR><TD><B>Enter New Price</B></TD>" +
		"<TD ALIGN=\"right\">$ <INPUT NAME=\"I_NEW_COST\"></TD></TR>");
      out.print("<TR><TD><B>Enter New Picture</B></TD><TD ALIGN=\"right\">" +
		"<INPUT NAME=\"I_NEW_IMAGE\"></TD></TR>\n");
      out.print("<TR><TD><B>Enter New Thumbnail</B></TD><TD ALIGN=\"RIGHT\">"+
		"<INPUT TYPE=\"TEXT\" NAME=\"I_NEW_THUMBNAIL\"></TD></TR>\n");
      out.print("</TABLE>");
      out.print("<P><BR CLEAR=\"ALL\"></P> <P ALIGN=\"center\">");
      if(SHOPPING_ID != null)
	  out.print("<INPUT TYPE=HIDDEN NAME=\"SHOPPING_ID\" value = \"" + 
		    SHOPPING_ID + "\">\n");
      if(C_ID!=null)
	  out.print("<INPUT TYPE=HIDDEN NAME=\"C_ID\" value = \"" + 
		    C_ID + "\">\n");
      out.print("<INPUT TYPE=\"IMAGE\" NAME=\"Submit\"" +
		" SRC=\"../tpcw/Images/submit_B.gif\">\n");
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
		+ "ALT=\"Home\"></A></P>\n");

      out.print("</FORM></BODY></HTML>");
      out.close();
      return;
    }
}
