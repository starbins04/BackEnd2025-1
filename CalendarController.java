package com.example.finalproject.controller;

import com.example.finalproject.entity.Calendar;
import com.example.finalproject.dto.CalendarForm;
import com.example.finalproject.repository.CalendarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Controller
public class CalendarController {
    @Autowired
    private CalendarRepository calendarRepository;

    @GetMapping("/calendar")
    public String calendarPage(Model model) {
        return "calendar";
    }

    @GetMapping("/calendar/new")
    public String newCalendarForm() {
        return "new"; // 등록 폼 (날짜 없음)
    }

    @GetMapping("/calendar/new/{date}")
    public String newCalendarFormWithDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        Calendar calendar = new Calendar();
        calendar.setDate(date);
        model.addAttribute("calendar", calendar);
        return "new"; // 날짜가 채워진 등록 폼
    }

    @PostMapping("/calendar/create")
    public String createCalendar(CalendarForm form) {
        Calendar calendar = form.toEntity();
        Calendar saved = calendarRepository.save(calendar);
        return "redirect:/calendar/" + saved.getDate(); // 예: 2025-05-18
    }

    @GetMapping("/calendar/{date}")
    public String showCalendar(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                               Model model, RedirectAttributes redirectAttributes) {
        Optional<Calendar> optionalCalendar = calendarRepository.findByDate(date);

        if (optionalCalendar.isPresent()) {
            model.addAttribute("calendar", optionalCalendar.get());
            return "calendars/show";
        } else {
            // 날짜만 채워서 new 폼으로 넘기기
            redirectAttributes.addFlashAttribute("calendar", new Calendar(date));
            return "redirect:/calendar/new?date=" + date.toString();
        }
    }

    //캘린더 시간 표시
    @GetMapping("/events")
    @ResponseBody
    public List<Map<String, Object>> getEvents() {
        List<Calendar> calendars = calendarRepository.findAll();

        List<Map<String, Object>> events = new ArrayList<>();

        for (Calendar cal : calendars) {
            // 1. 시간 차이 계산
            LocalTime start = cal.getStartTime();
            LocalTime end = cal.getEndTime();
            long workMinutes = Duration.between(start, end).toMinutes();

            // 2. 휴게 시간 빼기
            long breakMinutes = cal.getBreakTime(); // 분 단위
            long realMinutes = workMinutes - breakMinutes;

            // 3. 시/분으로 변환
            long hours = realMinutes / 60;
            long minutes = realMinutes % 60;

            // 4. 표시할 제목
            String displayTime = hours + "시간" + (minutes > 0 ? " " + minutes + "분" : "");
            Map<String, Object> event = new HashMap<>();
            event.put("title", "⏰ " + displayTime);
            event.put("start", cal.getDate().toString()); // 날짜는 LocalDate라고 가정
            event.put("allDay", true);
            events.add(event);
        }
        return events;
    }

    /*@GetMapping("/calendar")
    public String list(Model model) {
        List<Calendar> calendarList = calendarRepository.findAll();
        model.addAttribute("calendarList", calendarList);
        return "calendar/index";
    }*/

    /*@GetMapping("articles/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model) {
        Article articleEntity = articleRepository.findById(id).orElse(null);
        model.addAttribute("article", articleEntity);
        return "articles/edit";
    }

    @PostMapping ("articles/update")
    public String update(ArticleForm articleForm) {
        Article article = articleForm.toEntity();
        Article target = articleRepository.findById(article.getId()).orElse(null);
        if(target != null) {
            articleRepository.save(article);
        }
        return "redirect:/articles/" + article.getId();
    }

    @GetMapping("/articles/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes rttr) {
        Article target = articleRepository.findById(id).orElse(null);
        if( target != null) {
            articleRepository.delete(target);
            rttr.addFlashAttribute("msg", "삭제되었습니다.");
        }
        return "redirect:/articles";
    }*/
}