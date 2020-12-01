package codebayapi.demo.utils;

import codebayapi.demo.entity.User;

import java.util.Comparator;

public class CreationDateComparator implements Comparator<User> {
    @Override
    public int compare(User firstUser, User secondUser) {
        return firstUser.getCreation_date().compareTo(secondUser.getCreation_date());
    }
}
