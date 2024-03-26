package study.querydsl.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloContoller {
    @GetMapping("/")
    public String Home(){
        return "안녕 querDsl";
    }
}
