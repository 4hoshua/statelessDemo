package com.demo.statelessDemo.controller;

import com.demo.statelessDemo.entities.ChatMessage;
import com.demo.statelessDemo.entities.MessageType;
import com.demo.statelessDemo.entities.People;
import com.demo.statelessDemo.services.PeopleServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/people")
public class ChatController {

    //Setup
    private final PeopleServices services;
    private final SimpMessagingTemplate messagingTemplate;

    LinkedHashMap<Long, String> hashMapQueue = new LinkedHashMap<>();

    @Autowired
    public ChatController(PeopleServices services, SimpMessagingTemplate messagingTemplate) {
        this.services = services;
        this.messagingTemplate = messagingTemplate;
    }


    //A ideia é que o Admin vai fazer um post com o usuário fornecido e adicionar na fila.
    @PostMapping
    public People insertQueue(@RequestBody People people) {

        People savedPerson = services.insert(people);

        //Se estiver vazia ele vai notificar que alguém foi adicionado.
        if (hashMapQueue.isEmpty()) {

            hashMapQueue.put(people.getId(), people.getName());

            ChatMessage message = new ChatMessage();
            message.setType(MessageType.valueOf("CHAT"));
            message.setSender("Admin");
            message.setContent("Pessoa " + savedPerson.getName() + " adicionada na fila!");

            //Implementação: Fazer com que public varie conforme os departamentos
            messagingTemplate.convertAndSend("/topic/public", message);

            return savedPerson;
        }

        hashMapQueue.put(people.getId(), people.getName());
        return savedPerson;

    }

    //Aqui o usuário vai ser deletado do banco, chamado no chat e depois removido da fila. Falta lógica de exceção.
    @DeleteMapping(value = "/getNext")
    public People getNextPeople() {

        Long id = hashMapQueue.firstEntry().getKey();

        People deletedPeople = services.delete(id);

        ChatMessage message = new ChatMessage();
        message.setType(MessageType.valueOf("CHAT"));
        message.setSender("User");
        message.setContent(String.valueOf(hashMapQueue.firstEntry()));

        hashMapQueue.remove(id);

        messagingTemplate.convertAndSend("/topic/public", message);

        return deletedPeople;

    }

    //Ver quantas pessoas estão na fila
    @GetMapping(value = "/getAll")
    public void getAllQueue() {

        ChatMessage message = new ChatMessage();

        Set<Map.Entry<Long, String>> entrySet = hashMapQueue.entrySet();

        Iterator<Map.Entry<Long, String>> it = entrySet.iterator();

        while (it.hasNext()) {
            message.setType(MessageType.valueOf("CHAT"));
            message.setSender("User");
            message.setContent(String.valueOf(it.next()));

            messagingTemplate.convertAndSend("/topic/public", message);
        }

    }
}