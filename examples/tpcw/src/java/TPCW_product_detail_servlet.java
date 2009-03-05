/* 
 * TPCW_product_detail_servlet.java - Servlet Class implements product
 *                                    detail web interaction.
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

public class TPCW_product_detail_servlet extends HttpServlet {
    
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {
      String url;
      HttpSession session = req.getSession(false);
      String I_IDstr = req.getParameter("I_ID"); 
      int I_ID = Integer.parseInt(I_IDstr);
      String C_ID = req.getParameter("C_ID");
      String SHOPPING_ID = req.getParameter("SHOPPING_ID");
      
      PrintWriter out = res.getWriter();
      res.setContentType("text/html");
      
      Book mybook = TPCW_Database.getBook(I_ID);
      
      out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD W3 HTML//EN\">\n"); 
      out.print("<HTML><HEAD> <TITLE>TPC-W Product Detail Page</TITLE>\n");
      out.print("</HEAD> <BODY BGCOLOR=\"#ffffff\"> <H1 ALIGN=\"center\">" + 
                  "TPC Web Commerce Benchmark (TPC-W)</H1>\n");
      out.print("<CENTER><IMG SRC=\"../tpcw/Images/tpclogo.gif\"" + 
	      " ALIGN=\"BOTTOM\" BORDER=\"0\" WIDTH=\"288\" HEIGHT=\"67\">\n");
      out.print("</CENTER> <H2 ALIGN=\"center\">Product Detail Page</H2>\n"); 

      out.print("<H2> Title: "+mybook.i_title +"</H2>\n");
      out.print("<P>Author: "+mybook.a_fname +" "+mybook.a_lname + "<BR>\n");
      out.print("Subject: " + mybook.i_subject + "\n");
      out.print("<P><IMG SRC=../tpcw/Images/"+mybook.i_image + 
	    " ALIGN=\"RIGHT\" BORDER=\"0\" WIDTH=\"200\" HEIGHT=\"200\">\n");
      out.print("Decription: <I>"+ mybook.i_desc + "</I></P>\n");
      out.print("<BLOCKQUOTE><P><B>Suggested Retail: " + 
		  mybook.i_srp + "</B>\n");
      out.print("<BR><B>Our Price:</B>\n");
      out.print("<FONT COLOR=\"#dd0000\"> <B> " + mybook.i_cost + 
		  "</B></FONT><BR>\n");
      out.print("<B>You Save:</B><FONT COLOR=\"#dd0000\"> $" 
		  + (mybook.i_srp - mybook.i_cost) + "</B></FONT></P>\n");
      out.print("</BLOCKQUOTE><DL><DT><FONT SIZE=\"2\">\n");
      out.print("Backing: " + mybook.i_backing + ", " + mybook.i_page + 
		  " pages<BR>\n");
      out.print("Published by " + mybook.i_publisher +"<BR>\n");
      out.print("Publication date: " + mybook.i_pub_Date +"<BR>\n");
      out.print("Avail date: " + mybook.i_avail + "<BR>\n");
      out.print("Dimensions (in inches): " + mybook.i_dimensions + "<BR>\n");
      out.print("ISBN: " + mybook.i_isbn +"</FONT></DT></DL><P>\n");

      url = "TPCW_shopping_cart_interaction?I_ID="+I_ID+"&QTY=1";
      if(SHOPPING_ID != null)
	  url = url + "&SHOPPING_ID=" + SHOPPING_ID;
      if(C_ID != null)
	  url = url + "&C_ID=" + C_ID;
      url = url + "&ADD_FLAG=Y";
      out.print("<CENTER> <A HREF=\""+
		res.encodeUrl(url));
      out.print("\">\n");

      out.print("<IMG SRC=\"../tpcw/Images/add_B.gif\"" +
		  " ALT=\"Add to Basket\"></A>\n");
      url = "TPCW_search_request_servlet";

      if(SHOPPING_ID != null){
	  url = url+"?SHOPPING_ID="+SHOPPING_ID;
	  if(C_ID!=null)
	      url = url + "&C_ID=" + C_ID;
      }
      else if(C_ID!=null)
	  url = url + "?C_ID=" + C_ID;
      
      out.print("<A HREF=\"" + res.encodeUrl(url));

      out.print("\"><IMG SRC=\"../tpcw/Images/search_B.gif\"" +
		" ALT=\"Search\"></A>\n");
      url = "TPCW_home_interaction";
      if(SHOPPING_ID != null){
	  url = url+"?SHOPPING_ID="+SHOPPING_ID;
	  if(C_ID!=null)
	      url = url + "&C_ID=" + C_ID;
      }
      else if(C_ID!=null)
	  url = url + "?C_ID=" + C_ID;
      
      out.print("<A HREF=\"" + res.encodeUrl(url)); 
      out.print("\"><IMG SRC=\"../tpcw/Images/home_B.gif\" " + 
		"ALT=\"Home\"></A>\n");

      url = "TPCW_admin_request_servlet?I_ID=" + I_ID;
      if(SHOPPING_ID != null)
	  url = url + "&SHOPPING_ID=" + SHOPPING_ID;
      if(C_ID != null)
	  url = url + "&C_ID=" + C_ID;
       
      out.print("<A HREF=\"" + res.encodeUrl(url));
      out.print("\"><IMG SRC=\"../tpcw/Images/update_B.gif\"" + 
		" ALT=\"Update\"></A>\n");

      out.print("</BODY> </HTML>\n");
      out.close();
      return;
  }
}
