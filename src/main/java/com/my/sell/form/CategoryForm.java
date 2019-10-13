package com.my.sell.form;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CategoryForm {

    private Integer categoryId;

    /** 类目名字. */
    @NotEmpty(message = "类别名称必填")
    private String categoryName;

    /** 类目编号. */
    @NotNull(message = "类别编号必填")
    private Integer categoryType;
}
