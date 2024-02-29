package com.smc.connector.controller;

import com.smc.connector.util.AsyncTask;
import com.smc.connector.model.DatabaseRequestModel;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class DatabaseRequestController {
    @PostMapping(path = "/getData", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getData(@RequestBody Map model) {
        AsyncTask asyncTask = new AsyncTask(new DatabaseRequestModel(model));

        asyncTask.start();

        return "extraction started";
    }
}
