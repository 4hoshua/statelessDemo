package com.demo.statelessDemo.controller;

import com.demo.statelessDemo.entities.ChatMessage;
import com.demo.statelessDemo.entities.Department;
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

    //Implementação: Restringir Get para User
    //Ver quantas pessoas estão na fila
    @GetMapping(value = "/getAll/departamento_{department}")
    public void getAllQueuesDepartment(@PathVariable Integer department) {

        Department departmentEnum = Department.values()[department - 1];
        String lowerCase = String.valueOf(departmentEnum);
        getAllQueue(Objects.requireNonNull(chooseQueue(department)), lowerCase.toLowerCase());

    }

    //Implementação: Restringir Delete para User
    //Chamar o próximo e deletar do banco de dados e queue
    @DeleteMapping(value = "/getNext/departamento_{department}")
    public People getNextPeople(@PathVariable Integer department) {

        Department departmentEnum = Department.values()[department - 1];
        String lowerCase = String.valueOf(departmentEnum);
        return getNext(Objects.requireNonNull(chooseQueue(department)), lowerCase.toLowerCase());

    }


    //Selecionar Departamento com base em People ou Integer
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

    private LinkedHashMap<Long, String> chooseQueue(Integer departmentNumber) {

        return switch (departmentNumber) {
            case (1) -> hashMapQueue_1;
            case (2) -> hashMapQueue_2;
            case (3) -> hashMapQueue_3;
            case (4) -> hashMapQueue_4;
            case (5) -> hashMapQueue_5;
            default -> null;
        };

    }

    //Essa função é responsável pela lógica de todas as queues
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

    //Aplica a lógica para chamar todos da queue
    private void getAllQueue(LinkedHashMap<Long, String> selectedQueue, String departamentoRota) {

        ChatMessage message = new ChatMessage();

        Set<Map.Entry<Long, String>> entrySet = selectedQueue.entrySet();

        Iterator<Map.Entry<Long, String>> it = entrySet.iterator();

        while (it.hasNext()) {
            message.setType(MessageType.valueOf("CHAT"));
            message.setSender("User");
            message.setContent(String.valueOf(it.next()));

            messagingTemplate.convertAndSend("/queue/" + departamentoRota, message);
        }

    }

    //Aplica a lógica para chamar o próximo e deletar da queue e banco de dados
    private People getNext(LinkedHashMap<Long, String> selectedQueue, String departamentoRota) {

        Long id = selectedQueue.firstEntry().getKey();

        People deletedPeople = services.delete(id);

        ChatMessage message = new ChatMessage();
        message.setType(MessageType.valueOf("CHAT"));
        message.setSender("User");
        message.setContent("Chamando: " + String.valueOf(selectedQueue.firstEntry()));

        selectedQueue.remove(id);

        messagingTemplate.convertAndSend("/queue/"  + departamentoRota, message);

        return deletedPeople;
    }

}