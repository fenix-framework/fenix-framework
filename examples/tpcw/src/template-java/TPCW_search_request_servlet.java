/* 
 * TPCW_search_request_servlet.java - servlet class implements the 
 *                                    search request web interaction.
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

public class TPCW_search_request_servlet extends HttpServlet {
    
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {
      PrintWriter out = res.getWriter();
    // Set the content type of this servlet's result.
      res.setContentType("text/html");
      HttpSession session = req.getSession(false);
      String C_ID = req.getParameter("C_ID");
      String SHOPPING_ID = req.getParameter("SHOPPING_ID");
      String url;

      out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD W3 HTML//EN\">\n");
      out.print("<HTML> <HEAD><TITLE>Search Request Page</TITLE></HEAD>\n");
      out.print("<BODY BGCOLOR=\"#ffffff\">\n");
      out.print("<H1 ALIGN=\"center\">TPC W Commerce Benchmark (TPC-W)</H1>\n");
      out.print("<H2 ALIGN=\"center\">");
      out.print("<IMG SRC=\"../tpcw/Images/tpclogo.gif\" ALIGN=\"BOTTOM\" " + 
		"BORDER=\"0\" WIDTH=\"288\" HEIGHT=\"67\"></H2>\n");
      out.print("<H2 ALIGN=\"center\">Search Request Page</H2>");
      
      //Insert Promotional processing
      TPCW_promotional_processing.DisplayPromotions(out, req, res,-1);

      out.print("<FORM ACTION=\"./TPCW_execute_search;@sessionIdString@"+
		req.getRequestedSessionId()+"\" METHOD=\"get\">\n");
      out.print("<TABLE ALIGN=\"center\"><TR><TD ALIGN=\"right\">\n");
      out.print("<H3>Search by:</H3></TD><TD WIDTH=\"100\"></TD></TR>\n");
      out.print("<TR><TD ALIGN=\"right\">\n");
      out.print("<SELECT NAME=\"search_type\" SIZE=\"1\">\n");
      out.print("<OPTION SELECTED=\"SELECTED\" VALUE=\"author\">Author</OPTION>\n");
      out.print("<OPTION VALUE=\"title\">Title</OPTION>\n");
      out.print("<OPTION VALUE=\"subject\">Subject</OPTION></SELECT></TD>\n");
      
      out.print("<TD><INPUT NAME=\"search_string\" SIZE=\"30\"></TD></TR>\n");
      out.print("</TABLE>\n");
      out.print("<P ALIGN=\"CENTER\"><CENTER>\n");
      out.print("<INPUT TYPE=\"IMAGE\" NAME=\"Search\"" + 
		" SRC=\"../tpcw/Images/submit_B.gif\">\n");
      //      out.print("<INPUT TYPE=HIDDEN NAME=\"" + TPCW_Util.SESSION_ID + 
      //	"\" value=\"" 
      //	+ req.getRequestedSessionId() + "\">\n");
      if(SHOPPING_ID != null)
	  out.print("<INPUT TYPE=HIDDEN NAME=\"SHOPPING_ID\" value = \"" + 
		    SHOPPING_ID + "\">\n");
      if(C_ID!=null)
	  out.print("<INPUT TYPE=HIDDEN NAME=\"C_ID\" value = \"" + 
		    C_ID + "\">\n");
    
      url = "./TPCW_home_interaction";
      if(SHOPPING_ID != null){
	  url = url+"?SHOPPING_ID="+SHOPPING_ID;
	  if(C_ID!=null)
	      url = url + "&C_ID=" + C_ID;
      }
      else if(C_ID!=null)
	  url = url + "?C_ID=" + C_ID;

      out.print("<A HREF=\""+res.encodeUrl(url));
      out.print("\"><IMG SRC=\"../tpcw/Images/home_B.gif\" ALT=\"Home\"></A>\n");
      url = "TPCW_shopping_cart_interaction?ADD_FLAG=N";
      if(SHOPPING_ID != null)
	  url = url + "&SHOPPING_ID=" + SHOPPING_ID;
      if(C_ID != null)
	  url = url + "&C_ID=" + C_ID;

      out.print("<A HREF=\"" + res.encodeUrl(url));
      out.print("\"><IMG SRC=\"../tpcw/Images/shopping_cart_B.gif\"" +
		" ALT=\"Shopping Cart\"></A>\n");
      out.print("</CENTER></P></FORM></BODY></HTML>");
      out.close();
      return;
    }
}
