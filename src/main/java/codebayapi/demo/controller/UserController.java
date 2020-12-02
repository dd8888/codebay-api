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

/**
 * This class declares all the necessary endpoints for the API. Also, using the library
 * called Bucket4J I was able to limit the calls made to the API.
 */
@RestController("/user")
public class UserController {
    private UserRepository userRepository;
    private final Bucket bucket;

    @Autowired
    public UserController(UserRepository userRepository) {
        //Sets a maximum number of requests to 3 every 10 seconds.
        Bandwidth limit = Bandwidth.classic(3, Refill.greedy(3, Duration.ofSeconds(10)));
        this.bucket = Bucket4j.builder()
                .addLimit(limit)
                .build();
        this.userRepository = userRepository;
    }

    /**
     * Gets the active users based on the value "isActive"
     * @return an array with the active users or an error if the amount of request has been exceeded.
     */
    @GetMapping(value = "/activeUsers")
    public ResponseEntity getActiveUsers(){
        List<User> activeUsers = userRepository.findAll()
                .stream()
                .filter(user -> user.isActive())
                .collect(Collectors.toList());
        return checkRequests(activeUsers);
    }

    /**
     * Gets the cities starting with a certain letter
     * @param letter must be a char
     * @return an array with the cities that starts with the letter passed as a parameter
     */
    @GetMapping(value = "/users/cities/{letter}")
    public ResponseEntity getCitiesStartingWith(@PathVariable String letter){
        List<String> cities = new ArrayList<>();
        for (User user: userRepository.findAll()) {
            if(user.getCity().toLowerCase().startsWith(letter.toLowerCase()) && !cities.contains(user.getCity())) {
                cities.add(user.getCity());
            }
        }
        return checkRequests(cities);

    }

    /**
     * Gets the users ordered either ascendent or descendent
     * @param order is a string that must be "asc" or "desc"
     * @return the users ordered based on the param "order"
     */
    @GetMapping(value = "/users/{order}")
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

    /**
     * Posts a new user
     * @param newUserRequest must be an User object without the "creationDate"
     * @return the user posted if it worked
     */
    @GetMapping(value = "/users")
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

    /**
     * Checks if the amount of requests is lower than 3 in the last 10 seconds.
     * @param object the list to return
     * @param <T> is a generic since the are various types of objects being used
     * @return  The list of objects if everything worked. If not, returns an error.
     */
    private <T> ResponseEntity checkRequests(List<T> object){
        if (bucket.tryConsume(1)){
            return ResponseEntity.ok(object);
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests");
    }

}
