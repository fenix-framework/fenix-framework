package pt.ist.fenixframework.example.tpcw.domain;

public class Author extends Author_Base {
    
  public  Author(String fname, String lname, String mname, java.sql.Date dob, String bio, int a_id) {
    super();
    setFname(fname);
    setLname(lname);
    setMname(mname);
    setDob(dob);
    setBio(bio);
    setA_id(a_id);
  }
}
