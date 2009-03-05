/* 
 * TPCW_order_display_servlet.java - Servlet Class implements order
 *                                   display servlet.
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
import java.util.*;

import tpcw_dto.*;

public class TPCW_order_display_servlet extends HttpServlet {
    
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {

      PrintWriter out = res.getWriter();
      HttpSession session = req.getSession(false);
      String C_ID = req.getParameter("C_ID");
      String SHOPPING_ID = req.getParameter("SHOPPING_ID");
      String url;

      // Set the content type of this servlet's result.
      res.setContentType("text/html");

     out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD W3 HTML//EN\">\n"); 
     out.print("<HTML><HEAD><TITLE>TPC-W Order Display Page</TITLE></HEAD>\n");
     out.print("<BODY BGCOLOR=\"#FFFFFF\"><H1 ALIGN=\"CENTER\">" + 
	       "TPC Web Commerce Benchmark (TPC-W)</H1>\n"); 
     out.print("<H2 ALIGN=\"CENTER\">Order Display Page</H2>\n");
     out.print("<BLOCKQUOTE> <BLOCKQUOTE> <BLOCKQUOTE> <BLOCKQUOTE> <HR>\n"); 

     String uname = req.getParameter("UNAME");
     String passwd = req.getParameter("PASSWD");
     if(uname!= null && passwd!=null){
	 
	 String storedpasswd = TPCW_Database.GetPassword(uname);
	 if(!storedpasswd.equals(passwd)){
	     out.print("Error: Incorrect password.\n");
	 }
	 else {
	     Vector lines = new Vector();
	     Order order = TPCW_Database.GetMostRecentOrder(uname, lines);
	     if(order!=null)
		 printOrder(order, lines,out);
	     else out.print("User has no order!\n");
	 }	 
	 
     }
     else out.print("Error:TPCW_order_display_servlet, "
		    + "uname and passwd not set!.\n");
     
     //Print out the buttons that are on the bottom of the page
    out.print("<CENTER>\n");
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
  }

  private void printOrder(Order order, Vector lines, PrintWriter out){
      int i;
      out.print("<P>Order ID:" + order.o_id +"<BR>\n");
      out.print("Order Placed on " + order.o_date +"<BR>\n");
      out.print("Shipping Type:"+ order.o_ship_type + "<BR>\n");
      out.print("Ship Date: "+ order.o_ship_date + "<BR>\n");
      out.print("Order Subtotal: "+ order.o_subtotal +"<BR>\n");
      out.print("Order Tax: "+ order.o_tax +"<BR>\n");
      out.print("Order Total:"+ order.o_total +"<BR></P>\n");
      
      out.print("<TABLE BORDER=\"0\" WIDTH=\"80%\">\n");
      out.print("<TR><TD><B>Bill To:</B></TD><TD><B>Ship To:</B></TD></TR>");
      out.print("<TR><TD COLSPAN=\"2\"> <H4>"+ order.c_fname + " " + 
		order.c_lname + "</H4></TD></TR>\n");
      out.print("<TR><TD WIDTH=\"50%\"><ADDRESS>" + order.ship_addr_street1
		+"<BR>\n");
      out.print(order.ship_addr_street2 + "<BR>\n");
      out.print(order.ship_addr_state+ " " + order.ship_addr_zip + "<BR>\n");
      out.print(order.ship_co_name + "<BR><BR>\n");
      out.print("Email: " + order.c_email + "<BR>\n");
      out.print("Phone: " + order.c_phone +"</ADDRESS><BR><P>\n");
      out.print("Credit Card Type: " + order.cx_type + "<BR>\n");
      out.print("Order Status: " + order.o_status +"</P></TD>\n");
      out.print("<TD VALIGN=\"TOP\" WIDTH=\"50%\"><ADDRESS>" 
		+ order.bill_addr_street1 + 
		"<BR>\n");
      out.print(order.bill_addr_street2 + "<BR>\n");
      out.print(order.bill_addr_state + " " + order.bill_addr_zip + "<BR>\n");
      out.print(order.bill_co_name + "\n");
      out.print("</ADDRESS></TD></TR></TABLE>");
      out.print("</BLOCKQUOTE></BLOCKQUOTE></BLOCKQUOTE></ BLOCKQUOTE>");
      
      //Print out the list of items
      out.print("<CENTER><TABLE BORDER=\"1\" CELLPADDING=\"5\"" + 
		" CELLSPACING=\"0\">\n");
      out.print("<TR><TD><H4>Item #</H4></TD>");
      out.print("<TD><H4>Title</H4></TD>");
      out.print("<TD> <H4>Cost</H4></TD>");
      out.print("<TD> <H4>Qty</H4></TD> ");
      out.print("<TD> <H4>Discount</H4></TD>");
      out.print("<TD> <H4>Comment</H4></TD></TR>\n");
      if(lines!=null){
	  for(i = 0; i < lines.size(); i++) {
	  OrderLine line = (OrderLine) lines.elementAt(i);
	  out.print("<TR>");
	  out.print("<TD> <H4>" + line.ol_i_id +  "</H4></TD>\n");
	  out.print("<TD VALIGN=\"top\"><H4>" + line.i_title + 
		    "<BR>Publisher: " + line.i_publisher +
		    "</H4></TD>\n");
	  out.print("<TD> <H4>" + line.i_cost + "</H4></TD>\n"); //Cost
	  out.print("<TD> <H4>"+ line.ol_qty + "</H4></TD>\n"); //Qty
	  out.print("<TD> <H4>"+ line.ol_discount + "</H4></TD>\n"); //Discount
	  out.print("<TD> <H4>"+ line.ol_comments + "</H4></TD></TR>\n"); 
	  }
      }
      out.print("</TABLE><BR></CENTER>\n");
      out.close();
      return;
  }
}
