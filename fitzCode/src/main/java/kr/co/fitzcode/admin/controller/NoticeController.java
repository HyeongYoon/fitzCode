package kr.co.fitzcode.admin.controller;

import kr.co.fitzcode.admin.dto.NoticeDTO;
import kr.co.fitzcode.admin.service.NoticeService;
import kr.co.fitzcode.admin.exception.NoticeNotFoundException;
import kr.co.fitzcode.admin.exception.InvalidNoticeIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/notice")
public class NoticeController {

    private final NoticeService noticeService;

    // 공지사항 목록 조회
    @GetMapping
    public String getAllNotices(Model model) {
        try {
            List<NoticeDTO> notices = noticeService.getAllNotices();
            model.addAttribute("notices", notices);
            return "admin/notice/list";
        } catch (NoticeNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "/errorPage";
        }
    }

    // 공지사항 작성 폼
    @GetMapping("/add")
    public String createNoticeForm(Model model) {
        model.addAttribute("notice", new NoticeDTO());
        return "admin/notice/create";
    }

    // 공지사항 작성 처리
    @PostMapping
    public String createNotice(NoticeDTO noticeDTO, Model model) {
        try {
            noticeDTO.setUserId(40); //////////////////////////////////////////////////////////////// 임시 userId 설정
            noticeService.createNotice(noticeDTO);
            return "redirect:/admin/notice";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "/errorPage";
        }
    }

    // 공지사항 상세 조회
    @GetMapping("/{id}")
    public String getNoticeById(@PathVariable int id, Model model) {
        try {
            NoticeDTO notice = noticeService.getNoticeById(id);
            model.addAttribute("notice", notice);
            return "admin/notice/detail";
        } catch (NoticeNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "errorPage";
        }
    }

    // 공지사항 수정 폼
    @GetMapping("/{id}/edit")
    public String editNoticeForm(@PathVariable int id, Model model) {
        try {
            NoticeDTO notice = noticeService.getNoticeById(id);
            model.addAttribute("notice", notice);
            return "admin/notice/edit";
        } catch (NoticeNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "/errorPage";
        }
    }

    // 공지사항 수정 처리
    @PostMapping("/{id}")
    public String updateNotice(@PathVariable int id, NoticeDTO noticeDTO, Model model) {
        try {
            noticeDTO.setNoticeId(id);
            noticeService.updateNotice(noticeDTO);
            return "redirect:/admin/notice";
        } catch (InvalidNoticeIdException e) {
            model.addAttribute("error", e.getMessage());
            return "/errorPage";
        }
    }

    // 공지사항 삭제
    @GetMapping("/{id}/delete")
    public String deleteNotice(@PathVariable int id, Model model) {
        try {
            noticeService.deleteNotice(id);
            return "redirect:/admin/notice";
        } catch (InvalidNoticeIdException e) {
            model.addAttribute("error", e.getMessage());
            return "/errorPage";
        }
    }
}