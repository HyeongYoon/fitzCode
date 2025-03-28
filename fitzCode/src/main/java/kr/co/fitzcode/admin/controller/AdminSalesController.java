package kr.co.fitzcode.admin.controller;

import kr.co.fitzcode.admin.service.AdminSalesService;
import kr.co.fitzcode.common.dto.ChartDataDTO;
import kr.co.fitzcode.common.dto.SalesRankingDTO;
import kr.co.fitzcode.common.dto.SearchRankingDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/sales")
@RequiredArgsConstructor
public class AdminSalesController {

    private final AdminSalesService adminSalesService;

    @GetMapping
    public String getSalesDashboard(Model model) {
        // Spring Security에서 로그인된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedUserName = authentication.getName(); // 사용자 이름
        String loggedUserProfileImage = "/img/default-profile.png"; // 기본 프로필 이미지 (고정)

        // TODO : 알림 기능 구현
        // 알림 상태 확인
        boolean hasNotifications = false;

        // 사용자 정보 모델에 추가
        model.addAttribute("loggedUserName", loggedUserName);
        model.addAttribute("loggedUserProfileImage", loggedUserProfileImage);
        model.addAttribute("hasNotifications", hasNotifications);

        // 차트 데이터
        ChartDataDTO chartData = adminSalesService.getSalesDataForLast30Days();
        model.addAttribute("revenueData", chartData.getRevenueData());
        model.addAttribute("volumeData", chartData.getVolumeData());
        model.addAttribute("labels", adminSalesService.getDailyLabels());
        model.addAttribute("salesRanking", adminSalesService.getSalesRankingForLast14Days(1, 5)); // 첫 페이지 5개
        model.addAttribute("totalCount", adminSalesService.getTotalSalesRankingCount()); // 총 상품 수
        model.addAttribute("searchRanking", adminSalesService.getSearchRanking()); // 검색어 순위 추가
        model.addAttribute("searchTotalCount", adminSalesService.getTotalSearchRankingCount()); // 검색어 총 개수 추가

        // 이번 달 매출 및 지난달 대비 매출 계산
        LocalDate today = LocalDate.now();
        LocalDateTime startOfThisMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfThisMonth = today.atTime(23, 59, 59);
        LocalDateTime startOfLastMonth = today.minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfLastMonth = today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth()).atTime(23, 59, 59);

        // 이번 달 매출
        long totalIncome = adminSalesService.getTotalIncome(startOfThisMonth, endOfThisMonth);
        // 지난달 매출
        long lastMonthIncome = adminSalesService.getTotalIncome(startOfLastMonth, endOfLastMonth);
        // 지난달 대비 매출 증감율 계산
        double expenseGrowthRate = adminSalesService.calculateExpenseGrowthRate(lastMonthIncome, totalIncome);
        // 지난달 대비 증가율
        double incomeGrowthRate = adminSalesService.calculateIncomeGrowthRate(lastMonthIncome, totalIncome);
        // 이번 달 예상 매출
        long predictedIncome = adminSalesService.calculatePredictedIncome(startOfThisMonth, endOfThisMonth, totalIncome, today);

        // 모델에 추가
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("lastMonthIncome", lastMonthIncome);
        model.addAttribute("expenseGrowthRate", expenseGrowthRate);
        model.addAttribute("incomeGrowthRate", incomeGrowthRate);
        model.addAttribute("predictedIncome", predictedIncome);

        log.debug("totalIncome: {}, lastMonthIncome: {}, predictedIncome: {}, expenseGrowthRate: {}, searchRanking: {}",
                totalIncome, lastMonthIncome, predictedIncome, expenseGrowthRate, model.getAttribute("searchRanking"));
        return "admin/sales/salesStatus";
    }

    @GetMapping("/ranking")
    @ResponseBody
    public List<SalesRankingDTO> getSalesRanking(@RequestParam("page") int page) {
        return adminSalesService.getSalesRankingForLast14Days(page, 5); // 페이지당 5개
    }

    @GetMapping("/total-count")
    @ResponseBody
    public int getTotalSalesRankingCount() {
        return adminSalesService.getTotalSalesRankingCount();
    }

    @GetMapping("/search-ranking")
    @ResponseBody
    public List<SearchRankingDTO> getSearchRanking() {
        return adminSalesService.getSearchRanking();
    }

    @GetMapping("/search-total-count")
    @ResponseBody
    public int getTotalSearchRankingCount() {
        return adminSalesService.getTotalSearchRankingCount();
    }
}