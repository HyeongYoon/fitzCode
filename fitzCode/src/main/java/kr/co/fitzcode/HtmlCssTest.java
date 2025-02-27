package kr.co.fitzcode;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HtmlCssTest {

    @GetMapping("/fragments/footer")
    public String footer() {
        return "fragments/footer";
    }

    @GetMapping("/fragments/header")
    public String header() {
        return "fragments/header";
    }

    @GetMapping("/fragments/managerSidebar")
    public String sidebar() {
        return "fragments/managerSidebar";
    }

    @GetMapping("/fragment/weeklyNewUsers")
    public String weeklyNewUsers() {
        return "fragments/weeklyNewUsers";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard/dashboard";
    }
}