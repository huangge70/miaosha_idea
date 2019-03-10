package com.imooc.miaosha.exception;

import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.net.BindException;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request,Exception e){
        e.printStackTrace();
        if(e instanceof BindException){
            BindException ex= (BindException) e;
            String msg=ex.getMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        }else if(e instanceof GlobalException){
            GlobalException ex= (GlobalException) e;
            return Result.error(ex.getCm());
        }else {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
