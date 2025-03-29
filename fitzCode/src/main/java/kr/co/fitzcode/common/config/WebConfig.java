package kr.co.fitzcode.common.config;

import kr.co.fitzcode.admin.filter.VisitorLoggingFilter;
import kr.co.fitzcode.admin.mapper.DashboardMapper;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.net.InetAddress;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Bean
    public FilterRegistrationBean<VisitorLoggingFilter> visitorLoggingFilterRegistration() {
        FilterRegistrationBean<VisitorLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new VisitorLoggingFilter(dashboardMapper));
        registrationBean.addUrlPatterns("/*"); // 모든 경로
        registrationBean.setOrder(1); // 필터 순서
        return registrationBean;
    }

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        try {
//            registry.addInterceptor(new GeoIpInterceptor())
//                    .addPathPatterns("/**")
//                    .excludePathPatterns("/error");
//            System.out.println("GeoIpInterceptor 성공적으로 등록됨");
//        } catch (IOException e) {
//            throw new RuntimeException("GeoIpInterceptor 초기화 실패: " + e.getMessage(), e);
//        }
//    }
//
//    public static class GeoIpInterceptor implements HandlerInterceptor {
//
//        private final DatabaseReader dbReader;
//
//        public GeoIpInterceptor() throws IOException {
//            var resource = new ClassPathResource("GeoLite2-Country.mmdb");
//            if (!resource.exists()) {
//                throw new IOException("GeoLite2-Country.mmdb 파일이 없음");
//            }
//            var database = resource.getFile();
//            this.dbReader = new DatabaseReader.Builder(database).build();
//        }
//
//        @Override
//        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
//                throws Exception {
//            String ipAddress = getClientIp(request);
//
//            if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "127.0.0.1".equals(ipAddress)) {
//                return true;
//            }
//
//            try {
//                InetAddress inetAddress = InetAddress.getByName(ipAddress);
//                CountryResponse countryResponse = dbReader.country(inetAddress);
//                String countryCode = countryResponse.getCountry().getIsoCode();
//                if (countryCode == null) {
//                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "국가 정보를 확인할 수 없음");
//                    return false;
//                }
//                return switch (countryCode) {
//                    case "KR", "CA" -> true;
//                    default -> {
//                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "한국과 캐나다에서만 접속 가능");
//                        yield false;
//                    }
//                };
//            } catch (GeoIp2Exception | IOException e) {
//                response.sendError(HttpServletResponse.SC_FORBIDDEN, "위치를 확인할 수 없음: " + e.getMessage());
//                return false;
//            }
//        }
//
//        private String getClientIp(HttpServletRequest request) {
//            String ipAddress = request.getHeader("X-Forwarded-For");
//            if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
//                return ipAddress.split(",")[0].trim();
//            }
//            return request.getRemoteAddr();
//        }
//    }
}