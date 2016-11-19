package com.github.mrtheedge.twitchbot;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by E.J. Schroeder on 11/18/2016.
 *
 * Tests the functionality of SpamFilter
 */
public class SpamFilterTest {

    SpamFilter sf;

    @Before
    public void setUp() throws Exception {
        sf = new SpamFilter();
    }

    @Test
    public void testIsSpam() throws Exception {
        String message = "Hey I love the stream!!";
        String user = "generic_user";
        assertEquals(SpamType.NONE, sf.isSpam(user, message));

        message = "HEY I LOVE THE STREAM"; // More than 75% caps
        assertEquals(SpamType.CAPS, sf.isSpam(user, message));

        message = "Hey I Love The Stream"; // Less than 75% caps
        assertEquals(SpamType.NONE, sf.isSpam(user, message));

        message = "Check out this link: www.google.com";
        assertEquals(SpamType.LINK, sf.isSpam(user, message));
    }

    @Test
    public void testPardonUser() throws Exception {
        String user = "generic_user";
        String message = "This is the link: www.google.com";
        sf.pardonUser(user);

        assertEquals(SpamType.NONE, sf.isSpam(user, message));

        assertEquals(SpamType.LINK, sf.isSpam(user, message));
    }

    @Test
    public void testAddWordToBlacklist() throws Exception {

        String user = "generic_user";
        String message = "The word foobar should be blacklisted.";

        assertEquals(SpamType.NONE, sf.isSpam(user, message));

        sf.addWordToBlacklist("foobar");

        assertEquals(SpamType.BLACKLISTED, sf.isSpam(user, message));

    }

    @Test
    public void testRemoveWordFromBlacklist() throws Exception {

        String user = "generic_user";
        String message = "The word foobar should be blacklisted.";

        sf.addWordToBlacklist("foobar");
        assertEquals(SpamType.BLACKLISTED, sf.isSpam(user, message));

        sf.removeWordFromBlacklist("foobar");
        assertEquals(SpamType.NONE, sf.isSpam(user, message));

    }
}