package cn.itcast.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class LoginFilter extends ZuulFilter {

    /**
     * 指定过滤器的类型：pre route post error
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * 执行顺序：返回值越小，优先级越高
     * @return
     */
    @Override
    public int filterOrder() {
        return 10;
    }

    /**
     * 是否执行run方法：true-执行
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * 执行拦截的业务逻辑
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {

        // zuul网关的上下文对象
        RequestContext context = RequestContext.getCurrentContext();
        // 获取请求对象
        HttpServletRequest request = context.getRequest();
        // 获取请求参宿
        String token = request.getParameter("token");
        // 如果没有登录，就拦截
        if (StringUtils.isBlank(token)){
            // 不再转发请求
            context.setSendZuulResponse(false);
            // 设置响应状态码
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            // 设置响应信息
            context.setResponseBody("request error");
        }

        // 什么都不干
        return null;
    }
}
