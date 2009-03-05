package pt.ist.fenixframework.example.tpcw.domain.dto;

import java.util.Comparator;
import java.util.Map;

import pt.ist.fenixframework.example.tpcw.domain.Book;

public class BookComparatorByOrderQuantityDesc implements Comparator<Map.Entry<Book,Integer>> {
    
    public int compare(Map.Entry<Book,Integer> e1, Map.Entry<Book,Integer> e2) {
	return e2.getValue()-e1.getValue();
    }
}
