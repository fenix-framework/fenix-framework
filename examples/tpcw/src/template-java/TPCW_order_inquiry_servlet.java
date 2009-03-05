/* 
 * TPCW_order_inquiry_servlet.java - Servlet Class implements order 
 *                                   inquiry web interaction.
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

public class TPCW_order_inquiry_servlet extends HttpServlet {
    
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {
      HttpSession session = req.getSession(false);

      PrintWriter out = res.getWriter();
      // Set the content type of this servlet's result.
      res.setContentType("text/html");
      String username = "";
      String url;
      String C_ID = req.getParameter("C_ID");
      String SHOPPING_ID=req.getParameter("SHOPPING_ID");

      
      out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD W3 HTML//EN\">\n");
      out.print("<HTML><HEAD><TITLE>TPC-W Order Inquiry Page</TITLE>\n");
      out.print("</HEAD><BODY BGCOLOR=\"#ffffff\">\n"); 
      out.print("<H1 ALIGN=\"center\">TPC Web Commerce Benchmark (TPC-W)</H1>\n"); 
      out.print("<H2 ALIGN=\"center\">Order Inquiry Page</H2>\n"); 

      out.print("<FORM ACTION=\"TPCW_order_display_servlet;@sessionIdString@"+
		req.getRequestedSessionId()+"\" METHOD=\"get\">\n");
      out.print("<TABLE ALIGN=\"CENTER\">\n"); 
      out.print("<TR> <TD> <H4>Username:</H4></TD>\n"); 
      out.print("<TD><INPUT NAME=\"UNAME\" VALUE=\"" + username +
		"\" SIZE=\"23\"></TD></TR>\n"); 
      out.print("<TR><TD> <H4>Password:</H4></TD>\n"); 
      out.print("<TD> <INPUT NAME=\"PASSWD\" SIZE=\"14\" " +
		"TYPE=\"password\"></TD>\n");
      out.print("</TR></TABLE> <P ALIGN=\"CENTER\"><CENTER>\n");

      out.print("<INPUT TYPE=\"IMAGE\" NAME=\"Display Last Order\" "
		+ "SRC=\"../tpcw/Images/display_last_order_B.gif\">\n");
      //      out.print("<INPUT TYPE=HIDDEN NAME=\"" + TPCW_Util.SESSION_ID + 
      //	"\" value = \"" +
      //	req.getRequestedSessionId() + "\">\n");
      if(SHOPPING_ID != null)
	  out.print("<INPUT TYPE=HIDDEN NAME=\"SHOPPING_ID\" value = \"" + 
		    SHOPPING_ID + "\">\n");
      if(C_ID!=null)
	  out.print("<INPUT TYPE=HIDDEN NAME=\"C_ID\" value = \"" + 
		    C_ID + "\">\n");
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
      out.print("</CENTER></FORM></BODY></HTML>");		
      out.close();
      return;
    }
}
