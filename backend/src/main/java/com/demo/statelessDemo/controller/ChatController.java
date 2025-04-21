package com.demo.statelessDemo.controller;

import com.demo.statelessDemo.entities.ChatMessage;
import com.demo.statelessDemo.entities.MessageType;
import com.demo.statelessDemo.entities.People;
import com.demo.statelessDemo.services.PeopleServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/people")
public class ChatController {

    //Setup
    private final PeopleServices services;
    private final SimpMessagingTemplate messagingTemplate;

    LinkedHashMap<Long, String> hashMapQueue_1 = new LinkedHashMap<>();
    LinkedHashMap<Long, String> hashMapQueue_2 = new LinkedHashMap<>();
    LinkedHashMap<Long, String> hashMapQueue_3 = new LinkedHashMap<>();
    LinkedHashMap<Long, String> hashMapQueue_4 = new LinkedHashMap<>();
    LinkedHashMap<Long, String> hashMapQueue_5 = new LinkedHashMap<>();

    @Autowired
    public ChatController(PeopleServices services, SimpMessagingTemplate messagingTemplate) {
        this.services = services;
        this.messagingTemplate = messagingTemplate;
    }

    // Implementação: Restringir Post para Admin
    // selectInsertQueue é a função da lógica de inserir em uma fila conforme o departamento obtido por chooseQueue
    @PostMapping
    public People insertQueue(@RequestBody People people) {
        People savedPerson = services.insert(people);
        return selectInsertQueue(Objects.requireNonNull(chooseQueue(savedPerson)), savedPerson);
    }

    //Aqui o usuário vai ser deletado do banco, chamado no chat e depois removido da fila. Falta lógica de exceção.
    @DeleteMapping(value = "/getNext")
    public People getNextPeople() {

        Long id = hashMapQueue_1.firstEntry().getKey();

        People deletedPeople = services.delete(id);

        ChatMessage message = new ChatMessage();
        message.setType(MessageType.valueOf("CHAT"));
        message.setSender("User");
        message.setContent(String.valueOf(hashMapQueue_1.firstEntry()));

        hashMapQueue_1.remove(id);

        messagingTemplate.convertAndSend("/queue/public", message);

        return deletedPeople;

    }

    //Ver quantas pessoas estão na fila
    @GetMapping(value = "/getAll")
    public void getAllQueue() {

        ChatMessage message = new ChatMessage();

        Set<Map.Entry<Long, String>> entrySet = hashMapQueue_1.entrySet();

        Iterator<Map.Entry<Long, String>> it = entrySet.iterator();

        while (it.hasNext()) {
            message.setType(MessageType.valueOf("CHAT"));
            message.setSender("User");
            message.setContent(String.valueOf(it.next()));

            messagingTemplate.convertAndSend("/queue/public", message);
        }

    }


    private LinkedHashMap<Long, String> chooseQueue(People people) {

        String department = String.valueOf(people.getDepartment());

        return switch (department) {
            case ("DEPARTAMENTO_1") -> hashMapQueue_1;
            case ("DEPARTAMENTO_2") -> hashMapQueue_2;
            case ("DEPARTAMENTO_3") -> hashMapQueue_3;
            case ("DEPARTAMENTO_4") -> hashMapQueue_4;
            case ("DEPARTAMENTO_5") -> hashMapQueue_5;
            default -> null;
        };

    }

    //Essa função é responsável pela lógica de todas as queue
    private People selectInsertQueue(LinkedHashMap<Long, String> selectedQueue, People people) {

        //Se estiver vazia ele vai notificar que alguém foi adicionado
        if (selectedQueue.isEmpty()) {

            selectedQueue.put(people.getId(), people.getName());

            ChatMessage message = new ChatMessage ();

            message.setType(MessageType.valueOf("CHAT"));
            message.setSender("Admin");
            message.setContent("Pessoa " + people.getName() + " adicionada na fila");

            //Separar os departamentos
            String lowerCase = String.valueOf(people.getDepartment());

            messagingTemplate.convertAndSend("/queue/" + lowerCase.toLowerCase(), message);

            return people;
        }

        selectedQueue.put(people.getId(), people.getName());
        return people;
    }



}