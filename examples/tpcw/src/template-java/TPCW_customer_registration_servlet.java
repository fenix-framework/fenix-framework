/* 
 * TPCW_customer_registration_servlet.java - Servlet class implements the
 *                                           customer registration servlet
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

public class TPCW_customer_registration_servlet extends HttpServlet {
    
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {

      String url;
      HttpSession session = req.getSession(false);
      PrintWriter out = res.getWriter();
      // Set the content type of this servlet's result.
      res.setContentType("text/html");

      String C_ID = req.getParameter("C_ID");
      String SHOPPING_ID = req.getParameter("SHOPPING_ID");

      String username;
      if(C_ID != null){
	  int c_idnum = Integer.parseInt(C_ID);
	  username = TPCW_Database.GetUserName(c_idnum);
      }
      else username = "";


      out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD W3 HTML//EN\">\n");
      out.print("<HTML>\n");
      out.print("<HEAD><TITLE>Customer Registration</TITLE></HEAD>\n");
      out.print("<BODY BGCOLOR=\"#ffffff\">\n");
      out.print("<H1 ALIGN=\"center\">TPC Web Commerce Benchmark (TPC-W)"
		+"</H1><H1 ALIGN=\"center\">\n");
 
      out.print("<IMG SRC=\"../tpcw/Images/tpclogo.gif\" " +
		"ALIGN=\"BOTTOM\" BORDER=\"0\" WIDTH=\"288\" HEIGHT=\"67\">");
      out.print("</H1><H2 ALIGN=\"center\">Customer Registration Page</H2>\n");
      out.print("<FORM ACTION=\"TPCW_buy_request_servlet;@sessionIdString@"+
		req.getRequestedSessionId()+"\" METHOD=\"get\">");
      out.print("<BLOCKQUOTE><BLOCKQUOTE>\n");
      out.print("<HR><TABLE BORDER=\"0\"><TR>\n");
      out.print("<TD><INPUT CHECKED=\"CHECKED\" NAME=\"RETURNING_FLAG\" " + 
		"TYPE=\"radio\" VALUE=\"Y\">I am an existing customer");
      out.print("</TD></TR><TR><TD>\n");
      out.print("<INPUT NAME=\"RETURNING_FLAG\" TYPE=\"radio\" VALUE=\"N\">" +
		"I am a first time customer</TD></TR></TABLE>\n");
      out.print("<HR><P><B>If you're an existing customer, enter your User " +
		"ID and Password:</B><BR><BR></P>\n");
      out.print("<TABLE><TR ALIGN=\"left\">\n");
      out.print("<TD>User ID: <INPUT NAME=\"UNAME\" SIZE=\"23\"></TD></TR>\n");
      out.print("<TR ALIGN=\"left\">\n");
      out.print("<TD>Password: <INPUT SIZE=\"14\" NAME=\"PASSWD\" " + 
		"TYPE=\"password\"></TD></TR></TABLE> \n");
      out.print("<HR><P><B>If you re a first time customer, enter the " +
		"details below:</B><BR></P>\n");
      out.print("<TABLE><TR><TD>Enter your birth date (mm/dd/yyyy):</TD>\n");
      out.print("<TD> <INPUT NAME=\"BIRTHDATE\" SIZE=\"10\"></TD></TR>");
      out.print("<TR><TD>Enter your First Name:</TD>\n");
      out.print("<TD> <INPUT NAME=\"FNAME\" SIZE=\"15\"></TD></TR>\n");
      out.print("<TR><TD>Enter your Last Name:</TD>\n");
      out.print("<TD><INPUT NAME=\"LNAME\" SIZE=\"15\"></TD></TR>\n");
      out.print("<TR><TD>Enter your Address 1:</TD>\n");
      out.print("<TD><INPUT NAME=\"STREET1\" SIZE=\"40\"></TD></TR>\n");
      out.print("<TR><TD>Enter your Address 2:</TD>\n");
      out.print("<TD> <INPUT NAME=\"STREET2\" SIZE=\"40\"></TD></TR>\n");
      
      out.print("<TR><TD>Enter your City, State, Zip:</TD>\n");
      out.print("<TD><INPUT NAME=\"CITY\" SIZE=\"30\">" +
		"<INPUT NAME=\"STATE\"><INPUT NAME=\"ZIP\" SIZE=\"10\">\n");
      out.print("</TD></TR>");
      

      out.print("<TR><TD>Enter your Country:</TD>\n");
      out.print("<TD><INPUT NAME=\"COUNTRY\" SIZE=\"50\"></TD></TR>\n");
      out.print("<TR><TD>Enter your Phone:</TD>\n");
      out.print("<TD><INPUT NAME=\"PHONE\" SIZE=\"16\"></TD></TR>\n");
      out.print("<TR><TD>Enter your E-mail:</TD>\n");
      out.print("<TD> <INPUT NAME=\"EMAIL\" SIZE=\"50\"></TD></TR></TABLE>\n");

      out.print("<HR><TABLE><TR><TD COLSPAN=\"2\">Special Instructions:");
      out.print("<TEXTAREA COLS=\"65\" NAME=\"DATA\" ROWS=\"4\">" + 
		"</TEXTAREA></TD></TR></TABLE></BLOCKQUOTE></BLOCKQUOTE>" +
		"<CENTER>\n");
      out.print("<INPUT TYPE=\"IMAGE\" NAME=\"Enter Order\" " +
		"SRC=\"../tpcw/Images/submit_B.gif\">\n");
      //      out.print("<INPUT TYPE=HIDDEN NAME=\"" + TPCW_Util.SESSION_ID + 
      //	"\" value = \"" + req.getRequestedSessionId() + "\">\n");
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
      out.print("\"><IMG SRC=\"../tpcw/Images/search_B.gif\" ALT=\"Search Item\"></A>");

      url = "TPCW_home_interaction";
      if(SHOPPING_ID != null){
	  url = url+"?SHOPPING_ID="+SHOPPING_ID;
	  if(C_ID!=null)
	      url = url + "&C_ID=" + C_ID;
      }
      else if(C_ID!=null)
	  url = url + "?C_ID=" + C_ID;
      
      out.print("<A HREF=\"" + res.encodeUrl(url));
      out.print("\"><IMG SRC=\"../tpcw/Images/home_B.gif\" ALT=\"Home\"></A>");
      out.print("</CENTER></FORM>");
      out.print("</BODY></HTML>");
      out.close();
      return;
    }
}
