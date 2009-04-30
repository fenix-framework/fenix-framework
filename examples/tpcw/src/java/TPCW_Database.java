/* 
 * TPCW_Database.java - Contains all of the code involved with database
 *                      accesses, including all of the JDBC calls. These
 *                      functions are called by many of the servlets.
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
import java.net.URL;
import java.sql.*;
import java.lang.Math.*;
import java.util.*;
import java.sql.Date;
import java.sql.Timestamp;

import tpcw_dto.*;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.example.tpcw.database.DataAccess;
import jvstm.Atomic;

import pt.ist.fenixframework.example.tpcw.domain.Root;
import pt.ist.fenixframework.example.tpcw.domain.ShoppingCart;

public class TPCW_Database {
    static {
	initializeFenixFramework();
    }

    static void initializeFenixFramework() {
        Config config = new Config() {{
            domainModelPath = "/tpcw.dml";
   	    dbAlias = "//localhost:3306/tpcwFenix";
            dbUsername = "tpcw";
            dbPassword = "tpcw";
	    // updateRepositoryStructureIfNeeded = true;
            rootClass = Root.class;
        }};
        FenixFramework.initialize(config);
    }

    @Atomic
    public static String[] getName(int c_id) {
	return DataAccess.getName(c_id);
    }

    @Atomic
    public static Book getBook(int i_id) {
	return DataAccess.getBook(i_id);
    }

    @Atomic
    public static Customer getCustomer(String UNAME){
	return DataAccess.getCustomer(UNAME);
    }

    @Atomic
    public static Vector doSubjectSearch(String search_key) {
	return DataAccess.doSubjectSearch(search_key);
    }

    @Atomic
    public static Vector doTitleSearch(String search_key) {
	return DataAccess.doTitleSearch(search_key);
    }

    @Atomic
    public static Vector doAuthorSearch(String search_key) {
	return DataAccess.doAuthorSearch(search_key);
    }

    @Atomic
    public static Vector getNewProducts(String subject) {
	return DataAccess.getNewProducts(subject);
    }

    @Atomic
    public static Vector getBestSellers(String subject) {
	return DataAccess.getBestSellers(subject);
    }

    @Atomic
    public static void getRelated(int i_id, Vector i_id_vec, Vector i_thumbnail_vec) {
      DataAccess.getRelated(i_id, i_id_vec, i_thumbnail_vec);
    }

    @Atomic
    public static void adminUpdate(int i_id, double cost, String image, String thumbnail) {
	DataAccess.adminUpdate(i_id, cost, image, thumbnail);
    }

    @Atomic
    public static String GetUserName(int C_ID){
	return DataAccess.GetUserName(C_ID);
    }

    @Atomic
    public static String GetPassword(String C_UNAME){
	return DataAccess.GetPassword(C_UNAME);
    }

    @Atomic
    public static Order GetMostRecentOrder(String c_uname, Vector order_lines){
	return DataAccess.GetMostRecentOrder(c_uname, order_lines);
    }

    // ********************** Shopping Cart code below ************************* 

    // Called from: TPCW_shopping_cart_interaction 
    @Atomic
    public static int createEmptyCart(){
	return DataAccess.createEmptyCart();
    }

    @Atomic
    public static Cart doCart(int SHOPPING_ID, Integer I_ID, Vector ids, Vector quantities) {
	return DataAccess.doCart(SHOPPING_ID, I_ID, ids, quantities);
    }

    //This function finds the shopping cart item associated with SHOPPING_ID
    //and I_ID. If the item does not already exist, we create one with QTY=1,
    //otherwise we increment the quantity.
    
    @Atomic
    public static Cart getCart(int SHOPPING_ID, double c_discount) {
	return DataAccess.getCartDTO(SHOPPING_ID, c_discount);
    }


    // ************** Customer / Order code below *************************

    @Atomic
    public static void refreshSession(int C_ID) {
	DataAccess.refreshSession(C_ID);
    }

    @Atomic
    public static Customer createNewCustomer(Customer cust) {
	return DataAccess.createNewCustomer(cust);
    }

    //BUY CONFIRM 

    @Atomic
    public static BuyConfirmResult doBuyConfirm(int shopping_id, int customer_id, String cc_type, long cc_number, String cc_name,
						Date cc_expiry, String shipping) {
	return DataAccess.doBuyConfirm(shopping_id, customer_id, cc_type, cc_number, cc_name, cc_expiry, shipping);
    }

    @Atomic
    public static BuyConfirmResult doBuyConfirm(int shopping_id, int customer_id, String cc_type, long cc_number, String cc_name,
						Date cc_expiry, String shipping, String street_1, String street_2, String city,
						String state, String zip, String country) {
	return DataAccess.doBuyConfirm(shopping_id, customer_id, cc_type, cc_number, cc_name, cc_expiry, shipping,
				       street_1, street_2, city, state, zip, country);
    }
    
}

