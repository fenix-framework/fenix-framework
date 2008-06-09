package pt.ist.fenixframework.example.bankbench.hib;

import javax.persistence.*;


@MappedSuperclass
public class PersistentObject {
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Version
    private Long objVersion;
}
