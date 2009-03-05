/* 
 * TPCW_buy_confirm_servlet.java - Servlet Class implements the buy
 *                                 confirm web interaction.
 *
 * CAVEAT: This servlet does not fully adhere to the TPC-W
 *         specification for the buy confirm web interaction, in that
 *         it does not communicate with an external payment getway
 *         emulator.
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
import java.sql.Date;

import tpcw_dto.*;

//DATABASE CONNECTIVITY NEEDED: Lots!
//1. Given a SHOPPING_ID, I need a way to get the SCL_ID, SC_COST, and 
//   SC_QTY for each item in the shopping cart, as well as the SC_SUBTOTAL,
//   SC_TAX, SC_SHIP_COST, and SC_TOTAL for the entire cart.
//
//2. Given a C_ID, I need a DB call that returns the C_FNAME, C_LNAME,
//   C_DISCOUNT, and C_ADDR_ID from the customer table. 
//
//3. I need a function which takes, as parameters, STREET_1, STREET_2, CITY,
//   STATE, ZIP, and COUNTRY. The DB code should search the ADDRESS table for
//   and address which matches these parameters. If one does not exist,
//   a new row is added to the ADDRESS table with these parameters as columns.
//   The lookup and the insertion must happen in a single DB transaction
//   This is described in section 2.7.3.2


//4. I also need a DB function which does what is described in section 
//   2.7.3.3, given the following parameters: C_ID, SC_SUB_TOTAL, SC_TOTAL,
//   SHIPPING, C_ADDR_ID, and ADDR_ID. This involves putting the order in
//   The ORDER DB table, creating a message to the SSL PGE (which is currently
//   never sent), putting an entry in the CC table, and then clearing all of
//   the items from the cart. This function should return the newly created
//   unique O_ID.
//   
 
public class TPCW_buy_confirm_servlet extends HttpServlet {
    
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {
      int i;
      String url;
      PrintWriter out = res.getWriter();
    // Set the content type of this servlet's result.
      res.setContentType("text/html");

      HttpSession session = req.getSession(false);

      String SHOPPING_IDstr = req.getParameter("SHOPPING_ID");
      int SHOPPING_ID = Integer.parseInt(SHOPPING_IDstr);
      String C_IDstr = req.getParameter("C_ID");
      int C_ID = Integer.parseInt(C_IDstr);

      String CC_TYPE = req.getParameter("CC_TYPE");
      String CC_NUMBERstr = req.getParameter("CC_NUMBER");
      long CC_NUMBER = Long.parseLong(CC_NUMBERstr);
      String CC_NAME = req.getParameter("CC_NAME");
      String CC_EXPIRYstr = req.getParameter("CC_EXPIRY");
      java.util.Date CC_EXPIRY = new java.util.Date(CC_EXPIRYstr);
      String SHIPPING = req.getParameter("SHIPPING");

      String STREET_1 = req.getParameter("STREET_1");
      BuyConfirmResult result = null;
      if(!STREET_1.equals("")) {
	  String STREET_2 = req.getParameter("STREET_2");
	  String CITY = req.getParameter("CITY");
	  String STATE = req.getParameter("STATE");
	  String ZIP = req.getParameter("ZIP");
	  String COUNTRY = req.getParameter("COUNTRY");
	  result = TPCW_Database.doBuyConfirm(SHOPPING_ID, C_ID,CC_TYPE, 
					      CC_NUMBER, CC_NAME, 
					      new java.sql.Date(CC_EXPIRY.getTime()), 
					      SHIPPING, STREET_1, STREET_2,
					      CITY, STATE, ZIP, COUNTRY);
      }
      else result = TPCW_Database.doBuyConfirm(SHOPPING_ID, C_ID, 
					       CC_TYPE,CC_NUMBER,CC_NAME, 
					       new java.sql.Date(CC_EXPIRY.getTime()),
					       SHIPPING);


      //Make Database call to read the current countent of the shopping
      //cart, etc

      //Make a call to database to update the order table
      //and do ssl stuff, (We are not currently passing info to/from a PGE).

      //Print out the HTML page
      out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD W3 HTML//EN\"> <HTML>\n");
      out.print("<HEAD><TITLE>Order Confirmation</TITLE></HEAD> ");
      out.print("<BODY BGCOLOR=\"#FFFFFF\">"); 
      out.print("<H1 ALIGN=\"CENTER\">TPC Web Commerce Benchmark " + 
		"(TPC-W)</H1>\n");
      out.print("<H2 ALIGN=\"CENTER\">Buy Confirm Page</H2>\n");
      out.print("<BLOCKQUOTE><BLOCKQUOTE><BLOCKQUOTE><BLOCKQUOTE>\n");
      out.print("<H2 ALIGN=\"LEFT\">Order Information:</H2>\n");
      out.print("<TABLE BORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"0\">\n");
      out.print("<TR><TD><B>Qty</B></TD><TD><B>Product</B></TD></TR> ");

      //For each item in the shopping cart, print out its contents
      for(i = 0; i < result.cart.lines.size(); i++){
	  CartLine line = (CartLine) result.cart.lines.elementAt(i);
	  out.print("<TR><TD VALIGN=\"TOP\">" + line.scl_qty+ "</TD>\n");
	  out.print("<TD VALIGN=\"TOP\">Title:<I>" + line.scl_title 
		    + "</I> - Backing: " +line.scl_backing 
		    + "<BR>SRP. $" + line.scl_srp + 
		    "<FONT COLOR=\"#aa0000\"><B>Your Price: $" + 
		    line.scl_cost + "</FONT> </TD></TR>\n");
      }
      out.print("</TABLE><H2 ALIGN=\"LEFT\">Your Order has been processed.</H2>\n"); 
      out.print("<TABLE BORDER=\"1\" CELLPADDING=\"5\" CELLSPACING=\"0\">\n");
      out.print("<TR><TD><H4>Subtotal with discount:</H4></TD>\n");
      out.print("<TD> <H4>$" + result.cart.SC_SUB_TOTAL +"</H4></TD></TR>");
      out.print("<TR><TD><H4>Tax (8.25%):</H4></TD>\n");
      out.print("<TD><H4>$" + result.cart.SC_TAX + "</H4></TD></TR>\n");
      out.print("<TR><TD><H4>Shipping &amp; Handling:</H4></TD>\n");
      out.print("<TD><H4>$"+ result.cart.SC_SHIP_COST + "</H4></TD></TR>\n");
      out.print("<TR><TD> <H4>Total:</H4></TD>\n");
      out.print("<TD><H4>$" + result.cart.SC_TOTAL + "</H4></TD></TR></TABLE>\n");
      out.print("<P><BR></P><H2>Order Number: " + result.order_id + "</H2>\n");
      out.print("<H1>Thank you for shopping at TPC-W</H1> <P></P>\n");

      //Add the buttons
      url = "TPCW_search_request_servlet?SHOPPING_ID=" + SHOPPING_ID;
      if(C_IDstr != null)
	  url = url  + "&C_ID=" + C_IDstr;
      out.print("<CENTER><P><A HREF=\"" + 
		res.encodeUrl(url));
      out.print("\"><IMG SRC=\"../tpcw/Images/search_B.gif\"" +
		" ALT=\"Search\"></A>\n");
      
      url = "TPCW_home_interaction?SHOPPING_ID=" + SHOPPING_ID;
      if(C_IDstr != null)
	  url = url  + "&C_ID=" + C_IDstr;
      
      out.print("<A HREF=\"" + res.encodeUrl(url));
      out.print("\"><IMG SRC=\"../tpcw/Images/home_B.gif\" ALT=\"Home\"></A>\n");
      out.print("</CENTER></BLOCKQUOTE></BLOCKQUOTE></BLOCKQUOTE>" + 
		"</BLOCKQUOTE></BODY></HTML>");
      out.close();
      return;
  }

}
