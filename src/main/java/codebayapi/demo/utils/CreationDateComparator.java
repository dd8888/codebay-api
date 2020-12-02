package codebayapi.demo.utils;

import codebayapi.demo.entity.User;

import java.util.Comparator;

/**
 * Override of the method compare that compares between the creation dates of two users
 * to create an ordered list based on the creation dates.
 */
public class CreationDateComparator implements Comparator<User> {
    @Override
    public int compare(User firstUser, User secondUser) {
        return firstUser.getCreation_date().compareTo(secondUser.getCreation_date());
    }
}
