package test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Author extends Author_Base {

    public Author() {
        super();

        toString();
    }

    public Author(int id, int age) {
        this();
        setId(id);
        setAge(age);
    }
    
    @Override
    public Set<Book> getBookSet() {
        HashSet<Book> bookSet = new HashSet<>();
        for(Book book : super.getBookSet()){
            if(book.getAuthor().getId() % 3 == 0){
                bookSet.add(book);
            }
        }
        return bookSet;
    }
    
    @Override
    public String toString() {
        return "Author " + getId();
    }
}
