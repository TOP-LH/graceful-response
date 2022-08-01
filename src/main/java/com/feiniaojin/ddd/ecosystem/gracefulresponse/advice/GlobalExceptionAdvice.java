package com.feiniaojin.ddd.ecosystem.gracefulresponse.advice;

import com.feiniaojin.ddd.ecosystem.gracefulresponse.ExceptionAliasRegister;
import com.feiniaojin.ddd.ecosystem.gracefulresponse.GracefulResponseProperties;
import com.feiniaojin.ddd.ecosystem.gracefulresponse.api.ExceptionAliasFor;
import com.feiniaojin.ddd.ecosystem.gracefulresponse.api.ExceptionMapper;
import com.feiniaojin.ddd.ecosystem.gracefulresponse.api.ResponseFactory;
import com.feiniaojin.ddd.ecosystem.gracefulresponse.api.ResponseStatusFactory;
import com.feiniaojin.ddd.ecosystem.gracefulresponse.data.Response;
import com.feiniaojin.ddd.ecosystem.gracefulresponse.data.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 全局异常处理.
 *
 * @author <a href="mailto:943868899@qq.com">Yujie</a>
 * @version 0.1
 * @since 0.1
 */
@ControllerAdvice
public class GlobalExceptionAdvice {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionAdvice.class);

    @Resource
    private ResponseStatusFactory responseStatusFactory;

    @Resource
    private ResponseFactory responseFactory;

    @Resource
    private ExceptionAliasRegister exceptionAliasRegister;

    @Resource
    private GracefulResponseProperties gracefulResponseProperties;

    /**
     * 异常处理逻辑.
     *
     * @param throwable 业务逻辑抛出的异常
     * @return 统一返回包装后的结果
     */
    @ExceptionHandler({Throwable.class})
    @ResponseBody
    public Response exceptionHandler(Throwable throwable) {
        if (gracefulResponseProperties.isPrintExceptionInGlobalAdvice()) {
            logger.error("GlobalExceptionAdvice捕获到异常", throwable);
        }
        //校验异常转自定义异常
        Class<? extends Throwable> throwableClass = throwable.getClass();

        ResponseStatus statusLine = generateResponseStatus(throwableClass);

        return responseFactory.newInstance(statusLine);
    }

    private ResponseStatus generateResponseStatus(Class<? extends Throwable> clazz) {

        ExceptionMapper exceptionMapper = clazz.getAnnotation(ExceptionMapper.class);

        if (exceptionMapper != null) {
            ResponseStatus responseStatus = responseStatusFactory.newInstance(exceptionMapper.code(),
                    exceptionMapper.msg());
            return responseStatus;
        }

        //获取已注册的别名
        ExceptionAliasFor exceptionAliasFor = exceptionAliasRegister.getExceptionAliasFor(clazz);
        if (exceptionAliasFor != null) {
            ResponseStatus responseStatus = responseStatusFactory.newInstance(exceptionAliasFor.code(),
                    exceptionAliasFor.msg());
            return responseStatus;
        }

        return responseStatusFactory.defaultFail();
    }
}