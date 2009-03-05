/* 
 * TPCW_shopping_cart_interaction.java - Servlet class implements the 
 *                                       shopping cart web interaction.
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
import java.util.Vector;

import tpcw_dto.*;

public class TPCW_shopping_cart_interaction extends HttpServlet {
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {
	Cart cart;
	String url;
	HttpSession session = req.getSession(false);
	 
	PrintWriter out = res.getWriter();
    
	// Set the content type of this servlet's result.
	res.setContentType("text/html");
	String C_IDstr = req.getParameter("C_ID");	

	String SHOPPING_IDstr = req.getParameter("SHOPPING_ID");
	int SHOPPING_ID;
	if(SHOPPING_IDstr == null) {
	    SHOPPING_ID = TPCW_Database.createEmptyCart();
	} else {
	    SHOPPING_ID = Integer.parseInt(SHOPPING_IDstr);
	}
	
	String add_flag = req.getParameter("ADD_FLAG");
	Integer I_ID;
       
	if(add_flag.equals("Y")){
	    String I_IDstr = req.getParameter("I_ID");
	    if(I_IDstr == null){
		System.out.println("ERROR IN SHOPPING CART, add_flag==Y!");
		out.print("Error- need to specify an I_ID!</BODY></HTML>\n");
		return;
	    }
	    I_ID = new Integer(Integer.parseInt(I_IDstr));
	} else {
	    I_ID = null;
	}

	//We need to parse an arbitrary number of I_ID/QTR pairs from
	//the url line.
	Vector quantities = new Vector();
	Vector ids = new Vector();
	int i = 0;
	String curr_QTYstr;
	String curr_I_IDstr;
	
	curr_QTYstr = req.getParameter("QTY_" + i);
	curr_I_IDstr = req.getParameter("I_ID_" + i);
	while(curr_I_IDstr != null){
	    ids.addElement(curr_I_IDstr);
	    quantities.addElement(curr_QTYstr);
	    i++;
	    curr_QTYstr = req.getParameter("QTY_" + i);
	    curr_I_IDstr = req.getParameter("I_ID_" + i);
	}

	cart = TPCW_Database.doCart(SHOPPING_ID, I_ID, ids, quantities);

	//Add the top part of the HTML
	
	out.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD W3 HTML//EN\">\n");
	out.print("<HTML><!--Shopping Cart--> <HEAD><TITLE>TPC W Shopping Cart</TITLE></HEAD> \n");
	out.print("<BODY BGCOLOR=\"#ffffff\">\n");
	out.print("<H1 ALIGN=\"center\">TPC Web Commerce Benchmark " +
		  "(TPC-W)</H1>\n");
	out.print("<CENTER><IMG SRC=\"../tpcw/Images/tpclogo.gif\" " +
		  "ALIGN=\"BOTTOM\" BORDER=\"0\" WIDTH=\"288\" " + 
		  "HEIGHT=\"67\"></CENTER>\n");
	out.print("<H2 ALIGN=\"center\">Shopping Cart Page</H2>\n");


	//Print out the promotional processing stuff
	TPCW_promotional_processing.DisplayPromotions(out, req, res,
						      SHOPPING_ID);

	//Display the shopping cart contents
	out.print("<FORM ACTION=\"TPCW_shopping_cart_interaction;@sessionIdString@"+
		  req.getRequestedSessionId()+"\" METHOD=\"get\">\n");
	out.print("<CENTER><P></P><TABLE BORDER=\"0\">\n");
	out.print("<TR><TD><B>Qty</B></TD><TD><B>Product</B></TD></TR>\n"); 
	
	//Print out the entries in the shopping cart
	for(i = 0; i < cart.lines.size(); i++){
	    CartLine line = (CartLine) cart.lines.elementAt(i);
	    out.print("<TR><TD VALIGN=\"top\">\n");
	    out.print("<INPUT TYPE=HIDDEN NAME=\"I_ID_" + i + "\" value = \"" +
		      line.scl_i_id + "\">\n");
	    out.print("<INPUT NAME=\"QTY_" + i +"\" SIZE=\"3\" VALUE=\"" +
		      line.scl_qty +"\"></TD>\n");
	    out.print("<TD VALIGN=\"top\">Title:<I>" + line.scl_title +
		      "</I> - Backing: " + line.scl_backing + "<BR>\n");
	    out.print("SRP. $" + line.scl_srp + "</B>\n");
	    out.print("<FONT COLOR=\"#aa0000\"><B>Your Price: $" + 
		      line.scl_cost + "</B></FONT></TD></TR>\n");
	}

	out.print("</TABLE><B><I>Subtotal price: "  + cart.SC_SUB_TOTAL + 
		  "</I></B>\n");
	url = "TPCW_customer_registration_servlet?SHOPPING_ID=" + SHOPPING_ID;
	if(C_IDstr != null)
	    url = url + "&C_ID=" + C_IDstr;
	out.print("<P><BR><A HREF=\"" + res.encodeUrl(url)); 
	out.print("\"><IMG SRC=\"../tpcw/Images/checkout_B.gif\"></A>\n");

	url = "TPCW_home_interaction?SHOPPING_ID=" + SHOPPING_ID; 
	if(C_IDstr != null)
	    url = url + "&C_ID=" + C_IDstr;
	out.print("<A HREF=\"" + res.encodeUrl(url)); 

	out.print("\"><IMG SRC=\"../tpcw/Images/home_B.gif\"></P></A>\n");
	out.print("<P>If you have changed the quantities and/or taken " + 
		  "anything out<BR> of your shopping cart, click here to " + 
		  "refresh your shopping cart:</P> ");
	//out.print("<INPUT TYPE=HIDDEN NAME=\"" + TPCW_Util.SESSION_ID + 
	//	   "\" value = \"" + req.getRequestedSessionId() + "\">\n");
	out.print("<INPUT TYPE=HIDDEN NAME=\"ADD_FLAG\" value = \"N\">\n");
	out.print("<INPUT TYPE=HIDDEN NAME=\"SHOPPING_ID\" value = \"" + 
		    SHOPPING_ID + "\">\n");
	if(C_IDstr != null)
	    out.print("<INPUT TYPE=HIDDEN NAME=\"C_ID\" value = \"" + 
		      C_IDstr + "\">\n");
	
	out.print("<P><INPUT TYPE=\"IMAGE\" NAME=\"Refresh Shopping Cart\"" + 
		  "SRC=\"../tpcw/Images/refresh_B.gif\"></P>\n");
	out.print("</CENTER></FORM></BODY></HTML>");
	out.close();
	return;
    }
}

