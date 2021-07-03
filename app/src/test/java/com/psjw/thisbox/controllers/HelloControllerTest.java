package com.psjw.thisbox.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HelloControllerTest {
    HelloController controller = new HelloController();

    @Test
    public void hello(){
        assertEquals("hello modify",controller.hello());
    }
}