package com.demo.statelessDemo.services;

import com.demo.statelessDemo.entities.People;
import com.demo.statelessDemo.repository.PeopleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PeopleServices {

    @Autowired
    PeopleRepository repository;

    public People findById(Long id) {
        Optional<People> people = repository.findById(id);
        return people.get();
    }

    public People insert(People people) {
        people = repository.save(people);
        return people;
    }

    public People delete(Long id) {
        repository.deleteById(id);
        return null;
    }
}
