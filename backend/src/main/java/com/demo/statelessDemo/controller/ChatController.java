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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

@RestController
@RequestMapping("/people")
public class ChatController {

    private final PeopleServices services;
    private final SimpMessagingTemplate messagingTemplate;

    Queue<String> queue = new LinkedList<>();

    @Autowired
    public ChatController(PeopleServices services, SimpMessagingTemplate messagingTemplate) {
        this.services = services;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public People insertQueue(@RequestBody People people) {

        //Criando a pessoa e persistindo no banco
        People savedPerson = services.insert(people);

        if (queue.isEmpty()) {

            queue.add(savedPerson.toString());

            ChatMessage message = new ChatMessage();
            message.setType(MessageType.valueOf("CHAT"));
            message.setSender("Admin");
            message.setContent("Pessoa " + savedPerson.getName() + " adicionada na fila!");

            messagingTemplate.convertAndSend("/topic/public", message);

            return savedPerson;
        }

        queue.add(savedPerson.toString());
        return savedPerson;

    }

    @DeleteMapping(value = "/getNext")
    public void getNextPeople() {

    }

    @GetMapping(value = "/getAll")
    public void getAllQueue() {

        ChatMessage message = new ChatMessage();

        for (String s : queue) {
            message.setType(MessageType.valueOf("CHAT"));
            message.setSender("System");
            message.setContent(s + " ");
            messagingTemplate.convertAndSend("/topic/public", message);
        }

    }

    @MessageMapping("/sendMessage")
    @SendTo("/topic/public")
    public ChatMessage handleChatMessage(@Payload ChatMessage message) {
        return message;
    }

    @MessageMapping("/addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage message,
                               SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", message.getSender());
        return message;
    }
}