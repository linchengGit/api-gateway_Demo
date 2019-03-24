package com.lc.apigateway.filter;

import com.lc.apigateway.constant.CookieConstant;
import com.lc.apigateway.constant.RedisConstant;
import com.lc.apigateway.utils.CookieUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;

/**
 * 权限拦截（区分买家和卖家）
 *
 * @author lc
 * @date 2019/3/22
 */
@Component
public class AuthBuyeFilter extends ZuulFilter {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        if ("/order/order/create".equals(request.getRequestURI())) {
            return true;
        }
        return false;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        /**
         * /order/create 只能买家仿问(cookie里有openid)
         * /order/finish 只能卖家仿问(cookie里有token,并且对应的redis中的值)
         * /product/list 都可仿问
         */

        Cookie cookie = CookieUtil.get(request, CookieConstant.OPENID);
        if (cookie == null || StringUtils.isBlank(cookie.getValue())) {
            requestContext.setSendZuulResponse(false);//表示不通过
            requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value()); //设置状态码401权限不通过，
        }


        return null;
    }
}
