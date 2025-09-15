/**
 * This package contains the controller classes that handle incoming requests and send responses back to the client.
 * Each controller is responsible for a specific set of endpoints or functionalities in the application.
 * Controllers receive input from the client via HTTP requests, perform necessary processing or business logic, and
 * return a response in a format such of JSON.
 * They may interact with other components such as services, repositories, or models to perform their tasks.
 * The classes in this package should follow the naming convention of suffixing the class name with "Controller", e.g.
 * "UserController". They should also be annotated with the appropriate Spring annotations such
 * as @RestController or @Controller, and @RequestMapping or @GetMapping, depending on the HTTP method used.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
package com.mss.controller;
