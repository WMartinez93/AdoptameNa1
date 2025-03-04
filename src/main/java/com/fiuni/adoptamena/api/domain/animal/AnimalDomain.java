package com.fiuni.adoptamena.api.domain.animal;

import com.fiuni.adoptamena.api.domain.base.BaseDomain;

import lombok.*;
import jakarta.persistence.*;
import java.io.Serial;

@Entity
@Table(name = "animals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnimalDomain implements BaseDomain {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_animal", nullable = false, unique = true)
    private Integer id;

    @Column(name = "str_name", length = 100)
    private String name;

    @Column(name = "bool_deleted")
    private Boolean isDeleted;

}
