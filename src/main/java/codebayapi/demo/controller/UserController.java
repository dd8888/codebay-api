package codebayapi.demo.controller;

import codebayapi.demo.entity.User;
import codebayapi.demo.entity.request.NewUserRequest;
import codebayapi.demo.repository.UserRepository;
import codebayapi.demo.utils.CreationDateComparator;
import codebayapi.demo.utils.TodaysDate;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController("/user")
public class UserController {
    private UserRepository userRepository;
    private final Bucket bucket;

    @Autowired
    public UserController(UserRepository userRepository) {
        Bandwidth limit = Bandwidth.classic(3, Refill.greedy(3, Duration.ofSeconds(10)));
        this.bucket = Bucket4j.builder()
                .addLimit(limit)
                .build();
        this.userRepository = userRepository;
    }

    @RequestMapping(value = "/activeUsers", method = RequestMethod.GET)
    public ResponseEntity getActiveUsers(){
        List<User> activeUsers = userRepository.findAll()
                .stream()
                .filter(user -> user.isActive())
                .collect(Collectors.toList());
        return checkRequests(activeUsers);
    }

    @RequestMapping(value = "/users/cities/{letter}", method = RequestMethod.GET)
    public ResponseEntity getCitiesStartingWith(@PathVariable String letter){
        List<String> cities = new ArrayList<>();
        for (User user: userRepository.findAll()) {
            if(user.getCity().toLowerCase().startsWith(letter.toLowerCase())) {
                cities.add(user.getCity());
            }
        }
        return checkRequests(cities);

    }
    @RequestMapping(value = "/users/{order}", method = RequestMethod.GET)
    public ResponseEntity getUsersOrderedByCreation(@PathVariable String order){
        List<User> orderedUsers = new ArrayList<>(userRepository.findAll());
        Collections.sort(orderedUsers, new CreationDateComparator());

        if(order.equals("asc")){
            return checkRequests(orderedUsers);
        }else if(order.equals("desc")){
            Collections.reverse(orderedUsers);
            return checkRequests(orderedUsers);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid parameters");
    }

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public ResponseEntity addUser(@RequestBody NewUserRequest newUserRequest){
        TodaysDate date = new TodaysDate();
        User user = new User();
        user.setName(newUserRequest.getName());
        user.setSurname(newUserRequest.getSurname());
        user.setBirthday(newUserRequest.getBirthday());
        user.setCity(newUserRequest.getCity());
        user.setActive(newUserRequest.isActive());
        user.setEmail(newUserRequest.getEmail());
        user.setCreation_date(date.getCurrentDate());

        if(bucket.tryConsume(1)) {
            userRepository.save(user);
            return ResponseEntity.ok(userRepository.findAll());
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests");
    }

    private <T> ResponseEntity checkRequests(List<T> object){
        if (bucket.tryConsume(1)){
            return ResponseEntity.ok(object);
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests");
    }

}
