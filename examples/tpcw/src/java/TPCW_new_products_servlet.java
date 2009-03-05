/* 
 * TPCW_new_products_servlet.java - Servlet Class implements new products
 *                                  web interaction.
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

public class TPCW_new_products_servlet extends HttpServlet {
    
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {
      PrintWriter out = res.getWriter();
      int i;
      String url;
      HttpSession session = req.getSession(false);
      String subject = req.getParameter("subject");
      String C_ID = req.getParameter("C_ID");
      String SHOPPING_ID = req.getParameter("SHOPPING_ID");
      
      // Set the content type of this servlet's result.
      res.setContentType("text/html");
      out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD W3 HTML//EN\">\n"); 
      out.print("<HTML><HEAD><TITLE> New " + subject + "</TITLE></HEAD>\n"); 
      out.print("<BODY BGCOLOR=\"#ffffff\">\n"); 
      out.print("<H1 ALIGN=\"center\">TPC Web Commerce"+ 
		" Benchmark (TPC-W)</H1>\n"); 
      out.print("<P ALIGN=\"center\">\n");
      out.print("<IMG SRC=\"../tpcw/Images/tpclogo.gif\" ALIGN=\"BOTTOM\"" +
		" BORDER=\"0\" WIDTH=\"288\" HEIGHT=\"67\"> </P> <P></P>\n") ;
	  
      out.print("<H2 ALIGN=\"center\">New Products Page - Subject: " +
		subject + "</H2>\n"); 
	  
      //Display promotions
      TPCW_promotional_processing.DisplayPromotions(out, req, res,-1);

      //Display new products
      
      out.print("<TABLE BORDER=\"1\" CELLPADDING=\"1\" CELLSPACING=\"1\">\n");
      out.print("<TR> <TD WIDTH=\"30\"></TD>\n");
      out.print("<TD><FONT SIZE=\"+1\">Author</FONT></TD>\n"); 
      out.print("<TD><FONT SIZE=\"+1\">Title</FONT></TD></TR>\n");
      
      //Need to insert code here to get new products from the database,
      //and then spit them out in html to complete the table
      
      Vector books = TPCW_Database.getNewProducts(subject);
      for(i = 0; i < books.size(); i++){
	  ShortBook book = (ShortBook) books.elementAt(i);
	  out.print("<TR><TD>" + (i+1) + "</TD>\n");
	  out.print("<TD><I>"+ book.a_fname +" "+ book.a_lname +"</I></TD>\n");
	  url = "./TPCW_product_detail_servlet?I_ID="
	      + String.valueOf(book.i_id);
	  if(SHOPPING_ID != null)
	      url = url + "&SHOPPING_ID=" + SHOPPING_ID;
	  if(C_ID != null)
	      url = url + "&C_ID=" + C_ID;
	  out.print("<TD><A HREF=\"" + res.encodeUrl(url));
	  out.print("\">" + book.i_title + "</A></TD></TR>\n");
      }

      out.print("</TABLE><P><CENTER>\n");

      url = "TPCW_shopping_cart_interaction?ADD_FLAG=N";
      if(SHOPPING_ID != null)
	  url = url + "&SHOPPING_ID=" + SHOPPING_ID;
      if(C_ID != null)
	  url = url + "&C_ID=" + C_ID;
      out.print("<A HREF=\""+ res.encodeUrl(url));
      out.print("\"><IMG SRC=\"../tpcw/Images/shopping_cart_B.gif\" " +
		"ALT=\"Shopping Cart\"></A>\n");

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
      out.print("</BODY> </HTML>\n");
      out.close();
      return;
    }
}
