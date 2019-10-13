package com.my.sell.exception;

import com.my.sell.enums.ResultEnum;
import lombok.Getter;

/**
 * Created by 廖师兄
 * 2017-06-11 18:55
 */
@Getter
public class SellException extends RuntimeException{

    private Integer code;

    public SellException(ResultEnum resultEnum) {
        super(resultEnum.getMessage());

        this.code = resultEnum.getCode();
    }

    public SellException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
