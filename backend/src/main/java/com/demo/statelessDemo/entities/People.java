package com.demo.statelessDemo.entities;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "pessoas")
public class People {

    @Override
    public String toString() {
        return "People{" +
                "Id= " + Id +
                ", name= '" + name + '\'' +
                '}';
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    @Column(name = "nome")
    private String name;

    public People(){}

    public People(long id, String name) {
        Id = id;
        this.name = name;
    }

    public long getId() {return Id;}

    public void setId(long id) {Id = id;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        People people = (People) o;
        return Id == people.Id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Id);
    }
}
