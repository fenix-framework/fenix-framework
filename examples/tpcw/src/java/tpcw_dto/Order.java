package tpcw_dto;
/*
 * Order.java - Order class Stores the important pertinent to a
 *              single order
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

import java.util.Date;
import java.util.Vector;
import java.sql.*;

//Glorified struct to pass order information from the DB to servlets

public class Order {
    public Order() {}
    public Order(ResultSet rs) {
	try {
	    o_id = rs.getInt("o_id");
	    c_fname = rs.getString("c_fname");
	    c_lname = rs.getString("c_lname");
	    c_passwd = rs.getString("c_passwd");
	    c_uname = rs.getString("c_uname");
	    c_phone = rs.getString("c_phone");
	    c_email = rs.getString("c_email");
	    o_date = rs.getDate("o_date");
	    o_subtotal = rs.getDouble("o_sub_total");
	    o_tax = rs.getDouble("o_tax");
	    o_total = rs.getDouble("o_total");
	    o_ship_type = rs.getString("o_ship_type");
	    o_ship_date = rs.getDate("o_ship_date");
	    o_status = rs.getString("o_status");
	    cx_type = rs.getString("cx_type");
	    
	    bill_addr_street1 = rs.getString("bill_addr_street1");
	    bill_addr_street2 = rs.getString("bill_addr_street2");
	    bill_addr_state = rs.getString("bill_addr_state");
	    bill_addr_zip = rs.getString("bill_addr_zip");
	    bill_co_name = rs.getString("bill_co_name");
	    
	    ship_addr_street1 = rs.getString("ship_addr_street1");
	    ship_addr_street2 = rs.getString("ship_addr_street2");
	    ship_addr_state = rs.getString("ship_addr_state");
	    ship_addr_zip = rs.getString("ship_addr_zip");
	    ship_co_name = rs.getString("ship_co_name");
	} catch (java.lang.Exception ex) {
	    ex.printStackTrace();
	}
    }

    public int o_id;
    public String c_fname;
    public String c_lname;
    public String c_passwd;
    public String c_uname;
    public String c_phone;
    public String c_email;
    public Date o_date;
    public double o_subtotal;
    public double o_tax;
    public double o_total;
    public String o_ship_type;
    public Date o_ship_date;
    public String o_status;

    //Billing address
    public String bill_addr_street1;
    public String bill_addr_street2;
    public String bill_addr_state;
    public String bill_addr_zip;
    public String bill_co_name;
    
    //Shipping address
    public String ship_addr_street1;
    public String ship_addr_street2;
    public String ship_addr_state;
    public String ship_addr_zip;
    public String ship_co_name;
    
    public String cx_type;
}
