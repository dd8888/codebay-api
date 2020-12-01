package codebayapi.demo.controller;

import codebayapi.demo.entity.User;
import codebayapi.demo.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class UserController {
    private UserRepo userRepository;

    @Autowired
    public UserController(UserRepo userRepository) {
        this.userRepository = userRepository;
    }
    @RequestMapping(method = RequestMethod.GET)
    public List<User> getActiveUsers(){
        return userRepository.findAll();
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<String> getCitiesStartingX(){
        List<String> cities = new ArrayList<>();
        return cities;
    }
    @RequestMapping(method = RequestMethod.GET)
    public List<User> getUsersOrderedByCreation(){
        return userRepository.findAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public void addUser(@RequestBody User newUserRequest){
        User user = new User();
        user.setName(newUserRequest.getName());
        user.setSurname(newUserRequest.getSurname());
        user.setBirthday(newUserRequest.getBirthday());
        user.setCity(newUserRequest.getCity());
        user.setCreation_date(getCurrentDate());
        userRepository.save(user);
    }

    public String getCurrentDate(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }
}
