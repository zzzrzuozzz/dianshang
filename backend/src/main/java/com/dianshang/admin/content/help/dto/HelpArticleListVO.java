package com.dianshang.admin.content.help.dto;

import lombok.Data;

@Data
public class HelpArticleListVO {

    private String id;
    private Long typeId;
    private String categoryName;
    private String title;
    private String publishTime;
    private Integer clickCount;
    private Integer sort;
    private Integer status;
    private String statusText;
}
